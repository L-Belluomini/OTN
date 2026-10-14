# Standalone port validation

Validated October 14, 2026 against source commit `818815672e7ff60850f8c0afa326fe4819bd84ae`. Later documentation-only commits do not change the built source. Machine-readable results and APK SHA-256 hashes are in [validation/desktop-sdk58.json](validation/desktop-sdk58.json). APKs remain local and ignored.

- JBR 21.0.8, ATAK CIV 5.8.0.5 SDK (API 5.8.0), Android compile SDK 36, Gradle 8.14.3, AGP 8.13.0.
- `./gradlew --offline --no-daemon :app:testCivDebugUnitTest :app:assembleCivDebug :app:assembleCivRelease :app:lintCivDebug`: passed. The inherited Android unit test is a sample; routing behavior is checked separately below. Lint: zero errors, 118 warnings. Gradle 9 deprecation warnings remain.
- `./gradlew --offline --no-daemon -p tools/graph-fixture test run verifyAndroidRuntimeRouting writeRuntimeClasspath`: passed. Five tests cover full geometry/cue conversion, via instructions, avoidance, malformed instructions and out-of-graph rejection. The dependency-subset runner loads the fixture in mmap mode with desktop XML/elevation dependencies absent and checks repeated routing, via points and cues.
- R8/Gson configuration contract: the legacy policy loses nested fields and fails at runtime; the fixed policy preserves configuration and passes. This JVM test does not qualify Android engine execution.
- Both APK signatures verified with SDK build-tools `apksigner`; all packaged notice assets match their source bytes. The debug opt-in probe flag is present in debug DEX and absent from release DEX.
- `git diff --check` passed, with verbatim third-party license whitespace preserved via `.gitattributes`.

To repeat the R8 regression after `writeRuntimeClasspath`, run:

```sh
python3 tools/verify-release-config.py \
  --java-home "$JAVA_HOME" \
  --r8-jar "$ANDROID_HOME/build-tools/36.1.0/lib/d8.jar" \
  --output /tmp/otn-release-config
```

The recorded R8 fixture version is from build-tools 36.1.0. The Android release uses AGP's R8 pipeline separately. The desktop checks above did not modify a phone. Subsequent physical-device results are recorded below.


## Physical-device release check

On October 14, 2026, the exact obfuscated `81881567` release APK above was installed with `adb install -r` on a Motorola moto g play (2024), Android 14, running ATAK CIV 5.8.0.3 SDK. The installed APK hash matches the desktop artifact. The existing host and bridge APKs were retained. See [sanitized device results](validation/motorola-release-sdk58.json).

The updated OTN plugin loaded and registered `OTNOFFlineFast`. An existing external Android Auto debug bridge harness called the production OTN planner on a temporary real-road GraphHopper 6 graph, then exercised actual ATAK route preview, explicit Start, consumer disconnect/reconnect and Stop. The roughly 2.57 km route had 32 geometry points and seven valid cue associations. Preview left navigation idle; Start activated the route, text guidance was observed, reconnect retained the same session, and Stop returned to idle.

This was a stationary command-level test. The observed active state was `OFF_ROUTE`; this result does not establish driven guidance, automatic rerouting, audio, native car-template UI or vehicle compatibility. The harness is external to this standalone OTN repository; its installed APK hash is recorded for identification. Its raw report remains private, with only a digest and sanitized checks committed here. OTN's release APK has no active device probe.

The generated route and temporary graph were removed, original routes were retained, and the original OTN graph preference was restored while preserving unrelated preferences. After restart, ATAK was foreground and idle, with the car consumer released. The tested OTN release remains installed, and the preceding OTN APK is backed up locally. The Samsung and the other chat's checkout were not modified.

Official signing, large graphs, earlier hosts and the other qualification limits in [PORTING.md](PORTING.md) remain open. The exact debug APK was not installed in this run.
