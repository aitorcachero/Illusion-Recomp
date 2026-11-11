# Illusion of Time SNES Recomp (Native PC & Linux Port)

[![Build Linux Native Release](https://github.com/aitorcachero/Illusion-Recomp/actions/workflows/build-linux.yml/badge.svg)](https://github.com/aitorcachero/Illusion-Recomp/actions/workflows/build-linux.yml)
[![Release](https://img.shields.io/github/v/release/aitorcachero/Illusion-Recomp)](https://github.com/aitorcachero/Illusion-Recomp/releases)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)

*Read this in [Español (README_ES.md)](README_ES.md).*

A high-performance native PC (Windows 64-bit and Linux x86_64) and Android port of the Super Nintendo classic **Illusion of Time** (known in North America as *Illusion of Gaia*, official Spanish / ESP PAL release), built via static recompilation using the [snesrecomp](https://github.com/RetroPortingToolKit/snesrecomp) framework.

---

## 🌟 Key Features

* **🖥️ Native Resolution up to 4K UHD:** Razor-sharp rendering on modern desktop and TV displays with free or integer scaling.
* **📺 True 16:9 Widescreen FOV Expansion:** Broadens the horizontal camera view to native 16:9 without stretching or distorting character sprites.
* **🚀 Modern High-DPI Setup Launcher:**
  * Clean *Deep Navy & Vibrant Orange* design with DPI scaling relative to 1080p.
  * Instant cover art showcase.
  * Integrated ROM browser (`GetOpenFileName` on Windows / `zenity`/`kdialog` on Linux).
  * Quick Presets: **Modern (16:9 1080p+ 144Hz)**, **Steam Deck (1280x800)**, **Original 1994 (4:3 Retro)**, and **Custom**.
  * Dynamic Refresh Rate selection: **60 Hz**, **144 Hz**, **240 Hz**, **360 Hz**, and **Auto** (synced to your display).
* **🎨 Advanced Shaders:**
  * **HD-2D Remaster:** Subpixel smoothing and edge micro-contrast.
  * **CRT Soft & Warm Composite:** Authentic scanlines and vintage CRT phosphor warmth.
  * **xBR-lv2 & FXAA:** Clean anti-aliasing without blurring.
  * **Sharp HD:** Crisp pixel-perfect scaling.
* **🎮 Universal Gamepad Detection:** Hotplug plug & play support for Xbox, PlayStation (DualShock 4 / DualSense), Nintendo Switch Pro, and standard SDL3 controllers.
* **🔊 Pristine SNES SPC700 Audio:** Cycle-accurate S-DSP synthesis running at 48,000 Hz / 16-bit stereo with minimal buffer latency.
* **⏪ Real-time Rewind & Quick Save:** Rewind up to 60 seconds with F12 and save/load instantly across slots F1-F10.
* **📱 Multiplatform:** Android phone and Android TV builds available.

---

## ⚠️ Legal Notice

> **You must legally own an original physical cartridge or ROM copy of the game.**  
> This repository contains no copyrighted game ROMs, proprietary binaries, or assets owned by Nintendo or Enix / Quintet. The executable loads your own legally acquired dump of `Illusion of Time (ESP).sfc`.

### Compatible ROM Information

| Property | Value |
| :--- | :--- |
| **Title** | Illusion of Time |
| **Region** | Spain / Spanish (ESP) |
| **Filename** | `Illusion of Time (ESP).sfc` (or `.smc`) |
| **Memory Map** | HiROM |
| **CRC32** | `7c305dc8` |
| **SHA-256** | `7b98fd832f3012d902f7c312ca542f84385c6f0e883b04205cbecd769cf3e4b1` |

---

## 🕹️ Quick Start

### Windows:
1. Download `Illusion_of_Time_Recompiled_v1.0_Windows_x64.zip` from [Releases](https://github.com/aitorcachero/Illusion-Recomp/releases).
2. Extract the archive into any directory.
3. Place your `Illusion of Time (ESP).sfc` ROM in the game folder (or click *Browse* in the launcher).
4. Double-click **`JUGAR.bat`** (or launch `IllusionOfTimeSNESRecomp.exe`).

### Linux:
1. Download `Illusion_of_Time_Recompiled_v1.0_Linux_x64.tar.gz` from [Releases](https://github.com/aitorcachero/Illusion-Recomp/releases).
2. Extract the archive:
   ```bash
   tar -xzf Illusion_of_Time_Recompiled_v1.0_Linux_x64.tar.gz
   cd Illusion_of_Time_Linux_x64
   ```
3. Copy your `Illusion of Time (ESP).sfc` ROM into the folder.
4. Launch the game:
   ```bash
   chmod +x JUGAR.sh IllusionOfTimeSNESRecomp
   ./JUGAR.sh
   ```

---

## ⌨️ Controls

| In-Game Action | Keyboard | Gamepad (Xbox / PlayStation / Switch) |
| :--- | :--- | :--- |
| **Move** | Arrows / W A S D | D-Pad / Left Stick |
| **Attack / Run** | Z / Space | A (Cross / B) |
| **Flute / Talk** | X | B (Circle / A) |
| **Telekinesis Shield** | C / V | LB / RB (L / R) |
| **Inventory / Menu** | Enter | Start / Options |
| **Transform** | Shift / S | Y / X |
| **Toggle Fullscreen** | Alt + Enter / F11 | Guide / Touchpad |
| **Rewind** | F12 | L3 + R3 |
| **Quit Game** | Escape | Back / Select |

---

## 🛠️ Building from Source

```bash
git clone --recursive https://github.com/aitorcachero/Illusion-Recomp.git
cd Illusion-Recomp
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build --config Release -j
```
