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

Screenshots 📸
<img width="576" height="1280" alt="Home1" src="https://github.com/user-attachments/assets/0f59c1bd-97a9-427b-896d-353153016da4" />
Home
<img width="576" height="1280" alt="Location_Search" src="https://github.com/user-attachments/assets/e91bb8bc-99d8-4ed8-ab21-5dc5c4a0e21f" />
Location Search
<img width="576" height="1280" alt="Result" src="https://github.com/user-attachments/assets/06a100b6-84c2-47d0-8aae-0cdb37ba4ccf" />
Result
<img width="576" height="1280" alt="Create_report" src="https://github.com/user-attachments/assets/17df0fda-1734-42d6-a8f2-ee8290770de4" />
Create Report
<img width="576" height="1280" alt="Report" src="https://github.com/user-attachments/assets/de120d44-6512-4531-8a2b-bda0353be183" />
Report
<img width="576" height="1280" alt="Saved Report" src="https://github.com/user-attachments/assets/44bf14d6-9efa-4861-bddf-fa3ec08a314c" />
Saved Report




