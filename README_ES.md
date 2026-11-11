# Illusion of Time SNES Recomp (Port Nativo para PC & Linux)

Port nativo para Windows 64-bit y Linux x86_64 del clásico de Super Nintendo **Illusion of Time** (conocido en América como *Illusion of Gaia*, versión oficial en castellano / ESP), desarrollado mediante técnicas de ingeniería inversa y recompilación estática sobre la base de [snesrecomp](https://github.com/RetroPortingToolKit/snesrecomp).

---

## 🎮 Características Principales

* **🖥️ Resolución 1080p, 2K y 4K UHD:** Ejecución nativa para pantallas y monitores modernos con nitidez absoluta y escalado entero o libre.
* **📺 Modo 16:9 Panorámico Nativo (FOV Hack):** Campo de visión horizontal extendido a 16:9 widescreen real sin estirar la imagen ni deformar los sprites de Will, Freedan o Shadow.
* **🚀 Pantalla de Arranque Moderna (High-DPI Launcher):**
  * Estilo visual *Sega Rally Revo / Deep Navy & Vibrant Orange* con escalado adaptativo a la resolución del monitor.
  * Selector visual de carátula original en alta resolución.
  * Detección automática de ROMs con explorador de archivos nativo de Windows y Linux.
  * Tarjetas de Presets: **Moderna (16:9 1080p+ 144Hz)**, **Steam Deck (1280x800)**, **Original 1994 (4:3 Retro)** y **Personalizado**.
  * Selector de tasa de refresco: **60 Hz**, **144 Hz**, **240 Hz**, **360 Hz** y **Auto** (sincronización exacta con los Hz nativos de tu monitor).
* **🎨 Shaders y Filtros Gráficos:**
  * **HD-2D Remaster:** Suavizado subpíxel de bordes y micro-contraste.
  * **CRT Soft & Warm Composite:** Líneas de escaneado y calidez de televisión clásica de tubo.
  * **xBR-lv2 & FXAA:** Redondeo de bordes sin emborronar.
  * **Sharp HD:** Escalado píxel-perfect nítido.
  * **Smooth Bicúbico:** Interpolación suave Catmull-Rom.
* **🎮 Compatibilidad Total con Mandos:** Detección plug & play en caliente de mandos de Xbox Series/One/360, PlayStation 4/5 (DualShock 4 / DualSense), Nintendo Switch Pro y gamepads genéricos SDL3.
* **🔊 Audio Nativo SNES SPC700 / S-DSP:** Síntesis y mezcla de sonido nativa x64 a 48.000 Hz / 16-bit estéreo de latencia ultrabaja.
* **⏪ Rebobinado en Tiempo Real y Guardado Rápido:** Rebobina al vuelo hasta 60 segundos con F12 (o combinación en mando) y slots de guardado rápido (F1-F10).
* **📱 Versiones Multiplataforma:** Disponible también para Android móvil y Android TV.

---

## ⚠️ Aviso Legal

> **Es imprescindible poseer una copia legal original del juego.**  
> Este repositorio no contiene ROMs, código binario comercial con copyright ni recursos protegidos de Nintendo o Enix / Quintet. El ejecutable comprueba y carga tu propio volcado legal de `Illusion of Time (ESP).sfc`.

### Datos de la ROM compatible

| Campo | Valor |
| :--- | :--- |
| **Juego** | Illusion of Time |
| **Región** | España / Castellano (ESP) |
| **Archivo** | `Illusion of Time (ESP).sfc` (o `.smc`) |
| **Mapeado** | HiROM |
| **CRC32** | `7c305dc8` |
| **SHA-256** | `7b98fd832f3012d902f7c312ca542f84385c6f0e883b04205cbecd769cf3e4b1` |

---

## 🕹️ Cómo Jugar

### En Windows:
1. Descarga el paquete `Illusion_of_Time_Recompiled_v1.0_Windows_x64.zip` desde la sección de **Releases**.
2. Descomprime el archivo en cualquier carpeta.
3. Coloca tu archivo `Illusion of Time (ESP).sfc` (o `.smc`) en la carpeta del juego (o selecciónalo desde el lanzador con el botón *Examinar*).
4. Haz doble clic en **`JUGAR.bat`** (o ejecuta `IllusionOfTimeSNESRecomp.exe`).

### En Linux:
1. Descarga `Illusion_of_Time_Recompiled_v1.0_Linux_x64.tar.gz` desde **Releases**.
2. Descomprime el archivo:
   ```bash
   tar -xzf Illusion_of_Time_Recompiled_v1.0_Linux_x64.tar.gz
   cd Illusion_of_Time_Linux_x64
   ```
3. Coloca tu ROM `Illusion of Time (ESP).sfc` en la carpeta.
4. Dale permisos de ejecución e inicia:
   ```bash
   chmod +x JUGAR.sh IllusionOfTimeSNESRecomp
   ./JUGAR.sh
   ```

---

## ⌨️ Controles por Defecto

| Acción | Teclado | Mando (Xbox / PS / Switch) |
| :--- | :--- | :--- |
| **Mover personaje** | Flechas / W A S D | D-Pad / Stick Izquierdo |
| **Atacar / Correr** | Z / Espacio | Botón A (PS Cruz / Switch B) |
| **Flauta / Hablar** | X | Botón B (PS Círculo / Switch A) |
| **Defensa telequinética** | C / V | LB / RB (L / R) |
| **Menú / Inventario** | Enter / Intro | Start / Options |
| **Cambiar transformación** | Shift / S | Botón Y / X |
| **Pantalla Completa** | Alt + Enter / F11 | Guía / Touchpad |
| **Rebobinar** | F12 | L3 + R3 |
| **Salir del Juego** | Escape (Esc) | Back / Select |

---

## 🛠️ Compilación desde el código fuente

### Requisitos:
* CMake 3.20 o superior
* Compilador C/C++ compatible con C11 / C++17 (MSVC en Windows, GCC/Clang en Linux)
* Python 3.8+
* Bibliotecas de desarrollo (en Linux: `libgl1-mesa-dev`, `libasound2-dev`, etc.)

```bash
git clone --recursive https://github.com/aitorcachero/Illusion-Recomp.git
cd Illusion-Recomp
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release
cmake --build build --config Release -j
```
