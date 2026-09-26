<!-- Ark-Pets Documentation -->
<!-- Repository: https://github.com/isHarryh/Ark-Pets -->

<!--suppress HtmlDeprecatedAttribute -->
<div align="center" style="text-align:center">
   <h1> Ark-Pets </h1>
   <img alt="ArkPets icon" width="64" src="https://raw.githubusercontent.com/isHarryh/Ark-Pets/v3.x/assets/icons/icon.png">
   <p>Arknights Desktop Pets (ArkPets)</p>
   <p>
      <img alt="GitHub Latest Release" src="https://img.shields.io/github/v/release/isHarryh/Ark-Pets?display_name=tag&label=Release&sort=semver&include_prereleases">
      <img alt="GitHub Stars" src="https://img.shields.io/github/stars/isHarryh/Ark-Pets?label=Stars">
   </p>
   <hr>
   <p>
      <img alt="GitHub Top Language" src="https://img.shields.io/github/languages/top/isHarryh/Ark-Pets?label=Java">
      <img alt="GitHub License" src="https://img.shields.io/github/license/isHarryh/Ark-Pets?label=License">
      <img alt="Code Factor Grade" src="https://img.shields.io/codefactor/grade/github/isHarryh/Ark-Pets?label=CodeFactor">
      <img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/isHarryh/Ark-Pets/build.yml?label=Build">
   </p>
   <p>
      <a href="https://arkpets.harryh.cn?from=readme">🔗 Official Website</a> • 
      <a href="README.md">中文文档</a>
   </p>
</div>

## Introduction

### Features

1. **Launches *Arknights* character models as desktop pets.** <details><summary>View Details</summary>
    Supported model types currently include:
    1. Operator Base Chibis (including outfits);
    2. Operator Live2D / Dynamic Illustrations (including outfits);
    3. Enemy Combat Chibis.
2. **Graphical User Interface provided via launcher for browsing models and adjusting settings.** <details><summary>View Details</summary>
    1. Search models by name, Pinyin, or brand, or filter by category;
    2. Download community-maintained model repositories directly from the internet;
    3. Customise interactive actions, placement, and physics parameters;
    4. Customise display preferences such as image scaling, frame rate limits, and window bounds.
3. **Simulates base chibi behaviour from the game.** <details><summary>View Details</summary>
    1. Supports walking, sitting, and lying down animations;
    2. Responds to mouse clicks with poke interactions;
    3. Operators with unique base interactions have a chance to trigger them randomly.
4. **Simulates enemy chibi behaviour from the game.** <details><summary>View Details</summary>
    1. Enemies with walking animations can wander across the screen;
    2. Enemies with attack animations react when clicked.
5. **Simulated planar gravity field.** <details><summary>View Details</summary>
    1. Pets respond to gravity effects like free-fall physics;
    2. Pets can be dragged across onto extended monitor setups;
    3. Pets can land and stand on top of open application windows.
6. **System Tray Integration.** <details><summary>View Details</summary>
    1. Open the context menu by right-clicking either the tray icon or the pet itself;
    2. Toggle Manual Mode or Click-Through Mode;
    3. Switch forms for multi-form characters;
    4. Close individual pets or quit the entire launcher;
    5. Active desktop pets are consolidated into a single tray icon while the launcher runs;
    6. If the launcher is closed, each pet manages its own tray icon.
7. **Supports auto-start on boot and [additional features](#additional-features).**

### Preview

<table style="margin-left: auto; margin-right: auto;">
    <tr>
        <td> <img alt="demo1" width="250" src="https://raw.githubusercontent.com/isHarryh/Ark-Pets/v3.x/docs/imgs/demo_1.png"> </td>
        <td> <img alt="demo2" width="250" src="https://raw.githubusercontent.com/isHarryh/Ark-Pets/v3.x/docs/imgs/demo_2.png"> </td>
        <td> <img alt="demo3" width="250" src="https://raw.githubusercontent.com/isHarryh/Ark-Pets/v3.x/docs/imgs/demo_3.png"> </td>
    </tr>
</table>

### Roadmap

Planned updates for future development:

- Internationalisation and responsive layouts
- On-demand asset downloading
- Operator voice line support
- Upgrading core dependency library versions
- Persistent setting memory for features like Click-Through Mode

### Useful Resources

- **Changelog** > [View Here](CHANGELOG.md)
- **FAQ** > [View Here](docs/FAQ.EN.md)
- **Telemetry Notice** > [View Here](docs/Telemetry.EN.md)

## Usage Instructions

Currently supports Windows 7 and above.

### Quick Start

1. Head over to the [**Releases Page**](https://github.com/isHarryh/Ark-Pets/releases) and download the latest **ArkPets-Setup.exe** installer.
2. Run the installer to complete setup. Once finished, launch the ArkPets client.
3. **Model files must be downloaded** prior to first use. Navigate to the "Models" section in the launcher, open the "Model Repository Manager" panel, and click "Download Models".
4. Search for and select your desired character under the "Models" tab, then click "Launch" in the bottom-left corner to spawn your desktop pet.

> Notes:
> - To close a running pet, right-click either the character or the ArkPets system tray icon and click "Exit".
> - If network issues prevent downloading within the app, visit the [ArkModels Repository](https://github.com/isHarryh/Ark-Models) to download the archive manually, then click "Import Archive" under the "Model Repository Manager" panel.
> - Upgrading from v2.x or v3.x does not require uninstalling previous versions; running the new installer directly will update the application smoothly.

### Additional Features

Explore key highlights below, alongside further options inside the launcher itself.

#### Desktop Pet Features

- **Auto-Start on Boot**: Enable auto-start under launcher "Settings" to automatically launch your last-used pet upon Windows startup.
- **Manual Control**: Enable "Manual Mode" via the tray menu to move your pet around using the left and right arrow keys, or cycle through actions with the up and down keys.
- **Click-Through Mode**: Prevents accidental clicks whilst gaming or watching videos by ignoring all mouse interactions, passing inputs through to the windows underneath.
- **Taskbar Distance Offset**: If the pet fails to detect your taskbar position properly (causing it to submerge), adjust the taskbar height manually under the "Behaviour" tab.
- **Highlight Outlines & Shadows**: Recreates in-game base selection effects alongside directional shadows for added depth. Disable these in "Settings" to minimise performance usage on lower-end systems.

#### Launcher Features

- **Noticeboard**: Click the "Announcements" button on the sidebar to view update logs and guides. The noticeboard automatically opens upon startup whenever unread announcements are available.
- **Online Model Updates**: Check for and download model updates directly from the "Models" - "Model Repository Manager" panel.
- **Software Updates**: Check for and install software updates directly under the "Settings" menu.
- **Mirror-Chyan Integration**: Integrated [Mirror-Chyan](https://mirrorchyan.com/) starting from v3.9 to offer high-speed content downloads.

### Advanced Usage

Alongside the basic installer described in [Quick Start](#quick-start):

- Download portable `.zip` archives to extract and run without installation.
- Run stand-alone `.jar` executables directly if a `JDK17` [Java](https://www.java.com) environment is installed locally (note: auto-start on boot is unsupported via JAR execution).
- Refer to the [Command-Line Guide](docs/CmdLine.EN.md) to launch pets programmatically.
- Refer to the [Custom Model Guide](docs/CustomModel.EN.md) to import custom models.
- Disable "Launch pet as background process" in launcher settings when capturing desktop pets with streaming tools like OBS.

Native builds for macOS and Linux are currently in development.

## About

### Acknowledgements

Sincere thanks to every individual and community contributor who supported the development of ArkPets.

- See [Credits and Third-Party Dependencies](docs/Credits.EN.md)

### Licence

This project is licensed under the **GPL-3.0 Licence**. Source code may be freely modified and distributed, provided copyright notices are retained and derivative works remain open source under the same terms.

### Contributing

Contributions via [Issues](https://github.com/isHarryh/Ark-Pets/issues) and pull requests are warmly welcomed. Please check existing entries to avoid duplicate submissions and ensure the issue template is filled out completely.

- See the [Developer Wiki](https://github.com/isHarryh/Ark-Pets/wiki)

### Related Projects

Ecosystem projects linked to ArkPets:

- [isHarryh / Ark-Models](https://github.com/isHarryh/Ark-Models): *Arknights* Spine model repository
- [litwak913 / Ark-Pets-Integration](https://github.com/litwak913/Ark-Pets-Integration): Integration library for other desktop systems
- [fuyufjh / ArkPets-Web](https://github.com/fuyufjh/ArkPets-Web): Stand-alone web-rendered implementation of ArkPets
- [isHarryh / Ark-Unpacker](https://github.com/isHarryh/Ark-Unpacker): Utility for extracting in-game assets
- [Aloento / SuperSpineViewer](https://github.com/Aloento/SuperSpineViewer): Tool for inspecting and viewing Spine assets

-----

<div align="center">
   <p><i>GitHub Star History</i></p>
   <picture>
      <!--suppress HtmlUnknownTarget -->
      <source media="(prefers-color-scheme: dark)" srcset="https://api.star-history.com/chart?repos=isHarryh/Ark-Pets&type=date&theme=dark&legend=top-left&sealed_token=-hBNTJMB8JJLlN0Nzzg9Vd9lUkK3fVRMRHpHjy0cBTUzdR0niBpUbRQ8qVof2QRG1UC2k8L9OVvkra1_RHYwzV9IBgA2zY9nPbzZAsAUD3xfYHg5FtO-vw" />
      <!--suppress HtmlUnknownTarget -->
      <source media="(prefers-color-scheme: light)" srcset="https://api.star-history.com/chart?repos=isHarryh/Ark-Pets&type=date&legend=top-left&sealed_token=-hBNTJMB8JJLlN0Nzzg9Vd9lUkK3fVRMRHpHjy0cBTUzdR0niBpUbRQ8qVof2QRG1UC2k8L9OVvkra1_RHYwzV9IBgA2zY9nPbzZAsAUD3xfYHg5FtO-vw" />
      <img alt="Star History Chart" src="https://api.star-history.com/chart?repos=isHarryh/Ark-Pets&type=date&legend=top-left&sealed_token=-hBNTJMB8JJLlN0Nzzg9Vd9lUkK3fVRMRHpHjy0cBTUzdR0niBpUbRQ8qVof2QRG1UC2k8L9OVvkra1_RHYwzV9IBgA2zY9nPbzZAsAUD3xfYHg5FtO-vw" />
   </picture>
</div>