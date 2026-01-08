<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/JavaFX-21.0.6-blue?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX 21"/>
  <img src="https://img.shields.io/badge/Gradle-Kotlin_DSL-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle"/>
  <img src="https://img.shields.io/badge/License-BSD_3--Clause-green?style=for-the-badge" alt="License"/>
</p>

<h1 align="center">☕ Focus Bean</h1>

<p align="center">
  <strong>A modern, elegant Pomodoro timer application designed for deep work and productivity.</strong>
</p>

<p align="center">
  Built with JavaFX and inspired by the sleek design of Windows Clock Focus Sessions.
</p>

---

## ✨ Features

### 🎯 **Pomodoro Timer**
- Configurable work and break session durations
- Visual countdown timer with modern circular display
- Pause, resume, skip, and reset functionality
- Keyboard shortcuts for quick control

### 📊 **Daily Progress Tracking**
- Circular progress indicator showing daily goal completion
- Yesterday's performance comparison
- Current streak tracking
- Real-time progress updates

### 📜 **Session History**
- Complete log of work and break sessions
- Daily and weekly statistics summary
- Session status tracking (completed vs. skipped)
- Persistent data storage across application restarts

### ⚙️ **Customizable Settings**
- Adjustable work duration (1–120 minutes)
- Adjustable break duration (1–60 minutes)
- Configurable daily focus goal (1–480 minutes)
- Settings persisted locally

### 🖥️ **Modern UI Design**
- Clean, card-based layout
- Light theme with subtle shadows
- Responsive window sizing
- Windows Clock-inspired aesthetics

---

## 🖼️ Screenshots

> *Coming soon – screenshots showcasing the main timer view, daily progress, and history panel.*

---

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
| `Ctrl + ,`     | Open settings                         |

---

## 🏗️ Architecture

Focus Bean follows a clean **MVC (Model-View-Controller)** architecture:

```
src/main/java/io/github/kullik01/focusbean/
├── controller/     # Application logic and event handling
├── model/          # Data models (UserSettings, TimerSession, SessionHistory)
├── service/        # Core services (TimerService, PersistenceService)
├── util/           # Utilities and constants
└── view/           # JavaFX UI components
```

### Key Components

| Component             | Responsibility                                            |
|-----------------------|-----------------------------------------------------------|
| `TimerController`     | Coordinates timer logic, state transitions, and data flow |
| `TimerService`        | Manages the countdown using JavaFX Timeline               |
| `PersistenceService`  | Handles JSON-based data storage to local filesystem       |
| `MainView`            | Assembles UI components into the main window              |
| `DailyProgressView`   | Displays circular progress and daily stats                |
| `HistoryView`         | Shows session history table with statistics               |

---

## 🧪 Testing

Run the test suite with:

```bash
./gradlew test
```

The project uses **JUnit 5** for unit testing.

---

## 📦 Dependencies

| Dependency         | Version | Purpose                          |
|--------------------|---------|----------------------------------|
| JavaFX Controls    | 21.0.6  | UI components                    |
| JavaFX Graphics    | 21.0.6  | Rendering and animation          |
| Gson               | 2.11.0  | JSON serialization/deserialization |
| JUnit Jupiter      | 5.10.2  | Unit testing framework           |

---

## 🗂️ Data Storage

Focus Bean stores user data in the system's application data directory:

- **Windows:** `%APPDATA%/FocusBean/`
- **macOS/Linux:** `~/.focusbean/` *(platform-dependent)*

Files stored:
- `session_history.json` – Session logs and user settings

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

## 👤 Author

**Hannah Kullik**  
GitHub: [@kullik01](https://github.com/kullik01)

---

## 🙏 Acknowledgements

- Inspired by the [Windows Clock Focus Sessions](https://support.microsoft.com/en-us/windows/how-to-use-focus-in-windows-11-cbcc9ddb-8164-43fa-8919-b9a2af072382)
- Built with [JavaFX](https://openjfx.io/)
- Icons and design elements follow the [Fluent Design System](https://fluent2.microsoft.design/)

---

<p align="center">
  Made with ❤️ and ☕ for productivity enthusiasts
</p>