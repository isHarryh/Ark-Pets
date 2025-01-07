; *** Inno Setup Script for ArkPets ***
; This script is based on Inno Setup 6, a free installer for Windows programs.
; Documentation: https://jrsoftware.org/ishelp.php
; Download Inno Setup: https://jrsoftware.org/isdl.php

#define MyAppName "ArkPets"
#define MyAppVersion GetEnv("APP_VERSION")
#define AppCopyright GetEnv("APP_COPYRIGHT")
#define MyAppPublisher "Harry Huang"
#define MyAppURL "https://arkpets.harryh.cn/"

[Setup]
; WARN: The value of AppId uniquely identifies this app. Do not use the same AppId value in installers for other apps.
; (To generate a new GUID, click Tools | Generate GUID inside the Inno Setup IDE.)
AppCopyright        ={#AppCopyright}
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
OutputBaseFilename  ={#MyAppName}-Setup-v{#MyAppVersion}
OutputDir           =..\..\desktop\build\dist
SetupIconFile       =..\..\assets\icons\icon.ico
SolidCompression    =yes
UninstallDisplayIcon={app}\{#MyAppName}.ico
WizardStyle         =modern
ChangesEnvironment  =true

[Languages]
Name: "chinese_simplified";  MessagesFile: "ChineseSimplified.isl"
Name: "chinese_traditional";  MessagesFile: "ChineseTraditional.isl"
Name: "english";  MessagesFile: "compiler:Default.isl"
Name: "japanese"; MessagesFile: "compiler:Languages\Japanese.isl"

[Code]
const EnvironmentKey = 'SYSTEM\CurrentControlSet\Control\Session Manager\Environment';

procedure EnvAddPath(Path: string);
var
    Paths: string;
begin
    { Retrieve current path (use empty string if entry not exists) }
    if not RegQueryStringValue(HKEY_LOCAL_MACHINE, EnvironmentKey, 'Path', Paths)
    then Paths := '';

    { Skip if string already found in path }
    if Pos(';' + Uppercase(Path) + ';', ';' + Uppercase(Paths) + ';') > 0 then exit;

    { App string to the end of the path variable }
    Paths := Paths + ';'+ Path +';'

    { Overwrite (or create if missing) path environment variable }
    if RegWriteStringValue(HKEY_LOCAL_MACHINE, EnvironmentKey, 'Path', Paths)
    then Log(Format('The [%s] added to PATH: [%s]', [Path, Paths]))
    else Log(Format('Error while adding the [%s] to PATH: [%s]', [Path, Paths]));
end;

procedure EnvRemovePath(Path: string);
var
    Paths: string;
    P: Integer;
begin
    { Skip if registry entry not exists }
    if not RegQueryStringValue(HKEY_LOCAL_MACHINE, EnvironmentKey, 'Path', Paths) then
        exit;

    { Skip if string not found in path }
    P := Pos(';' + Uppercase(Path) + ';', ';' + Uppercase(Paths) + ';');
    if P = 0 then exit;

    { Update path variable }
    Delete(Paths, P - 1, Length(Path) + 1);

    { Overwrite path environment variable }
    if RegWriteStringValue(HKEY_LOCAL_MACHINE, EnvironmentKey, 'Path', Paths)
    then Log(Format('The [%s] removed from PATH: [%s]', [Path, Paths]))
    else Log(Format('Error while removing the [%s] from PATH: [%s]', [Path, Paths]));
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
    if CurStep = ssPostInstall
    then EnvAddPath(ExpandConstant('{app}') + '\app');
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
    if CurUninstallStep = usPostUninstall
    then EnvRemovePath(ExpandConstant('{app}') + '\app');
end;

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"
; Name: envPath; Description: "Add to PATH variable"

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
Type: filesandordirs; Name: "{app}\models_illust"
Type: files; Name: "{userstartup}\ArkPetsStartup.lnk"
Type: files; Name: "{userstartup}\ArkPetsStartupService.vbs"
