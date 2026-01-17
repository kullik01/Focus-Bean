<p align="center">

[![Windows](https://img.shields.io/badge/Windows-0881d9?style=for-the-badge&logo=windows&logoColor=white)](https://www.microsoft.com/en-us/windows)
[![Linux](https://img.shields.io/badge/Linux-0f5689?style=for-the-badge&logo=linux&logoColor=white)](https://www.linux.org/pages/download/)
[![Java 25](https://img.shields.io/badge/Java-25-f29111?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JavaFX 25](https://img.shields.io/badge/JavaFX-25-4aa2c9?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![Gradle](https://img.shields.io/badge/Gradle-Kotlin_DSL-3f297f?style=for-the-badge&logo=gradle&logoColor=white)](https://docs.gradle.org/current/kotlin-dsl/)
[![License](https://img.shields.io/badge/License-BSD_3--Clause-green?style=for-the-badge)](https://opensource.org/license/bsd-3-clause)
[![Release](https://img.shields.io/badge/Release-v1.0.1-602718?style=for-the-badge&color=602718&logo=github)](https://github.com/kullik01/Focus-Bean/releases)

</p>


<p align="center">
  <img src="src/main/resources/io/github/kullik01/focusbean/view/FocusBean_full_shadow.png" alt="Focus Bean Logo" width="200"/>
</p>

<p align="center">
  <strong>A modern, elegant timer application designed for deep work and productivity.</strong>
</p>

<p align="center">
  Built with JavaFX and featuring a warm, coffee-themed design.
</p>

---
## Table of Contents
- [⬇️ Installation](#-installation)
  - [Windows](#windows)
  - [Linux](#linux)
- [✨ Features](#-features)
- [🏗️ Build from Source (Optional)](#-build-from-source-optional)
- [📦 Dependencies](#-dependencies)
- [🙏 Acknowledgements](#-acknowledgements)
- [📝 License](#-license)

---

## ⬇️ Installation

### Windows
1. **Download**: Get `FocusBean-{version}-Windows.zip` from the **[Releases](../../releases)** page.
2. **Setup**: Extract the file to your desired location.
3. **Run**: Double-click `FocusBean.exe`.

### Linux
*Focus Bean allows for a clean, user-local installation in your home directory.*

#### Runtime Requirements
Before running the application, ensure you have the standard JavaFX dependencies installed (GTK3, ALSA, GStreamer). Most desktop systems have these, but you can verify:

**AlmaLinux / RHEL:**
```bash
sudo dnf install -y gtk3 alsa-lib gstreamer1-plugins-base gstreamer1
```

**openSUSE:**
```bash
sudo zypper install -y libgtk-3-0 libasound2 gstreamer-plugins-base gstreamer-plugins-good
```

#### Install & Run
1. **Download**: Get `FocusBean-{version}-Linux.zip` from the **[Releases](../../releases)** page.
2. **Install**: Open a terminal and run the following to install to `~/.focusbean`:
   ```bash
   mkdir -p ~/.focusbean
   unzip FocusBean-{version}-Linux.zip -d ~/.focusbean
   ```
3. **Run**:
   ```bash
   ~/.focusbean/focusbean-{version}/bin/FocusBean
   ```

---

## ✨ Features

- **⏲️ Smart Timer**: Configurable work (1-900 min) and break sessions with visual circular countdown.
- **📊 Progress Tracking**: Daily goal tracking, streak monitor, and "yesterday vs. today" comparison.
- **📜 Session History**: Detailed logs of all work sessions featuring both table and chart views.
- **🔔 Notifications**: Custom sound support (MP3/WAV) and system tray alerts for session transitions.
- **🖥️ Modern UI**: Clean, coffee-themed design with custom window controls.
- **⌨️ Shortcuts**: Space (Start/Pause), R (Reset), S (Settings).
- **🗂️ Local Data**: All data is stored locally:
  - Windows: `%APPDATA%/FocusBean/`
  - Linux: `~/.local/share/FocusBean/`

---

### 🏗️ Build from Source (Optional)

If you prefer to build the application yourself:

**Prerequisites**: Java 25 JDK, Gradle 9.1.

```bash
git clone https://github.com/kullik01/Focus-Bean.git
cd Focus-Bean
./gradlew run
```

---

## 📦 Dependencies

Focus Bean is built with the following technologies:

| Dependency         | Version | Purpose                            |
|--------------------|---------|----------------------------------- |
| **JavaFX**         | 25      | UI components and core graphics    |
| **Gson**           | 2.11.0  | JSON serialization for user data   |
| **JUnit 5**        | 5.10.2  | Unit testing framework             |

*Note: The application bundle includes the necessary Java runtime, so you do **not** need to install Java globally.*

---

## 🙏 Acknowledgements

- Built with [JavaFX](https://openjfx.io/)
- JSON serialization by [Gson](https://github.com/google/gson)

---

## 📝 License
**BSD 3-Clause License** – see [LICENSE](LICENSE).

---
<p align="center">Made with ❤️ and ☕ for productivity.</p>
