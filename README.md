# WeatherSnap

WeatherSnap is an Android intern assignment app built with Kotlin, Jetpack Compose, MVVM, StateFlow, Coroutines, Hilt, Navigation Compose, Retrofit, Room, CameraX, Coil, and Material 3.

## Features

- Search city suggestions from Open-Meteo geocoding after more than 2 letters.
- Cache city suggestions in memory to avoid repeat API calls for the same query.
- Fetch live current weather from Open-Meteo forecast API.
- Show loading, empty, success, and error UI states.
- Create a weather report from the selected weather snapshot.
- Capture a report photo in a custom CameraX screen.
- Compress the captured image and show original/compressed sizes.
- Save notes, weather data, image path, sizes, and timestamp in Room.
- View saved reports with an empty state.

## Run

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Run the app module on a device or emulator with camera support.

Command-line debug build:

```bash
./gradlew :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Recording Flow Checklist

1. Type a city name to show autocomplete suggestions.
2. Select a city to load weather.
3. Tap Create Report.
4. Tap Capture Photo and use the Custom Camera screen.
5. Confirm original and compressed image sizes are visible.
6. Enter notes.
7. Save the report.
8. Verify the saved reports list shows the report.
