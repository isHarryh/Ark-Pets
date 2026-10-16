ArkPets Supplementary Documentation
# Frequently Asked Questions

### Table of Contents
1. [Software or games crash when launching a pet?](#1)
2. [Desktop pet floats in mid-air or sticks to screen coordinates?](#2)
3. [Desktop pet window background displays solid black?](#3)
4. ["Neural Connection Failed" or model downloads fail due to network errors?](#4)
5. [Visual seams, line artefacts, or misaligned texture rendering?](#5)
6. [Launcher UI displays garbled text or broken fonts?](#6)
7. [How to collect application log files?](#7)

---

### 1.
Software or games crash when launching a pet?
> - Games built on **certain rendering engines** may crash while desktop pets are active due to graphics pipeline conflicts with LWJGL.

### 2.
Desktop pet floats in mid-air or sticks to screen coordinates?
> Common causes:
> 1. Desktop customisation tools (e.g. Wallpaper Engine, fences tools) disrupt window boundary detection routines.
> 2. The pet is standing on invisible border margins belonging to background windows.
> 3. Gravitational acceleration is set to zero under launcher **Behaviour** settings.

### 3.
Desktop pet window background displays solid black?
> Diagnostics from [Issue #7](https://github.com/isHarryh/Ark-Pets/issues/7) indicate this primarily affects NVIDIA GeForce graphics cards involving GDI driver interaction with OpenGL context initialisation. **Workaround options:**
> 
> **Option 1** (Recommended)
> 1. Open launcher Options.
> 2. Under *Rendering - Other*, tick **ANGLE Native Rendering**.
> 3. Relaunch the desktop pet.
> 
> **Option 2**
> 1. Open *Windows Settings > System > Display*.
> 2. Navigate to *Graphics settings* (Win10) or *Graphics* (Win11).
> 3. Click **Browse** and add both `ArkPets.exe` **and** `runtime/bin/java.exe` from your installation folder, setting both to **Power Saving** graphics performance.
> 4. Restart the desktop pet. If the issue persists, proceed with the following steps:
> 5. Change the graphics setting in Step 3 to **High Performance**.
> 6. Open *NVIDIA Control Panel* > *Manage 3D Settings*, then set `OpenGL GDI Compatibility` to **Prefer compatible**.
> 7. Apply settings and restart desktop pets.
> 
> **Option 3**  
> 1. Open *NVIDIA Control Panel* > *Manage 3D Settings*.
> 2. Under *Program Settings*, click **Add**, select `ArkPets.exe` and `runtime/bin/java.exe`, and set Preferred Graphics Processor to **Integrated Graphics**.
> 3. Apply changes and restart desktop pets.
> 
> **Option 4**  
> 1. Press `Win + X` and select **Device Manager**.
> 2. Expand **Display adapters**, right-click your NVIDIA graphics device, and open **Properties**.
> 3. Under the **Driver** tab, check if **Roll Back Driver** is available. If active, roll back to a previous driver release.

### 4.
"Neural Connection Failed" or model downloads fail due to network errors?
> Troubleshooting steps:
> 1. Retry downloading multiple times.
> 2. Switch **Download Strategy** under launcher Options.
> 3. If using a proxy/VPN, configure network proxy details within launcher Options.
> 4. Navigate to **Models** > **Manage Repositories**, click **Download Issues?**, and follow the diagnostic steps provided.
> 5. Switch network interfaces (e.g., mobile hotspot).
> 6. Temporarily disable security software (e.g., Kaspersky) or network firewalls.

### 5.
Visual seams, line artefacts, or misaligned texture rendering?
> This issue occurs when application versions mismatch active model database formats. **Ensure both ArkPets and model assets are updated to latest versions.**
> 
> Compatibility breakdown:
> 
> | ArkPets Version | Model Database Version | Visual Seams | Texture Artefacts |
> |:---|:---|:---:|:---:|
> | \<=2.4.1 or \>=3.7.0 | 2023 or earlier | - | - |
> | \<=2.4.1 or \>=3.7.0 | Jan 2024 – Feb 2025 | - | **+** |
> | \<=2.4.1 or \>=3.7.0 | Mar 2025 or newer | - | - |
> | \>=2.4.2 and \<=3.6.0 | 2023 or earlier | **+** | - |
> | \>=2.4.2 and \<=3.6.0 | Jan 2024 – Feb 2025 | **+** | - |
> | \>=2.4.2 and \<=3.6.0 | Mar 2025 or newer | **++** | - |
> 
> **Key:** `-` Normal operation; `+` Affects certain models; `++` Affects all models.  
> Detailed investigation records available in [Issue #76](https://github.com/isHarryh/Ark-Pets/issues/76).

### 6.
Launcher UI displays garbled text or broken fonts?
> Font rendering issues typically stem from locally installed system font conflicts (e.g. Source Han Sans variants).
> 
> Force system font fallback to resolve rendering errors:
> 1. Navigate to the application `app` directory.
> 2. Open `ArkPets.cfg` using a text editor.
> 3. Append `java-options=-Darkpets.usesystemfont=true` to the final line.
> 4. Save the file and relaunch the application.

### 7.
How to collect application log files?
> 1. In launcher Options, click **Export Logs**.
> 2. Select **Recent Logs** inside the log manager dialog, then click **Export Selected Logs**.
> 3. Set log verbosity to `DEBUG` prior to reproduction if detailed diagnostic traces are required.