# Test Report - Matale SafeRide

Test date: 24 August 2026

## Automated core-logic smoke test
Command used:

```bash
javac -d build_logic app/src/main/java/lk/matale/saferide/SchoolTransportState.java app/src/test/java/lk/matale/saferide/AppLogicSmokeTest.java
java -cp build_logic lk.matale.saferide.AppLogicSmokeTest
```

Result: **PASS**

Verified:
- Initial trip is inactive
- Four demo children are loaded
- Start Trip activates the trip and resets child states
- Pickup changes a child to PICKED_UP
- Drop-off changes a child to DROPPED_OFF
- Delay message is stored
- Emergency message is stored
- End Trip deactivates the trip
- AndroidManifest.xml and strings.xml are well-formed XML

## Build/runtime note
The source project is structured for Android Studio and intentionally uses only Android platform APIs (no third-party runtime dependencies). The current execution container did not include the Android SDK/Gradle toolchain, so an APK could not be compiled or launched in an Android emulator here. Open the included project in Android Studio to perform the final device/emulator build.
