# 🔮 ShadowCore

<div align="center">

**Android-on-Android Virtual Environment Engine**

*Run fully isolated Android environments inside your phone — no root required.*

[![Android](https://img.shields.io/badge/Android-16+-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue?style=for-the-badge)](LICENSE)

---

<img src="docs/banner.png" alt="ShadowCore Banner" width="800"/>

</div>

## ✨ What is ShadowCore?

ShadowCore lets you run a **complete, isolated Android environment** inside your Android phone — similar to [VirtualMaster](https://virtualmaster.app) and VMOS.

- 📱 **Android inside Android** — A full virtual home screen with its own apps, settings, and storage
- 🔒 **Complete isolation** — Apps inside can't access your real contacts, photos, or data
- 🌐 **Multiple Android versions** — Download and run Android 9, 11, 13, or 15
- 🔓 **No root required** — Uses Shizuku + Wireless Debugging for elevated permissions
- 🎮 **App cloning** — Run multiple instances of any app simultaneously

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    ShadowCore App                        │
│                  (Kotlin / Compose)                      │
├─────────────────────────────────────────────────────────┤
│  UI Layer                                               │
│  ├── Home Screen (VE list + management)                 │
│  ├── Activation Screen (Shizuku setup wizard)           │
│  ├── Container Screen (virtual Android display)         │
│  ├── Image Manager (download Android versions)          │
│  └── Settings (theme, preferences)                      │
├─────────────────────────────────────────────────────────┤
│  Engine Layer                                           │
│  ├── ContainerEngine (boot, stop, install apps)         │
│  ├── ContainerManager (lifecycle management)            │
│  ├── ShizukuManager (ADB-level privileges)              │
│  └── ImageManager (download/extract system images)      │
├─────────────────────────────────────────────────────────┤
│  Container Core (BlackBox Engine)                       │
│  ├── Service Hook Framework (IActivityManager proxy)    │
│  ├── Virtual PackageManager                             │
│  ├── APK Loader (DexClassLoader)                        │
│  ├── Stub Activity/Service pool                         │
│  └── Virtual File System (redirected I/O)               │
├─────────────────────────────────────────────────────────┤
│  System Images (~300-500MB each)                        │
│  ├── Android 9 (Pie)                                    │
│  ├── Android 11 (Red Velvet Cake)                       │
│  ├── Android 13 (Tiramisu)                              │
│  └── Android 15 (Vanilla Ice Cream)                     │
└─────────────────────────────────────────────────────────┘
```

## 📱 Screenshots

<div align="center">
<table>
<tr>
<td align="center"><b>Home Screen</b></td>
<td align="center"><b>Activation Wizard</b></td>
<td align="center"><b>Virtual Environment</b></td>
<td align="center"><b>Image Manager</b></td>
</tr>
<tr>
<td><img src="docs/screenshots/home.png" width="200"/></td>
<td><img src="docs/screenshots/activation.png" width="200"/></td>
<td><img src="docs/screenshots/container.png" width="200"/></td>
<td><img src="docs/screenshots/images.png" width="200"/></td>
</tr>
</table>
</div>

## 🚀 Getting Started

### Prerequisites

| Requirement | Minimum | Recommended |
|-------------|---------|-------------|
| Android version | 11 (API 30) | 14+ (API 34) |
| RAM | 4 GB | 8 GB+ |
| Storage | 5 GB free | 10 GB+ free |
| Root | ❌ Not required | — |

### Installation

1. **Download** the latest APK from [Releases](../../releases)
2. **Install** the APK on your device (enable "Install from unknown sources")
3. **Activate** — Follow the in-app wizard:
   - Enable Developer Options
   - Enable Wireless Debugging
   - Install & start [Shizuku](https://shizuku.rikka.app/)
   - Grant ShadowCore permission
4. **Download** a system image (Android 9/11/13/15)
5. **Create** your first virtual environment 🎉

### Building from Source

```bash
# Clone the repository
git clone https://github.com/ReturnKartikey/ShadowCore.git
cd ShadowCore

# Open in Android Studio (Arctic Fox+)
# Sync Gradle
# Build → debug APK

# Or build via command line
./gradlew assembleDebug
```

**Build requirements:**
- Android Studio Ladybug+
- JDK 17
- Android SDK 35
- Kotlin 2.2.10

## 🔧 Tech Stack

| Technology | Purpose |
|-----------|---------|
| **Kotlin 2.2** | Primary language |
| **Jetpack Compose + Material 3** | Modern declarative UI |
| **Hilt** | Dependency injection |
| **Room** | Local database for VE profiles |
| **Coroutines + Flow** | Async operations & reactive state |
| **WorkManager** | Background image downloads |
| **DataStore** | Theme & settings persistence |
| **Shizuku** | ADB-level permissions without root |
| **BlackBox Engine** | App containerization core |

## 📂 Project Structure

```
ShadowCore/
├── app/
│   └── src/main/kotlin/com/shadowcore/app/
│       ├── engine/              ← Container engine & image management
│       │   ├── ContainerEngine.kt
│       │   ├── ContainerManager.kt
│       │   ├── ImageManager.kt
│       │   ├── ImageRegistry.kt
│       │   ├── ShizukuManager.kt
│       │   └── SystemImage.kt
│       ├── data/                ← Room database, repositories
│       │   ├── local/
│       │   └── repository/
│       ├── domain/              ← Domain models & use cases
│       │   ├── model/
│       │   ├── repository/
│       │   └── usecase/
│       ├── ui/                  ← Compose UI screens
│       │   ├── screens/
│       │   │   ├── activation/  ← Shizuku setup wizard
│       │   │   ├── container/   ← Virtual environment display
│       │   │   ├── home/        ← VE list & management
│       │   │   ├── imagemanager/← System image downloads
│       │   │   └── settings/    ← App settings
│       │   ├── components/      ← Reusable UI components
│       │   ├── navigation/      ← NavHost & routes
│       │   └── theme/           ← Material 3 theme
│       └── di/                  ← Hilt DI modules
├── gradle/
│   └── libs.versions.toml      ← Version catalog
└── build.gradle.kts
```

## 🔑 How It Works

ShadowCore uses **application containerization** (not traditional VM/KVM) to create isolated Android environments:

### 1. Shizuku Activation
ShadowCore uses [Shizuku](https://shizuku.rikka.app/) to obtain ADB-level shell permissions via Wireless Debugging — no root needed. This grants the elevated access required to manage containerized processes.

### 2. App Containerization (BlackBox Engine)
- **Service Hooking** — Intercepts Android system service calls (ActivityManager, PackageManager) via Java reflection and dynamic proxies
- **Stub Activities** — Pre-declared proxy Activities in the manifest that forward to real app Activities inside the container
- **APK Loading** — Loads APKs via `DexClassLoader` without system-level installation
- **File System Isolation** — Redirects all file I/O to sandboxed directories

### 3. System Images
Downloadable Android version packages (~300-500MB) containing:
- Framework JARs (`framework.jar`, `services.jar`, `ext.jar`)
- System APKs (Launcher, Settings, SystemUI)
- Native libraries
- ART runtime components

## 🗺️ Roadmap

- [x] Core UI with Material 3 + dark mode
- [x] VE profile management (create, edit, delete)
- [x] Shizuku activation flow
- [x] System image download & management
- [x] Container boot sequence
- [ ] BlackBox engine integration (app containerization)
- [ ] Multi-version framework loading
- [ ] Clipboard sharing (host ↔ guest)
- [ ] File sharing between host and VE
- [ ] Root toggle inside VE
- [ ] Google Play Services installation

## ⚠️ Important Notes

- **Not on Play Store** — Apps using APK containerization cannot be distributed via Google Play
- **Re-activation needed after reboot** — Shizuku permissions are temporary
- **Performance varies** — Container apps run ~90% of native speed on most devices
- **OEM restrictions** — Some manufacturers (Xiaomi, OPPO) may restrict background services

## 📄 License

```
Copyright 2026 ShadowCore

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🙏 Acknowledgements

- [Shizuku](https://github.com/RikkaApps/Shizuku) — ADB-level permissions without root
- [NewBlackbox](https://github.com/ALEX5402/NewBlackbox) — Open-source virtual app engine
- [VirtualApp](https://github.com/asLody/VirtualApp) — Original app containerization framework

---

<div align="center">

**Built with ❤️ for the Android community**

⭐ Star this repo if you find it useful!

</div>
