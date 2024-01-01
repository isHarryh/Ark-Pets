; *** Inno Setup Script for ArkPets ***
; This script is based on Inno Setup 6, a free installer for Windows programs.
; Documentation: https://jrsoftware.org/ishelp.php
; Download Inno Setup: https://jrsoftware.org/isdl.php

#define MyAppName "ArkPets"
#define MyAppVersion "3.0.0"
#define MyAppPublisher "Harry Huang"
#define MyAppURL "https://arkpets.harryh.cn/"

[Setup]
; WARN: The value of AppId uniquely identifies this app. Do not use the same AppId value in installers for other apps.
; (To generate a new GUID, click Tools | Generate GUID inside the Inno Setup IDE.)
AppCopyright        = Copyright (C) 2022-2023 {#MyAppPublisher}
AppId               ={{213DB689-8F8A-4DEA-BE79-545FAD7769A6}
AppName             ={#MyAppName}
AppVersion          ={#MyAppVersion}
AppVerName          ="{#MyAppName} {#MyAppVersion}"
AppPublisher        ={#MyAppPublisher}
AppPublisherURL     ={#MyAppURL}
AppSupportURL       ={#MyAppURL}

AllowNoIcons        =yes
Compression         =lzma2/max
DefaultDirName      ="{userpf}\{#MyAppName}"
DefaultGroupName    ={#MyAppName}
PrivilegesRequired  =lowest
OutputBaseFilename  ={#MyAppName}-v{#MyAppVersion}-Setup
OutputDir           =..\..\desktop\build\dist
SetupIconFile       =..\..\assets\icons\icon.ico
SolidCompression    =yes
UninstallDisplayIcon={app}\{#MyAppName}.ico
WizardStyle         =modern

[Languages]
Name: "chinese_simplified";  MessagesFile: "ChineseSimplified.isl"
Name: "chinese_traditional";  MessagesFile: "ChineseTraditional.isl"
Name: "english";  MessagesFile: "compiler:Default.isl"
Name: "japanese"; MessagesFile: "compiler:Languages\Japanese.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
Source: "..\..\desktop\build\jpackage\{#MyAppName}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\..\desktop\build\jpackage\LICENSE"; DestDir: "{app}"; Flags: ignoreversion
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppName}.exe"; WorkingDir: "{app}"
Name: "{group}\{cm:ProgramOnTheWeb,{#MyAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppName}.exe"; Tasks: desktopicon; WorkingDir: "{app}"

[Run]
Filename: "{app}\{#MyAppName}.exe"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: files; Name: "{app}\ArkPetsConfig.json"
Type: files; Name: "{app}\models_data.json"
Type: filesandordirs; Name: "{app}\logs"
Type: filesandordirs; Name: "{app}\temp"
Type: filesandordirs; Name: "{app}\models"
Type: filesandordirs; Name: "{app}\models_enemies"
Type: files; Name: "{userstartup}\ArkPetsStartupService.vbs"
