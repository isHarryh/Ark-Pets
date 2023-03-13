; *** Inno Setup Script for ArkPets ***
; Documentation: https://jrsoftware.org/ishelp.php

#define MyAppName "ArkPets"
#define MyAppVersion "2.0.0"
#define MyAppPublisher "Harry Huang"
#define MyAppURL "https://arkpets.harryh.cn/"

[Setup]
; NOTE: The value of AppId uniquely identifies this application. Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId     ={{213DB689-8F8A-4DEA-BE79-545FAD7769A6}
AppName   ={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName="{#MyAppName} v{#MyAppVersion}"
AppPublisher    ={#MyAppPublisher}
AppPublisherURL ={#MyAppURL}
AppSupportURL   ={#MyAppURL}
AppUpdatesURL   ={#MyAppURL}        
Compression     =lzma/ultra
DefaultDirName  =/{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons    =yes
; Remove the following line to run in administrative install mode (install for all users.)
PrivilegesRequired=lowest
OutputBaseFilename={#MyAppName}-Setup
OutputDir         =..\..\desktop\build\dist
SetupIconFile     =..\..\assets\icon.ico
SolidCompression  =yes
WizardStyle       =modern

[Languages]
Name: "chinese";  MessagesFile: "ChineseSimplified.isl"
Name: "english";  MessagesFile: "compiler:Default.isl"
Name: "japanese"; MessagesFile: "compiler:Languages\Japanese.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "..\..\desktop\build\jpackage\{#MyAppName}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\..\desktop\build\jpackage\LICENSE"; DestDir: "{app}"; Flags: ignoreversion
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppName}.exe"; WorkingDir: "{app}"
Name: "{group}\{cm:ProgramOnTheWeb,{#MyAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
;Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppName}.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppName}.exe"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: files; Name: "{app}\ArkPetsCustom.config"
Type: filesandordirs; Name: "{app}\temp"
Type: filesandordirs; Name: "{app}\models"
Type: filesandordirs; Name: "{app}\models_enemies"
