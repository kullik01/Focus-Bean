<p align="center">
  <img src="https://img.shields.io/badge/Windows-0078D6?style=for-the-badge&logo=windows&logoColor=white" alt="Windows"/>
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/JavaFX-21.0.6-blue?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX 21"/>
  <img src="https://img.shields.io/badge/Gradle-Kotlin_DSL-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle"/>
  <img src="https://img.shields.io/badge/License-BSD_3--Clause-green?style=for-the-badge" alt="License"/>
</p>

<h1 align="center">Focus Bean</h1>

<p align="center">
  <strong>A modern, elegant timer application designed for deep work and productivity.</strong>
</p>

<p align="center">
  Built with JavaFX and featuring a warm, coffee-themed design.
</p>

---

## ✨ Features
### ⏲️ **Timer**
- Configurable work and break session durations (1–900 minutes)
- Visual countdown timer with modern circular display
- Pause, resume, skip, and reset functionality
- Automatic session transitions with notifications
- Keyboard shortcuts for quick control

### 📊 **Daily Progress Tracking**
- Circular progress indicator showing daily goal completion
- Yesterday's performance comparison
- Current streak tracking
- Real-time progress updates

### 📜 **Session History**
- Complete log of work and break sessions
- Toggle between **table view** and **bar chart view**
- Configurable chart display period (1–365 days)
- Daily and weekly statistics summary
- Clear history functionality with confirmation dialog
- Persistent data storage across application restarts

### 🔔 **Notifications**
- **Sound notifications** with multiple built-in sounds
- **Custom sound support** – use your own audio files (WAV, MP3)
- Sound preview directly in settings
- **System tray popup notifications** for session completion
- Independently configurable sound and popup settings

### 🖥️ **Modern UI Design**
- Custom title bar with minimize/close controls
- Clean, card-based layout with rounded corners
- Warm, coffee-themed color palette
- Custom application icon support
- Consistent styling throughout the application

## 🚀 Getting Started
### Prerequisites

- **Java 21** or later (JDK)
- **Gradle 8.x** (wrapper included)

### Installation

1. **Clone the repository:**

   ```bash
   git clone https://github.com/kullik01/Focus-Bean.git
   cd Focus-Bean
   ```

2. **Build the project:**

   ```bash
   ./gradlew build
   ```

3. **Run the application:**

   ```bash
   ./gradlew run
   ```

### Building a Distributable Package

To create a standalone distribution with a native launcher:

```bash
./gradlew jlink
```

The output will be located in `build/distributions/FocusBean-{version}.zip`.

---

## ⌨️ Keyboard Shortcuts

| Shortcut       | Action                                |
|----------------|---------------------------------------|
| `Space`        | Start / Pause / Resume timer          |
| `R`            | Reset timer                           |
| `S`            | Skip current session                  |

---

## 🏗️ Architecture

Focus Bean follows a clean **MVC (Model-View-Controller)** architecture:

```
src/main/java/io/github/kullik01/focusbean/
├── controller/     # Application logic and event handling
├── model/          # Data models (UserSettings, TimerSession, SessionHistory)
├── service/        # Core services (TimerService, PersistenceService, NotificationService)
├── util/           # Utilities and constants
└── view/           # JavaFX UI components
```

### Key Components

| Component             | Responsibility                                            |
|-----------------------|-----------------------------------------------------------|
| `TimerController`     | Coordinates timer logic, state transitions, and data flow |
| `TimerService`        | Manages the countdown using JavaFX Timeline               |
| `PersistenceService`  | Handles JSON-based data storage to local filesystem       |
| `NotificationService` | Manages sound and system tray notifications               |
| `MainView`            | Assembles UI components into the main window              |
| `CustomTitleBar`      | Provides custom window chrome with minimize/close buttons |
| `DailyProgressView`   | Displays circular progress and daily stats                |
| `HistoryView`         | Shows session history table/chart with statistics         |
| `SettingsView`        | Provides embedded settings panel                          |

---

## 🧪 Testing

Run the test suite with:

```bash
./gradlew test
```

The project uses **JUnit 5** for unit testing.

---

## 📦 Dependencies

| Dependency         | Version | Purpose                            |
|--------------------|---------|----------------------------------- |
| JavaFX Controls    | 21.0.6  | UI components                      |
| JavaFX Graphics    | 21.0.6  | Rendering and animation            |
| JavaFX Media       | 21.0.6  | Audio playback for notifications   |
| Gson               | 2.11.0  | JSON serialization/deserialization |
| JUnit Jupiter      | 5.10.2  | Unit testing framework             |

---

## 🗂️ Data Storage

Focus Bean stores user data in the system's application data directory:

- **Windows:** `%APPDATA%/FocusBean/`

Files stored:
- `session_history.json` – Session logs and user settings

---

## 🎨 Custom Logo

You can use your own application logo by placing a `logo.png` file in:
- `src/main/resources/io/github/kullik01/focusbean/view/`

The logo will be used for both the taskbar icon and the custom title bar. If no custom logo is found, the application uses a default coffee bean icon.

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the **BSD 3-Clause License** – see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgements
- Built with [JavaFX](https://openjfx.io/)
- JSON serialization by [Gson](https://github.com/google/gson)

---

<p align="center">
  Made with ❤️ and ☕ for productivity enthusiasts.
</p>
