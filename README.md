# SoulFlow - Wellness Tracking Mobile App

A holistic wellness application that helps you track and improve your mental health, habits, and physical wellness all in one place.

## Features

✨ **Mood Tracking** - Log and monitor your emotional states throughout the day

🎯 **Habit Management** - Create, track, and build positive habits over time

💧 **Hydration Tracking** - Monitor your water intake and stay hydrated

📊 **Data Analytics** - Visualize trends and insights from your tracked data

🔔 **Smart Notifications** - Get reminders for habits and hydration goals

🎨 **Dark Mode** - Comfortable viewing in any lighting condition

📱 **Home Dashboard** - View all your wellness metrics at a glance

🏠 **Home Screen Widget** - Quick access to tracked metrics

## Tech Stack

- **Kotlin** - Modern, safe programming language
- **Android Jetpack** - ViewModel, LiveData, WorkManager, Navigation
- **Material Design 3** - Beautiful and consistent UI
- **MPAndroidChart** - Powerful data visualization
- **Gradle** - Build automation and dependency management

## Requirements

- Android SDK 24 (minimum)
- Android 14 (target)
- Java 11+

## Getting Started

### Prerequisites

- Android Studio (latest version)
- Gradle 8.11+
- Android SDK Tools

### Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/SoulFlow-Mobile-Application.git
cd SoulFlow-Mobile-Application
```

2. Open the project in Android Studio

3. Build the project:
```bash
./gradlew build
```

4. Run on emulator or device:
```bash
./gradlew installDebug
```

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/SoulFlow/
│   │   │   ├── ui/              # UI screens (Home, Mood, Habits, Hydration, Settings)
│   │   │   ├── data/            # Data models and repositories
│   │   │   ├── widget/          # Home screen widget
│   │   │   ├── workers/         # Background tasks
│   │   │   └── sensors/         # Device sensor integration
│   │   └── res/                 # Resources (layouts, drawables, colors)
│   └── test/                    # Unit tests
└── build.gradle.kts             # Build configuration
```

## How to Use

1. **Open the App** - Launch SoulFlow on your Android device
2. **Set Up Profile** - Complete the onboarding process
3. **Track Mood** - Log your emotions daily
4. **Create Habits** - Set goals you want to achieve
5. **Log Hydration** - Track water intake throughout the day
6. **View Analytics** - Check trends and progress in the Home tab

## Testing

Run unit tests:
```bash
./gradlew test
```

Run UI tests:
```bash
./gradlew connectedAndroidTest
```
## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

If you encounter any issues or have suggestions, please create an issue on GitHub.

---

**Made with ❤️ for your wellness journey**
