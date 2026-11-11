#!/usr/bin/env bash
set -Eeuo pipefail

base=$(cd -- "$(dirname -- "$0")" && pwd)
repo=${HMGAPI_BUILD:-/mnt/c/EV6_TBT/HMG-API-v1.0.1}
key=${EV6_SIGNING_KEY:-/mnt/c/EV6_TBT/LOCAL-PRIVATE-KEY/ev6kr-tbt-author.key}
sdk=${ANDROID_HOME:-$HOME/Android}
android_jar="$sdk/platforms/android-29/android.jar"
d8="$sdk/build-tools/35.0.0/d8"
signing=hmgapi.engine.Signing
uuid=6e427e68-3b31-80ab-b159-a4513e1d7995

[[ -f "$android_jar" && -x "$d8" ]] || { echo "Android 29 and build-tools 35.0.0 are required." >&2; exit 1; }
[[ -d "$repo/out/classes" && -f "$repo/out/engine-host/hmgapi/engine/Signing.class" ]] || {
  echo "Build the HMG-API v1.0.1 runtime and host tools first." >&2
  exit 1
}
[[ -f "$key" && -f "$key.pub" ]] || { echo "The existing EV6 signing key and public key are required." >&2; exit 1; }
[[ $(java -cp "$repo/out/engine-host" "$signing" uuid "$key.pub") == "$uuid" ]] || {
  echo "The signing public key does not match the existing EV6 mod." >&2
  exit 1
}

mkdir -p "$base/.build"
work=$(mktemp -d "$base/.build/build.XXXXXX")
mkdir -p "$work/classes" "$work/stub-classes" "$work/payload"
mapfile -d '' sources < <(find "$base/mod/src" -name '*.java' -print0)
mapfile -d '' stubs < <(find "$base/mod/stubs" -name '*.java' -print0)
javac --release 8 -cp "$android_jar" -d "$work/stub-classes" "${stubs[@]}"
javac --release 8 -cp "$android_jar:$repo/out/classes:$work/stub-classes" \
  -d "$work/classes" "${sources[@]}"
mapfile -d '' classes < <(find "$work/classes" -name '*.class' -print0)
"$d8" --release --min-api 19 --lib "$android_jar" --classpath "$repo/out/classes" \
  --classpath "$work/stub-classes" --output "$work/payload/hooks.jar" "${classes[@]}"
cp "$base/mod/mod.json" "$base/mod/settings.json" "$work/payload/"
cp -R "$base/mod/stubs" "$work/payload/"
java -cp "$repo/out/engine-host" "$signing" sign "$work/payload" "$key"

artifact=EV6_KR_Wired_CarPlay_TBT_v0.1.6_EXPERIMENTAL.zip
python3 - "$work/payload" "$work/$artifact" <<'PY'
from pathlib import Path
import sys
import zipfile

root, output = map(Path, sys.argv[1:])
with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED) as z:
    for path in sorted(p for p in root.rglob("*") if p.is_file()):
        z.write(path, path.relative_to(root).as_posix())
with zipfile.ZipFile(output) as z:
    if z.testzip() is not None:
        raise SystemExit("Damaged ZIP entry")
    z.extractall(output.parent / "verified")
PY
java -cp "$repo/out/engine-host" "$signing" verify "$work/verified" "$uuid"
mv -- "$work/$artifact" "$base/$artifact"
sha256sum "$base/$artifact"
