package hmgapi.mods.wireless.dipo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.tcc.iap2.IIap2Listener;
import com.tcc.iap2.IIap2Service;
import com.tcc.iap2.Iap2ListenerInfo;
import com.tcc.iap2.Iap2NotiMsg;

import hmgapi.hook.Car;
import hmgapi.hook.Cluster;
import hmgapi.hook.LoadedApp;
import hmgapi.hook.MethodHook;
import hmgapi.hook.ModContext;

/**
 * EV6 CVEV KOR 260902, stock WIRED CarPlay only. Experimental v0.1.4.
 * The transport and wireless bridge code from IONIQ5 is NOT included.
 * The listener/session ownership and CarPlay-specific guarding were informed by
 * b-lack-kr/ioniq5-external-wirelss-carplay (AGPL-3.0).
 * No vehicle validation: do not treat a successful binder call as TBT display.
 */
public final class Dipo {
    private Dipo() {}

    private static final String IAP2_ACTION = "com.remote.service.IAP2SERVICE";
    private static final int RG_TYPE = 4096;
    private static final int RG_UPDATE = 120, RG_MANEUVER = 121, RG_LANES = 122;
    private static final int USB_COMPONENT = 1;
    private static final String[] RG_COMPONENTS = { "RouteGuidanceComponent0" };
    private static final long POLL_MS = 2000;
    private static final long GUIDANCE_RECOVERY_MS = 2 * POLL_MS;
    private static final int MAX_RETRIES = 3;

    static ModContext mod;
    static LoadedApp app;
    static volatile IIap2Service service;
    static volatile RgListener listener;
    static Handler handler;
    static CpTurns turns;
    static final ThreadLocal<Boolean> OWN = new ThreadLocal<Boolean>();

    // All lifecycle and request state below is owned by the handler thread.
    private static boolean projectionStarted, usbConnected, requested, requestPending;
    private static boolean sourceRefreshPending, guideRepeatSent, lastGuiding, waitingForGuidance;
    private static int generation, attempts, rxTotal, rxRoute, rxManeuver, rxLanes;
    private static int noListenerDeferrals;
    private static boolean pollRunning;

    static void log(String s) { if (mod != null) mod.log("EV6_TBT_014: " + s); }
    static void error(String s, Throwable t) { if (mod != null) mod.error("EV6_TBT_014: " + s, t); }

    public static void start(ModContext m, LoadedApp a) {
        mod = m;
        app = a;
        if (!initialize()) return;
        final ServiceConnection connection = new ServiceConnection() {
            @Override public void onServiceConnected(ComponentName name, IBinder binder) {
                final IIap2Service s = IIap2Service.Stub.asInterface(binder);
                if (s == null) {
                    log("IAP2_SERVICE_NULL");
                    return;
                }
                service = s;
                register(s);
                post(new Runnable() {
                    @Override public void run() {
                        if (service == s) {
                            log("IAP2_SERVICE_BOUND");
                            noListenerDeferrals = 0;
                            if (active()) schedule(350, "service-bound");
                        }
                    }
                });
            }
            @Override public void onServiceDisconnected(ComponentName name) {
                service = null;
                listener = null;
                post(new Runnable() {
                    @Override public void run() {
                        projectionStarted = false;
                        usbConnected = false;
                        generation++;
                        resetRequest();
                        turns.disconnected();
                        log("IAP2_SERVICE_LOST");
                    }
                });
            }
        };
        try {
            boolean bound = a.context.bindService(new Intent(IAP2_ACTION), connection,
                    Context.BIND_AUTO_CREATE);
            log("IAP2_BIND requested=" + bound);
        } catch (Throwable t) {
            error("IAP2_BIND_FAILED", t);
        }
    }

    private static boolean initialize() {
        try {
            if (Car.stepOfSpec() >= 999) {
                log("STOCK_CARPLAY_TBT_EXISTS: refusing competing listener");
                return false;
            }
            Cluster cluster = Cluster.of(app.context);
            if (cluster == null) {
                log("CLUSTER_UNAVAILABLE: listener disabled");
                return false;
            }
            HandlerThread thread = new HandlerThread("ev6-kr-cp-tbt-014");
            thread.start();
            handler = new Handler(thread.getLooper());
            turns = new CpTurns(app.context, cluster, new Runnable() {
                @Override public void run() { queueRouteRefresh(); }
            });
            // Observe only. Do not replace the head unit's stock STOP call.
            mod.hook(app.classLoader, "dipo.iap2_proxy.stop_route_guidance", new MethodHook() {
                @Override protected void afterHookedMethod(Param p) {
                    if (OWN.get() != null || p.getThrowable() != null
                            || !Boolean.TRUE.equals(p.getResult())) return;
                    final RgListener target = listener;
                    post(new Runnable() {
                        @Override public void run() {
                            if (!active() || !current(target)) return;
                            final int token = ++generation;
                            resetRequest();
                            waitingForGuidance = true;
                            lastGuiding = guiding();
                            turns.reset();
                            turns.stop();
                            log("STOCK_STOP observed; waiting for CarPlay guidance to resume");
                            handler.postDelayed(new Runnable() {
                                @Override public void run() {
                                    recoverGuidanceAfterStop(token, target);
                                }
                            }, GUIDANCE_RECOVERY_MS);
                        }
                    });
                }
            });
            mod.hook(app.classLoader, "dipo.carplay.session_started", new MethodHook() {
                @Override protected void afterHookedMethod(Param p) {
                    if (p.getThrowable() != null) return;
                    final int transport = p.args != null && p.args.length > 0
                            && p.args[0] instanceof Number ? ((Number) p.args[0]).intValue() : -1;
                    post(new Runnable() {
                        @Override public void run() {
                            // No wireless bridge is present in this module: we only
                            // observe the native, wired CarPlay session.
                            log("PROJECTION_STARTED transport=" + transport);
                            if (!projectionStarted) {
                                projectionStarted = true;
                                generation++;
                                resetRequest();
                            }
                            ensurePoll();
                            schedule(1200, "projection-started");
                        }
                    });
                }
            });
            mod.hook(app.classLoader, "dipo.carplay.session_finalized", new MethodHook() {
                @Override protected void afterHookedMethod(Param p) {
                    post(new Runnable() {
                        @Override public void run() {
                            projectionStarted = false;
                            usbConnected = false;
                            generation++;
                            resetRequest();
                            turns.disconnected();
                            log("PROJECTION_ENDED");
                        }
                    });
                }
            });
            log("LISTENER_READY wired-only; no Wi-Fi/native/AA hooks");
            return true;
        } catch (Throwable t) {
            error("INITIALIZE", t);
            return false;
        }
    }

    private static void post(final Runnable action) {
        Handler h = handler;
        if (h == null) return;
        h.post(new Runnable() {
            @Override public void run() {
                try { action.run(); }
                catch (Throwable t) { error("HANDLER", t); }
            }
        });
    }

    private static void register(IIap2Service s) {
        final RgListener incoming = new RgListener(s);
        listener = incoming; // can receive callbacks synchronously in Iap2SvcInit
        try {
            Iap2ListenerInfo info = new Iap2ListenerInfo();
            info.onSetSupportType(RG_TYPE);
            info.onSetListenerName("HMGAPI_RG");
            boolean ok = s.Iap2SvcInit(incoming, info);
            if (!ok && listener == incoming) listener = null;
            log("LISTENER_REGISTERED=" + ok);
        } catch (Throwable t) {
            if (listener == incoming) listener = null;
            error("LISTENER_REGISTER", t);
        }
    }

    private static boolean active() { return projectionStarted || usbConnected; }
    private static boolean current(RgListener l) {
        return l != null && l == listener && l.service == service && mod.enabled();
    }
    private static boolean guiding() {
        try { return Car.carPlayGuiding(app.context); }
        catch (Throwable t) { return false; }
    }
    private static void resetRequest() {
        requested = requestPending = sourceRefreshPending = guideRepeatSent = false;
        lastGuiding = waitingForGuidance = false;
        attempts = rxTotal = rxRoute = rxManeuver = rxLanes = 0;
        noListenerDeferrals = 0;
    }

    /** One lightweight CarPlay-only transition monitor. No polling while disconnected. */
    private static void ensurePoll() {
        if (pollRunning || handler == null) return;
        pollRunning = true;
        handler.post(new Runnable() {
            @Override public void run() {
                try {
                    if (!active() || !mod.enabled()) {
                        pollRunning = false;
                        return;
                    }
                    boolean now = guiding();
                    if (now != lastGuiding) {
                        log("GUIDANCE_STATE active=" + now);
                        if (now) {
                            waitingForGuidance = false;
                            if (!requested) {
                                // Attempts made before this route started must not consume its budget.
                                attempts = 0;
                                schedule(450, "guidance-became-active");
                            }
                            else if (rxTotal == 0 && !guideRepeatSent) {
                                guideRepeatSent = true;
                                // Same START as the original request; intentionally
                                // NO forced STOP of stock USB route guidance.
                                schedule(650, "guidance-repeat-start");
                            }
                        }
                        lastGuiding = now;
                    }
                    handler.postDelayed(this, POLL_MS);
                } catch (Throwable t) {
                    error("GUIDANCE_MONITOR", t);
                    handler.postDelayed(this, POLL_MS);
                }
            }
        });
    }

    private static void schedule(long delay, final String reason) {
        if (!active() || requestPending || sourceRefreshPending || waitingForGuidance
                || attempts >= MAX_RETRIES) return;
        if (requested && !"guidance-repeat-start".equals(reason)) return;
        requestPending = true;
        final int token = generation;
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (generation != token) return;
                requestPending = false;
                if (active() && (!requested || "guidance-repeat-start".equals(reason)))
                    request(reason, true);
            }
        }, delay);
    }

    /** A brief off/on transition can fall between polls. Probe only with current CarPlay ownership. */
    private static void recoverGuidanceAfterStop(int token, RgListener target) {
        if (generation != token || !waitingForGuidance || !active()
                || !current(target) || !guiding()) return;
        log("GUIDANCE_RECOVERY: CarPlay still reports active guidance");
        request("guidance-after-stock-stop", false);
        if (requested) {
            lastGuiding = true;
            waitingForGuidance = false;
        }
    }

    private static void request(String reason, boolean retry) {
        final RgListener target = listener;
        if (!current(target)) {
            noListenerDeferrals++;
            if (noListenerDeferrals <= MAX_RETRIES)
                log("REQUEST_DEFERRED no-current-listener count=" + noListenerDeferrals);
            if (retry && noListenerDeferrals < MAX_RETRIES)
                schedule(1700, "listener-not-ready");
            return;
        }
        noListenerDeferrals = 0;
        attempts++;
        try {
            // Initial/repeat START only. Never steal the USB session with STOP.
            boolean ok = target.service.Iap2SvcStartRouteGuidanceUpdate(RG_COMPONENTS, true, true);
            requested = ok;
            log("REQUEST_START accepted=" + ok + " attempt=" + attempts + " reason=" + reason);
            if (!ok) {
                if (retry) schedule(2700, "rejected-retry");
            }
            else if (rxTotal == 0) {
                final int token = generation;
                handler.postDelayed(new Runnable() {
                    @Override public void run() {
                        if (generation == token && active() && rxTotal == 0)
                            log("NO_RG_MESSAGES after START; app must be guiding to diagnose");
                    }
                }, 12000);
            }
        } catch (Throwable t) {
            error("REQUEST_START_EXCEPTION", t);
            if (retry) schedule(2700, "exception-retry");
        }
    }

    /** Called on the TBT handler by CpTurns. Defer the binder call until parsing returns. */
    private static void queueRouteRefresh() {
        final RgListener target = listener;
        final int token = generation;
        post(new Runnable() {
            @Override public void run() {
                if (generation == token) refreshRouteGuidance(target);
            }
        });
    }

    private static void refreshRouteGuidance(final RgListener target) {
        if (!active() || !current(target) || sourceRefreshPending || waitingForGuidance) return;
        // The IONIQ 5 implementation refreshes Stop -> Start when CarPlay owns
        // guidance. Never STOP a potentially stock-owned USB route while the
        // Korean EV6's CarPlay guidance state is unknown/false.
        if (guiding()) {
            try {
                OWN.set(Boolean.TRUE);
                boolean stopped = target.service.Iap2SvcStopRouteGuidanceUpdate(RG_COMPONENTS);
                log("SOURCE_SWITCH_STOP accepted=" + stopped);
                if (!stopped && !guiding()) return;
            } catch (Throwable t) {
                error("SOURCE_SWITCH_STOP", t);
                return;
            } finally {
                OWN.remove();
            }
        } else {
            log("SOURCE_SWITCH_NO_STOP guiding=false");
        }
        // Replace any queued retry with this refresh's delayed START.
        final int token = ++generation;
        requestPending = false;
        sourceRefreshPending = true;
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (generation != token) return;
                sourceRefreshPending = false;
                requested = false;
                attempts = 0;
                if (!active() || !current(target)) return;
                schedule(0, "source-switch");
            }
        }, 200);
    }

    static final class RgListener extends IIap2Listener.Stub {
        final IIap2Service service;
        RgListener(IIap2Service service) { this.service = service; }

        @Override public void connected(final int component, String serial) {
            post(new Runnable() {
                @Override public void run() {
                    if (!current(RgListener.this)) return;
                    log("IAP2_CONNECTED component=" + component);
                    if (component == USB_COMPONENT) {
                        usbConnected = true;
                        generation++;
                        resetRequest();
                        ensurePoll();
                        schedule(1200, "usb-component-connected");
                    }
                }
            });
        }
        @Override public void disconnected(final int component) {
            post(new Runnable() {
                @Override public void run() {
                    if (!current(RgListener.this)) return;
                    log("IAP2_DISCONNECTED component=" + component);
                    if (component != USB_COMPONENT) return; // component 2 is not USB
                    usbConnected = false;
                    projectionStarted = false;
                    generation++;
                    resetRequest();
                    turns.disconnected();
                }
            });
        }
        @Override public void notify(final int what, int arg1, int arg2,
                                     final Iap2NotiMsg msg) {
            if (what != RG_UPDATE && what != RG_MANEUVER && what != RG_LANES) return;
            post(new Runnable() {
                @Override public void run() {
                    if (!current(RgListener.this) || !active() || waitingForGuidance) return;
                    rxTotal++;
                    if (what == RG_UPDATE) rxRoute++;
                    else if (what == RG_MANEUVER) rxManeuver++;
                    else rxLanes++;
                    if (rxTotal <= 8 || rxTotal % 50 == 0)
                        log("RG_RECEIVED type=" + what + " count=" + rxTotal
                                + " guide=" + guiding() + " null=" + (msg == null));
                    if (msg == null) return;
                    // The original HMG-API v1.0.1 CpTurns parser is preserved.
                    // "PARSED" proves code ran, not that a cluster/HUD displayed it.
                    try {
                        if (what == RG_UPDATE) turns.routeGuidance(msg);
                        else if (what == RG_MANEUVER) turns.maneuver(msg);
                        else turns.lanes(msg);
                        if (rxTotal <= 3) log("RG_PARSED type=" + what);
                    } catch (Throwable t) {
                        error("RG_PARSE_FAILED type=" + what, t);
                    }
                }
            });
        }
    }
}
