; *** Inno Setup Script for ArkPets ***
; Documentation: https://jrsoftware.org/ishelp.php
; Download Inno Setup: https://jrsoftware.org/isdl.php

#define MyAppName "ArkPets"
#define MyAppVersion "2.1.0"
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
AppUpdatesURL       ={#MyAppURL}
Compression         =lzma/max
DefaultDirName      =/{#MyAppName}
DefaultGroupName    ={#MyAppName}
AllowNoIcons        =yes
; Remove the following line to run in administrative install mode (install for all users.)
PrivilegesRequired  =lowest
OutputBaseFilename  ={#MyAppName}-v{#MyAppVersion}-Setup
OutputDir           =..\..\desktop\build\dist
SetupIconFile       =..\..\assets\icons\icon.ico
SolidCompression    =yes
WizardStyle         =modern

[Languages]
Name: "chinese";  MessagesFile: "ChineseSimplified.isl"
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
