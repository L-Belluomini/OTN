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

The recorded R8 fixture version is from build-tools 36.1.0. The Android release uses AGP's R8 pipeline separately. No device install, navigation session or phone settings were changed in this validation. Official signing, device runtime for these exact APKs and the remaining qualification limits in [PORTING.md](PORTING.md) are open.
