# ATAK CIV 5.8 development port

This branch ports upstream OTN `9f35f1ff99eb01b892df412c5c86701bfd248e9e` to ATAK CIV 5.8.0. It retains the GPL-3.0 license and GraphHopper 6 graph format. Android API 26 or later is required. This is a development-host port; official ATAK release signing and production qualification remain open.

## Build

Use JDK 17 or later, Android SDK platform 36, and the ATAK CIV 5.8 SDK. The wrapper uses Gradle 8.14.3 and Android Gradle Plugin 8.13.0. Copy `template.local.properties` to ignored `local.properties` and replace the paths. SDK binaries, keys, APKs and downloaded road extracts are not committed.

```sh
./gradlew :app:testCivDebugUnitTest :app:assembleCivDebug :app:assembleCivRelease :app:lintCivDebug
./gradlew -p tools/graph-fixture test run verifyAndroidRuntimeRouting writeRuntimeClasspath
```

The local SDK's `android_keystore` and its standard development alias/password sign both development variants. The release variant exercises R8 obfuscation and packaging; it is not an official release artifact. Use the matching SDK host to load these APKs. The SDK's TAK Gradle plugin requires `sdk.path` as well as this project's `atak.sdk.path`.

## Port changes

- Replace legacy lifecycle/tool extensions with `IPlugin` and the host toolbar service, with current Android discovery metadata and unload cleanup.
- Implement `RoutePlannerInterface2`; retain planner ID `OTNOFFlineFast`. ATAK 5.8's `getPlannerByName` uses the descriptive name. For an ID lookup, iterate `getRoutePlanners2()` and compare `getUniqueIdenfier()` (the SDK spelling).
- Preserve every geometry point and assign valid waypoint coordinates. Associate cues with waypoint UIDs using GraphHopper instruction intervals, including zero-length via/finish instructions.
- Copy request points and graph configuration to avoid mutating caller settings or accumulating endpoints. Check graph loads and close routing resources on completion, error and observed cancellation.
- Use GraphHopper 6's `block_area` weighting for avoidance, with explicit errors for unsupported regions.
- Repair metadata reads after close, directory creation, malformed graph handling, profile bounds, locale parsing and registration/listener cleanup.
- Use Maven dependencies without modified Jackson jars. Exclude unused desktop XML/elevation import dependencies from Android packaging. Preserve nested Gson graph settings through release obfuscation and retain dependency licenses/notices.

## Validation

See [VALIDATION.md](VALIDATION.md) for checks performed in this standalone checkout. Desktop tests import a fictional 6x6 grid near 0,0 and use real GraphHopper routing. The grid must never be used for navigation.

Earlier development-host testing in the recovered project exercised OTN route generation on Samsung Android 13 and Motorola Android 14 running ATAK 5.8.0.3 SDK, including stationary Android Auto integration. Those historical results do not qualify this branch's fresh APKs. This checkout's validation does not install APKs or interrupt devices used by other work.

## Optional device probe

The debug build contains an opt-in generation probe; release contains an inert implementation. Generate the fixture with `./gradlew -p tools/graph-fixture run`. On an idle test host, copy `tools/graph-fixture/build/fixture/graph/` to `atak/tools/OTN/graphs/Codex-Test-Grid`, add an empty `RUN_DEVICE_PROBE` file inside that directory, and load OTN. If other graphs exist, select this graph explicitly. The consumed flag enables one probe. Read `device-probe.json`, remove the synthetic graph, and restart the test host afterward.

The probe calls the registered shared planner off the UI thread and checks coordinates, cue references, repeated via requests and out-of-graph rejection. It does not add a map route, start navigation, change GPS or send CoT. Real navigation requires compatible GraphHopper 6 data prepared with OTN Companion.

## Limits

Large graph catalogs, custom profiles, native avoidance UI, cancellation during a real calculation, automatic rerouting, driven guidance, audio, earlier ATAK hosts and official release signing remain unqualified. Graph scanning/hashing still runs on the host UI thread.

`isRerouteCapable()` means ATAK can request replacement route generation. OTN does not implement an off-route navigation controller; this flag does not establish automatic rerouting. The optional road-import tools accept caller-provided OSM XML and metadata; no production navigation data is distributed here.
