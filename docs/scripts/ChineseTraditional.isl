; *** Inno Setup version 6.1.0+ Chinese Traditional (zh-tw) messages ***
;
; To download user-contributed translations of this file, go to:
;   https://jrsoftware.org/files/istrans/
;
; Note: When translating this text, do not add periods (.) to the end of
; messages that didn't have them already, because on those messages Inno
; Setup adds the periods automatically (appending a period would result in
; two periods being displayed).
;
; Translated by Harry Huang at 2023-9-1
;

[LangOptions]
; The following three entries are very important. Be sure to read and 
; understand the '[LangOptions] section' topic in the help file.
LanguageName=繁體中文
LanguageID=$0404
LanguageCodePage=0
; If the language you are translating to requires special font faces or
; sizes, uncomment any of the following entries and change them accordingly.
;DialogFontName=
;DialogFontSize=8
;WelcomeFontName=Verdana
;WelcomeFontSize=12
;TitleFontName=Arial
;TitleFontSize=29
;CopyrightFontName=Arial
;CopyrightFontSize=8

[Messages]

; *** Application titles
SetupAppTitle=安裝
SetupWindowTitle=安裝 %1
UninstallAppTitle=卸載
UninstallAppFullTitle=卸載 %1

; *** Misc. common
InformationTitle=提示
ConfirmTitle=確認
ErrorTitle=錯誤

; *** SetupLdr messages
SetupLdrStartupMessage=現在將要安裝 %1。您想要繼續安裝嗎？
LdrCannotCreateTemp=無法創建臨時文件，安裝已中止
LdrCannotExecTemp=無法訪問臨時文件，安裝已中止
HelpTextNote=

; *** Startup error messages
LastErrorMessage=%1.%n%n錯誤 %2: %3
SetupFileMissing=安裝目錄中缺少文件 %1。請修正此問題後重試，或者重新獲取程式安裝包。
SetupFileCorrupt=安裝文件已損壞，請重新獲取程式安裝包。
SetupFileCorruptOrWrongVer=安裝文件已損壞，或者與此安裝程式的版本不兼容。請修正此問題後重試，或者重新獲取程式安裝包。
InvalidParameter=無效的命令行參數：%n%n%1
SetupAlreadyRunning=安裝程式已經在運行。
WindowsVersionNotSupported=此程式不支援當前計算機上的 Windows 版本。
WindowsServicePackRequired=此程式需要 %1 服務包（%2 或更高）。
NotOnThisPlatform=此程式不能運行在 %1 上。
OnlyOnThisPlatform=此程式必須運行在 %1 上。
OnlyOnTheseArchitectures=此程式只能運行在下列處理器架構的 Windows 上：%n%n%1
WinVersionTooLowError=此程式需要 %1 版本 %2 或更高。
WinVersionTooHighError=此程式不能運行於 %1 版本 %2 或更高.
AdminPrivilegesRequired=您必須以 管理員身份 運行此安裝程式。
PowerUserPrivilegesRequired=您必須以 管理員或者擁有相關權限的用戶身份 運行此安裝程式。
SetupAppRunningError=安裝程式檢測到 %1 仍在運行。%n%n請先關閉已啟動的 %1 程式，然後點選“確定”繼續，或者點選“取消”以退出安裝。
UninstallAppRunningError=卸載程式檢測到 %1 仍在運行。%n%n請先關閉已啟動的 %1 程式，然後點選“確定”繼續，或者點選“取消”以退出卸載。

; *** Startup questions
PrivilegesRequiredOverrideTitle=選擇安裝模式
PrivilegesRequiredOverrideInstruction=請選擇程式的安裝模式
PrivilegesRequiredOverrideText1=您可以為計算機上的所有用戶安裝 %1（需要管理員權限），也可以僅為當前用戶安裝。
PrivilegesRequiredOverrideText2=您可以僅為當前用戶安裝 %1，也可以為計算機上的所有用戶安裝（需要管理員權限）。
PrivilegesRequiredOverrideAllUsers=為計算機上的所有用戶安裝
PrivilegesRequiredOverrideAllUsersRecommended=為計算機上的所有用戶安裝（推薦）(&A)
PrivilegesRequiredOverrideCurrentUser=僅為當前用戶安裝(&M)
PrivilegesRequiredOverrideCurrentUserRecommended=僅為當前用戶安裝（推薦）(&M)

; *** Misc. errors
ErrorCreatingDir=安裝程式無法創建目錄："%1"
ErrorTooManyFilesInDir=由於目錄 "%1" 包含了太多的文件，安裝程式無法嚮其中寫入文件。

; *** Setup common messages
ExitSetupTitle=退出安裝
ExitSetupMessage=如果您退出安裝，安裝將會被取消。%n%n確認退出安裝？
AboutSetupMenuItem=關於安裝程式(&A)...
AboutSetupTitle=關於安裝程式
AboutSetupMessage=%1 版本 %2%n%3%n%n%1 主頁：%n%4
AboutSetupNote=
TranslatorNote=

; *** Buttons
ButtonBack=< 上一步(&B)
ButtonNext=下一步(&N) >
ButtonInstall=安裝(&I)
ButtonOK=確定
ButtonCancel=取消
ButtonYes=是(&Y)
ButtonYesToAll=全是(&A)
ButtonNo=否(&N)
ButtonNoToAll=全否(&O)
ButtonFinish=完成(&F)
ButtonBrowse=瀏覽(&B)...
ButtonWizardBrowse=瀏覽(&R)...
ButtonNewFolder=新建檔案夾(&M)

; *** "Select Language" dialog messages
SelectLanguageTitle=選擇嚮導語言
SelectLanguageLabel=請選擇安裝嚮導所使用的語言。

; *** Common wizard text
ClickNext=點選“下一步”繼續，或點選“取消”以退出安裝。
BeveledLabel=
BrowseDialogTitle=瀏覽檔案夾
BrowseDialogLabel=在下方列錶中選擇一個檔案夾，然後點選“確定”。
NewFolderName=新建檔案夾

; *** "Welcome" wizard page
WelcomeLabel1=歡迎使用 [name] 安裝嚮導！
WelcomeLabel2=我們將安裝 [name/ver] 到您的計算機上。%n%n建議您在繼續安裝前，關閉所有可能幹擾安裝的應用程式，例如反病毒軟體。

; *** "Password" wizard page
WizardPassword=密碼
PasswordLabel1=此安裝程式有密碼保護。
PasswordLabel3=請輸入密碼（區分大小寫），然後點選“下一步”繼續。
PasswordEditLabel=密碼(&P)：
IncorrectPassword=您所輸入的密碼不正確，請重新輸入。

; *** "License Agreement" wizard page
WizardLicense=許可協議
LicenseLabel=請在繼續安裝前閱讀以下信息。
LicenseLabel3=請閱讀下方的許可協議。您必須同意此協議才能繼續安裝。
LicenseAccepted=我同意此協議(&A)
LicenseNotAccepted=我不同意此協議(&D)

; *** "Information" wizard pages
WizardInfoBefore=信息
InfoBeforeLabel=請在繼續安裝前閱讀以下信息。
InfoBeforeClickLabel=如果您想繼續安裝，請點選“下一步”。
WizardInfoAfter=信息
InfoAfterLabel=請在繼續安裝前閱讀以下信息。
InfoAfterClickLabel=如果您想繼續安裝，請點選“下一步”。

; *** "User Information" wizard page
WizardUserInfo=用戶信息
UserInfoDesc=請輸入您的信息。
UserInfoName=用戶名(&U)：
UserInfoOrg=組織(&O)：
UserInfoSerial=序列號(&S)：
UserInfoNameRequired=用戶名是必填項。

; *** "Select Destination Location" wizard page
WizardSelectDir=選擇安裝目錄
SelectDirDesc=您想將 [name] 安裝在哪裏？
SelectDirLabel3=安裝嚮導將把 [name] 安裝到下方檔案夾中。
SelectDirBrowseLabel=點選“下一步”繼續。如果您想選擇其他位置，請點選“瀏覽”。
DiskSpaceGBLabel=至少需要有 [gb] GB 的可用磁盤空間。
DiskSpaceMBLabel=至少需要有 [mb] MB 的可用磁盤空間。
CannotInstallToNetworkDrive=該程式無法被安裝到一個網路驅動器中。
CannotInstallToUNCPath=該程式無法被安裝到一個 UNC 路徑中。
InvalidPath=安裝路徑不合法。您必須輸入一個帶驅動器卷標的完整路徑，例如：%n%nC:\APP%n%n 或 UNC 路徑：%n%n\\server\share
InvalidDrive=安裝路徑所在的存儲設備（例如磁盤驅動器）不存在或不能訪問。請您插入該存儲設備，或選擇其他安裝路徑。
DiskSpaceWarningTitle=磁盤可用空間不足
DiskSpaceWarning=安裝此程式至少需要 %1 KB 的可用空間，但所選驅動器只有 %2 KB 的可用空間。%n%n您一定要繼續安裝嗎？（不推薦）
DirNameTooLong=檔案夾名稱或路徑太長。
InvalidDirName=檔案夾名稱無效。
BadDirName32=檔案夾名稱不能包含下方的任何字符：%n%n%1
DirExistsTitle=檔案夾已經存在
DirExists=檔案夾：%n%n%1%n%n已經存在。您一定要強制安裝到這個檔案夾中嗎？
DirDoesntExistTitle=檔案夾不存在
DirDoesntExist=檔案夾：%n%n%1%n%n不存在。您想要創建此檔案夾嗎？

; *** "Select Components" wizard page
WizardSelectComponents=選擇組件
SelectComponentsDesc=您想安裝此程式的哪些組件？
SelectComponentsLabel2=選擇您想要安裝的組件，去掉您不想安裝的組件，然後點選“下一步”繼續。
FullInstallation=完整安裝
; if possible don't translate 'Compact' as 'Minimal' (I mean 'Minimal' in your language)
CompactInstallation=簡潔安裝
CustomInstallation=自定義安裝
NoUninstallWarningTitle=該組件已存在
NoUninstallWarning=安裝程式檢測到下列組件已在您的計算機中安裝：%n%n%1%n%n如果您不選上這些組件，將不能成功地卸載它們。%n%n您一定要繼續嗎？
ComponentSize1=%1 KB
ComponentSize2=%1 MB
ComponentsDiskSpaceGBLabel=當前所選的組件至少需要 [gb] GB 的可用磁盤空間。
ComponentsDiskSpaceMBLabel=當前所選的組件至少需要 [mb] MB 的可用磁盤空間。

; *** "Select Additional Tasks" wizard page
WizardSelectTasks=選擇附加任務
SelectTasksDesc=您想要安裝嚮導執行哪些附加任務？
SelectTasksLabel2=選擇您想要安裝嚮導在安裝 [name] 時執行的附加任務，然後點選“下一步”繼續。

; *** "Select Start Menu Folder" wizard page
WizardSelectProgramGroup=選擇開始菜單檔案夾
SelectStartMenuFolderDesc=您想將開始菜單捷徑添加到哪裏？
SelectStartMenuFolderLabel3=安裝嚮導將在以下開始菜單檔案夾中創建程式的捷徑。
SelectStartMenuFolderBrowseLabel=點選“下一步”繼續。如果您想選擇其他位置，請點選“瀏覽”。
MustEnterGroupName=檔案夾名稱是必填項。
GroupNameTooLong=檔案夾名稱或路徑太長。
InvalidGroupName=檔案夾名稱無效。
BadGroupName=檔案夾名稱不能包含下方的任何字符：%n%n%1
NoProgramGroupCheck2=不要在開始菜單中添加捷徑(&D)

; *** "Ready to Install" wizard page
WizardReady=准備安裝
ReadyLabel1=安裝嚮導已准備好安裝 [name] 到您的計算機中。
ReadyLabel2a=點選“安裝”以進行安裝。如果您想要檢視或調整任何設定，請點選“上一步”。
ReadyLabel2b=點選“安裝”以進行安裝。
ReadyMemoUserInfo=用戶信息：
ReadyMemoDir=安裝目錄：
ReadyMemoType=安裝類型：
ReadyMemoComponents=所選組件：
ReadyMemoGroup=開始菜單檔案夾：
ReadyMemoTasks=附加任務：

; *** TDownloadWizardPage wizard page and DownloadTemporaryFile
DownloadingLabel=正在下載附加文件...
ButtonStopDownload=停止下載(&S)
StopDownload=您確定要停止下載嗎？
ErrorDownloadAborted=下載已中止
ErrorDownloadFailed=下載失敗：%1 %2
ErrorDownloadSizeFailed=獲取下載大小失敗：%1 %2
ErrorFileHash1=校驗文件哈希失敗：%1
ErrorFileHash2=無效的文件哈希：%n預期 %1，實際 %2。%n獲取的文件可能不完整。
ErrorProgress=無效的進度值：%1 / %2
ErrorFileSize=無效的文件大小：%n預期 %1，實際 %2。%n獲取的文件可能不完整。

; *** "Preparing to Install" wizard page
WizardPreparing=開始安裝
PreparingDesc=安裝嚮導正在安裝 [name] 到您的計算機中。
PreviousInstallNotCompleted=先前程式的安裝/卸載尚未完成。%n%n您需要重新啟動計算機，然後再次運行安裝程式以完成 [name] 的安裝。
CannotContinue=安裝嚮導遇到問題，無法繼續進行安裝。請點選“取消”退出。
ApplicationsFound=以下應用程式正在佔用需要由安裝嚮導更新的文件。建議您允許安裝嚮導自動關閉這些應用程式。
ApplicationsFound2=以下應用程式正在佔用需要由安裝嚮導更新的文件。建議您允許安裝嚮導自動關閉這些應用程式。安裝完成後，安裝嚮導將嘗試重啟這些應用程式。
CloseApplications=自動關閉這些應用程式(&A)
DontCloseApplications=不要關閉這些應用程式(&D)
ErrorCloseApplications=安裝嚮導無法自動關閉所有應用程式。在繼續之前，建議您手動關閉所有正在佔用相關文件的應用程式。
PrepareToInstallNeedsRestart=安裝嚮導請求重新啟動計算機。重啟後，請您再次運行安裝程式以完成 [name] 的安裝。%n%n是否立即重新啟動？

; *** "Installing" wizard page
WizardInstalling=正在安裝
InstallingLabel=我們正在安裝 [name] 到您的計算機上，請稍作等待...

; *** "Setup Completed" wizard page
FinishedHeadingLabel=[name] 安裝完成
FinishedLabelNoIcons=已成功安裝 [name]。
FinishedLabel=已在您的計算機上安裝了 [name]，此應用程式可以通過點選捷徑運行。
ClickFinish=點選“完成”以關閉安裝嚮導。祝您使用愉快！
FinishedRestartLabel=要完成 [name] 的安裝，我們必須重新啟動您的計算機。是否立即重新啟動？
FinishedRestartMessage=要完成 [name] 的安裝，我們必須重新啟動您的計算機。%n%n是否立即重新啟動？
ShowReadmeCheck=是，我想查閱自述文件
YesRadio=是，立即重新啟動計算機(&Y)
NoRadio=否，稍後重新啟動計算機(&N)
; used for example as 'Run MyProg.exe'
RunEntryExec=立即啟動 %1
; used for example as 'View Readme.txt'
RunEntryShellExec=查閱 %1

; *** "Setup Needs the Next Disk" stuff
ChangeDiskTitle=安裝程式需要下一張磁盤
SelectDiskLabel2=請插入磁盤 %1 併點選“確定”。%n%n如果此磁盤中的文件可以在下列檔案夾之外的檔案夾中找到，請輸入正確的路徑或點選“瀏覽”。
PathLabel=路徑(&P)：
FileNotInDir2=文件 "%1" 未位於 "%2" 中。請您插入該存儲設備，或選擇其他安裝路徑。
SelectDirectoryLabel=請指定下一張磁盤的位置。

; *** Installation phase messages
SetupAborted=安裝程式未完成安裝。%n%n請修正此問題併重新運行安裝程式。
AbortRetryIgnoreSelectAction=選擇操作
AbortRetryIgnoreRetry=重試(&T)
AbortRetryIgnoreIgnore=忽略錯誤併繼續(&I)
AbortRetryIgnoreCancel=取消安裝

; *** Installation status messages
StatusClosingApplications=正在關閉應用程式...
StatusCreateDirs=正在創建目錄...
StatusExtractFiles=正在解壓縮文件...
StatusCreateIcons=正在創建捷徑...
StatusCreateIniEntries=正在創建 INI 條目...
StatusCreateRegistryEntries=正在創建註冊錶條目...
StatusRegisterFiles=正在註冊文件...
StatusSavingUninstall=正在保存卸載信息...
StatusRunProgram=馬上就好...
StatusRestartingApplications=正在重啟應用程式...
StatusRollback=正在撤銷更改...

; *** Misc. errors
ErrorInternal2=內部錯誤：%1
ErrorFunctionFailedNoCode=%1 失敗
ErrorFunctionFailed=%1 失敗；錯誤代碼 %2
ErrorFunctionFailedWithMessage=%1 失敗；錯誤代碼 %2；%n%3
ErrorExecutingProgram=無法執行此文件：%n%1

; *** Registry errors
ErrorRegOpenKey=打開註冊錶項時出錯：%n%1\%2
ErrorRegCreateKey=創建註冊錶項時出錯：%n%1\%2
ErrorRegWriteKey=寫入註冊錶項時出錯：%n%1\%2

; *** INI errors
ErrorIniEntry=在文件 "%1" 中創建 INI 條目時出錯。

; *** File copying errors
FileAbortRetryIgnoreSkipNotRecommended=跳過這個文件(&S)（不推薦）
FileAbortRetryIgnoreIgnoreNotRecommended=忽略錯誤併繼續(&I)（不推薦）
SourceIsCorrupted=源文件已損壞
SourceDoesntExist=源文件 "%1" 不存在
ExistingFileReadOnly2=無法替換已存在的文件，因為它具有只讀屬性。
ExistingFileReadOnlyRetry=移除只讀屬性併重試(&R)
ExistingFileReadOnlyKeepExisting=保留此文件(&K)
ErrorReadingExistingDest=嘗試讀取已存在的文件時出錯：
FileExistsSelectAction=選擇操作
FileExists2=文件已經存在。
FileExistsOverwriteExisting=覆蓋已存在的文件(&O)
FileExistsKeepExisting=保留已存在的文件(&K)
FileExistsOverwriteOrKeepAll=為所有沖突項執行此操作(&D)
ExistingFileNewerSelectAction=選擇操作
ExistingFileNewer2=文件已經存在，併且其新於將要安裝的文件。
ExistingFileNewerOverwriteExisting=覆蓋已存在的文件(&O)
ExistingFileNewerKeepExisting=保留已存在的文件(&K)（推薦）
ExistingFileNewerOverwriteOrKeepAll=為所有沖突項執行此操作(&D)
ErrorChangingAttr=嘗試修改文件屬性時出錯：
ErrorCreatingTemp=嘗試在目標目錄中創建文件時出錯：
ErrorReadingSource=嘗試讀取源文件時出錯：
ErrorCopying=嘗試復制文件時出錯：
ErrorReplacingExistingFile=嘗試替換文件時出錯：
ErrorRestartReplace=重啟替換失敗：
ErrorRenamingTemp=嘗試重命名目標目錄中的文件時出錯：
ErrorRegisterServer=無法註冊 DLL/OCX：%1
ErrorRegSvr32Failed=RegSvr32 註冊失敗，返回代碼 %1
ErrorRegisterTypeLib=無法註冊類型庫：%1

; *** Uninstall display name markings
; used for example as 'My Program (32-bit)'
UninstallDisplayNameMark=%1 (%2)
; used for example as 'My Program (32-bit, All users)'
UninstallDisplayNameMarks=%1 (%2, %3)
UninstallDisplayNameMark32Bit=32位
UninstallDisplayNameMark64Bit=64位
UninstallDisplayNameMarkAllUsers=所有用戶
UninstallDisplayNameMarkCurrentUser=當前用戶

; *** Post-installation errors
ErrorOpeningReadme=嘗試打開自述文件時出錯。
ErrorRestartingComputer=安裝嚮導無法重新啟動計算機，請您手動重啟。

; *** Uninstaller messages
UninstallNotFound=文件 "%1" 已丟失，無法繼續進行卸載。
UninstallOpenError=文件 "%1" 無法訪問，無法繼續進行卸載
UninstallUnsupportedVer=此版本的卸載程式無法識別卸載檔案文件 "%1" 的格式，無法繼續進行卸載。
UninstallUnknownEntry=在卸載檔案中遇到一個未知的條目 (%1)
ConfirmUninstall=您確定想要卸載 %1 及其所有組件嗎？
UninstallOnlyOnWin64=該程式只能在 64位 Windows 中進行卸載。
OnlyAdminCanUninstall=該程式需要用戶以 管理員身份 進行卸載。
UninstallStatusLabel=正在從您的計算機中卸載 %1，請稍作等待...
UninstalledAll=%1 已順利地完成了卸載。%n%n期待與您的再次相見。
UninstalledMost=%1 的卸載已完成。%n%n有一些內容無法被刪除，您可以手動刪除它們。
UninstalledAndNeedsRestart=要完成 [name] 的安裝，我們必須重新啟動您的計算機。%n%n是否立即重新啟動？
UninstallDataCorrupted=文件 "%1" 已損壞，無法繼續進行卸載

; *** Uninstallation phase messages
ConfirmDeleteSharedFileTitle=是否刪除共享文件？
ConfirmDeleteSharedFile2=以下共享文件可能已經不再被其他程式使用。您想要刪除這些文件嗎？%n%n如果仍有程式需要使用這些文件，那麽在刪除文件後，這些程式可能無法正常運行。如果您不確定，請選擇“否”，把這些文件保留在繫統中併沒有什麽壞處。
SharedFileNameLabel=文件名稱：
SharedFileLocationLabel=位置：
WizardUninstalling=卸載狀態
StatusUninstalling=正在卸載 %1 ...

; *** Shutdown block reasons
ShutdownBlockReasonInstallingApp=仍在安裝 %1 ...
ShutdownBlockReasonUninstallingApp=仍在卸載 %1 ...

; The custom messages below aren't used by Setup itself, but if you make
; use of them in your scripts, you'll want to translate them.

[CustomMessages]

NameAndVersion=%1 版本 %2
AdditionalIcons=附加捷徑：
CreateDesktopIcon=創建桌面捷徑(&D)
CreateQuickLaunchIcon=創建快速啟動圖示(&Q)
ProgramOnTheWeb=%1 網站
UninstallProgram=卸載 %1
LaunchProgram=立即啟動 %1
AssocFileExtension=將文件擴展名 %2 關聯到 %1 (&A)
AssocingFileExtension=正在將文件擴展名 %2 關聯到 %1 ...
AutoStartProgramGroupDescription=啟動組：
AutoStartProgram=自動啟動 %1
AddonHostProgramNotFound=無法在所選檔案夾中找到 %1。%n%n您仍然要繼續嗎？
