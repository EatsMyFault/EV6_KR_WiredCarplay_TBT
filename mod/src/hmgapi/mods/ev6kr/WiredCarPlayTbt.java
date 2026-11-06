package hmgapi.mods.ev6kr;

import hmgapi.hook.Car;
import hmgapi.hook.LoadedApp;
import hmgapi.hook.MethodHook;
import hmgapi.hook.ModContext;
import hmgapi.hook.ModEntry;
import hmgapi.hook.Points;

/**
 * EV6 KOR 260902 wired CarPlay TBT only (v0.1.4, not vehicle validated).
 *
 * No GPM (Android Auto), Bluetooth bridge, wireless projection, audio conversion,
 * Wi-Fi feature flags or native patches are ever requested.
 * Reuses the official HMG-API's CarPlay iAP2 listener + CpTurns implementation.
 *
 * Source base: VysseCodes/HMG-API v1.0.1, AGPL-3.0.
 * THIS HAS NOT BEEN TESTED ON A VEHICLE.
 */
public final class WiredCarPlayTbt extends ModEntry {
    private static final String NAV = "com.mnsoft.navi";
    private static final String DIPO = "com.daudio.app.dipo";
    private static final String IAP2 = "com.tcc.iap2server";
    private static final String[] NAVI_TBT = {
        "update_tbt_info", "update_tbt_sub_info", "update_tbt_sub_info_b",
        "update_tbt_sub_info2", "update_tbt_sub_info2_b", "update_tbt_sub_info_2c",
        "update_remain_dis", "update_tp_crossroad_name", "update_tp_destination_name",
        "update_tp_turn_list", "update_tp_turn_list_new", "update_tp_lane_info"
    };

    @Override
    public void appLoaded(final ModContext mod, final LoadedApp app) {
        if (!mod.settingBoolean("tbt", true)) return;
        if (NAV.equals(app.packageName)) {
            nav(mod, app);
        } else if (IAP2.equals(app.packageName)) {
            iap2(mod, app);
        } else if (DIPO.equals(app.packageName)) {
            dipo(mod, app);
        }
    }

    private void nav(final ModContext mod, final LoadedApp app) {
        if (!mod.settingBoolean("yield_stock", true)) {
            mod.log("EV6_TBT_014: NATIVE_YIELD_DISABLED");
            return;
        }
        MethodHook yieldToPhone = new MethodHook() {
            @Override
            protected void beforeHookedMethod(Param p) {
                if (Car.carPlayGuiding(app.context)) p.setResult(null);
            }
        };
        for (String p : NAVI_TBT) {
            mod.hook(null, "framework.navigation_cluster." + p, yieldToPhone);
        }
        mod.hook(null, "framework.navigation_cluster.update_lane_info", new MethodHook() {
            @Override
            protected void beforeHookedMethod(Param p) {
                byte[] frame = (byte[]) p.args[0];
                if (frame != null && frame.length > 0 && frame[0] == (byte) 0xF1
                        && Car.carPlayGuiding(app.context)) p.setResult(null);
            }
        });
        mod.log("EV6_TBT_014: stock navigation yields TBT calls ONLY while CarPlay guides");
    }

    private void iap2(final ModContext mod, final LoadedApp app) {
        final ClassLoader cl = app.classLoader;
        mod.hook(cl, "iap2.init_info.check_supports_features", new MethodHook() {
            @Override
            protected void afterHookedMethod(Param p) {
                try {
                    Points.field(cl, "iap2.init_info.route_guidance").setInt(null, 1);
                    mod.log("EV6_TBT_014: RG_ADVERTISED");
                } catch (Throwable t) {
                    mod.error("Wired CarPlay TBT: iAP2 RouteGuidance", t);
                }
            }
        });
    }

    private void dipo(ModContext mod, LoadedApp app) {
        try {
            mod.appPart(app, "hmgapi.mods.wireless.dipo.Dipo")
               .getMethod("start", ModContext.class, LoadedApp.class)
               .invoke(null, mod, app);
        } catch (Throwable t) {
            mod.error("Wired CarPlay TBT: DiPO listener", t instanceof java.lang.reflect.InvocationTargetException
                    ? t.getCause() : t);
        }
    }
}
