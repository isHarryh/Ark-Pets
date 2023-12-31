; *** Inno Setup version 6.1.0+ Chinese Simplified (zh-cn) messages ***
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
LanguageName=简体中文
LanguageID=$0804
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
SetupAppTitle=安装
SetupWindowTitle=安装 %1
UninstallAppTitle=卸载
UninstallAppFullTitle=卸载 %1

; *** Misc. common
InformationTitle=提示
ConfirmTitle=确认
ErrorTitle=错误

; *** SetupLdr messages
SetupLdrStartupMessage=现在将要安装 %1。您想要继续安装吗？
LdrCannotCreateTemp=无法创建临时文件，安装已中止
LdrCannotExecTemp=无法访问临时文件，安装已中止
HelpTextNote=

; *** Startup error messages
LastErrorMessage=%1.%n%n错误 %2: %3
SetupFileMissing=安装目录中缺少文件 %1。请修正此问题后重试，或者重新获取程序安装包。
SetupFileCorrupt=安装文件已损坏，请重新获取程序安装包。
SetupFileCorruptOrWrongVer=安装文件已损坏，或者与此安装程序的版本不兼容。请修正此问题后重试，或者重新获取程序安装包。
InvalidParameter=无效的命令行参数：%n%n%1
SetupAlreadyRunning=安装程序已经在运行。
WindowsVersionNotSupported=此程序不支持当前计算机上的 Windows 版本。
WindowsServicePackRequired=此程序需要 %1 服务包（%2 或更高）。
NotOnThisPlatform=此程序不能运行在 %1 上。
OnlyOnThisPlatform=此程序必须运行在 %1 上。
OnlyOnTheseArchitectures=此程序只能运行在下列处理器架构的 Windows 上：%n%n%1
WinVersionTooLowError=此程序需要 %1 版本 %2 或更高。
WinVersionTooHighError=此程序不能运行于 %1 版本 %2 或更高.
AdminPrivilegesRequired=您必须以 管理员身份 运行此安装程序。
PowerUserPrivilegesRequired=您必须以 管理员或者拥有相关权限的用户身份 运行此安装程序。
SetupAppRunningError=安装程序检测到 %1 仍在运行。%n%n请先关闭已启动的 %1 程序，然后点击“确定”继续，或者点击“取消”以退出安装。
UninstallAppRunningError=卸载程序检测到 %1 仍在运行。%n%n请先关闭已启动的 %1 程序，然后点击“确定”继续，或者点击“取消”以退出卸载。

; *** Startup questions
PrivilegesRequiredOverrideTitle=选择安装模式
PrivilegesRequiredOverrideInstruction=请选择程序的安装模式
PrivilegesRequiredOverrideText1=您可以为计算机上的所有用户安装 %1（需要管理员权限），也可以仅为当前用户安装。
PrivilegesRequiredOverrideText2=您可以仅为当前用户安装 %1，也可以为计算机上的所有用户安装（需要管理员权限）。
PrivilegesRequiredOverrideAllUsers=为计算机上的所有用户安装
PrivilegesRequiredOverrideAllUsersRecommended=为计算机上的所有用户安装（推荐）(&A)
PrivilegesRequiredOverrideCurrentUser=仅为当前用户安装(&M)
PrivilegesRequiredOverrideCurrentUserRecommended=仅为当前用户安装（推荐）(&M)

; *** Misc. errors
ErrorCreatingDir=安装程序无法创建目录："%1"
ErrorTooManyFilesInDir=由于目录 "%1" 包含了太多的文件，安装程序无法向其中写入文件。

; *** Setup common messages
ExitSetupTitle=退出安装
ExitSetupMessage=如果您退出安装，安装将会被取消。%n%n确认退出安装？
AboutSetupMenuItem=关于安装程序(&A)...
AboutSetupTitle=关于安装程序
AboutSetupMessage=%1 版本 %2%n%3%n%n%1 主页：%n%4
AboutSetupNote=
TranslatorNote=

; *** Buttons
ButtonBack=< 上一步(&B)
ButtonNext=下一步(&N) >
ButtonInstall=安装(&I)
ButtonOK=确定
ButtonCancel=取消
ButtonYes=是(&Y)
ButtonYesToAll=全是(&A)
ButtonNo=否(&N)
ButtonNoToAll=全否(&O)
ButtonFinish=完成(&F)
ButtonBrowse=浏览(&B)...
ButtonWizardBrowse=浏览(&R)...
ButtonNewFolder=新建文件夹(&M)

; *** "Select Language" dialog messages
SelectLanguageTitle=选择向导语言
SelectLanguageLabel=请选择安装向导所使用的语言。

; *** Common wizard text
ClickNext=点击“下一步”继续，或点击“取消”以退出安装。
BeveledLabel=
BrowseDialogTitle=浏览文件夹
BrowseDialogLabel=在下方列表中选择一个文件夹，然后点击“确定”。
NewFolderName=新建文件夹

; *** "Welcome" wizard page
WelcomeLabel1=欢迎使用 [name] 安装向导！
WelcomeLabel2=我们将安装 [name/ver] 到您的计算机上。%n%n建议您在继续安装前，关闭所有可能干扰安装的应用程序，例如反病毒软件。

; *** "Password" wizard page
WizardPassword=密码
PasswordLabel1=此安装程序有密码保护。
PasswordLabel3=请输入密码（区分大小写），然后点击“下一步”继续。
PasswordEditLabel=密码(&P)：
IncorrectPassword=您所输入的密码不正确，请重新输入。

; *** "License Agreement" wizard page
WizardLicense=许可协议
LicenseLabel=请在继续安装前阅读以下信息。
LicenseLabel3=请阅读下方的许可协议。您必须同意此协议才能继续安装。
LicenseAccepted=我同意此协议(&A)
LicenseNotAccepted=我不同意此协议(&D)

; *** "Information" wizard pages
WizardInfoBefore=信息
InfoBeforeLabel=请在继续安装前阅读以下信息。
InfoBeforeClickLabel=如果您想继续安装，请点击“下一步”。
WizardInfoAfter=信息
InfoAfterLabel=请在继续安装前阅读以下信息。
InfoAfterClickLabel=如果您想继续安装，请点击“下一步”。

; *** "User Information" wizard page
WizardUserInfo=用户信息
UserInfoDesc=请输入您的信息。
UserInfoName=用户名(&U)：
UserInfoOrg=组织(&O)：
UserInfoSerial=序列号(&S)：
UserInfoNameRequired=用户名是必填项。

; *** "Select Destination Location" wizard page
WizardSelectDir=选择安装目录
SelectDirDesc=您想将 [name] 安装在哪里？
SelectDirLabel3=安装向导将把 [name] 安装到下方文件夹中。
SelectDirBrowseLabel=点击“下一步”继续。如果您想选择其他位置，请点击“浏览”。
DiskSpaceGBLabel=至少需要有 [gb] GB 的可用磁盘空间。
DiskSpaceMBLabel=至少需要有 [mb] MB 的可用磁盘空间。
CannotInstallToNetworkDrive=该程序无法被安装到一个网络驱动器中。
CannotInstallToUNCPath=该程序无法被安装到一个 UNC 路径中。
InvalidPath=安装路径不合法。您必须输入一个带驱动器卷标的完整路径，例如：%n%nC:\APP%n%n 或 UNC 路径：%n%n\\server\share
InvalidDrive=安装路径所在的存储设备（例如磁盘驱动器）不存在或不能访问。请您插入该存储设备，或选择其他安装路径。
DiskSpaceWarningTitle=磁盘可用空间不足
DiskSpaceWarning=安装此程序至少需要 %1 KB 的可用空间，但所选驱动器只有 %2 KB 的可用空间。%n%n您一定要继续安装吗？（不推荐）
DirNameTooLong=文件夹名称或路径太长。
InvalidDirName=文件夹名称无效。
BadDirName32=文件夹名称不能包含下方的任何字符：%n%n%1
DirExistsTitle=文件夹已经存在
DirExists=文件夹：%n%n%1%n%n已经存在。您一定要强制安装到这个文件夹中吗？
DirDoesntExistTitle=文件夹不存在
DirDoesntExist=文件夹：%n%n%1%n%n不存在。您想要创建此文件夹吗？

; *** "Select Components" wizard page
WizardSelectComponents=选择组件
SelectComponentsDesc=您想安装此程序的哪些组件？
SelectComponentsLabel2=选择您想要安装的组件，去掉您不想安装的组件，然后点击“下一步”继续。
FullInstallation=完整安装
; if possible don't translate 'Compact' as 'Minimal' (I mean 'Minimal' in your language)
CompactInstallation=简洁安装
CustomInstallation=自定义安装
NoUninstallWarningTitle=该组件已存在
NoUninstallWarning=安装程序检测到下列组件已在您的计算机中安装：%n%n%1%n%n如果您不选上这些组件，将不能成功地卸载它们。%n%n您一定要继续吗？
ComponentSize1=%1 KB
ComponentSize2=%1 MB
ComponentsDiskSpaceGBLabel=当前所选的组件至少需要 [gb] GB 的可用磁盘空间。
ComponentsDiskSpaceMBLabel=当前所选的组件至少需要 [mb] MB 的可用磁盘空间。

; *** "Select Additional Tasks" wizard page
WizardSelectTasks=选择附加任务
SelectTasksDesc=您想要安装向导执行哪些附加任务？
SelectTasksLabel2=选择您想要安装向导在安装 [name] 时执行的附加任务，然后点击“下一步”继续。

; *** "Select Start Menu Folder" wizard page
WizardSelectProgramGroup=选择开始菜单文件夹
SelectStartMenuFolderDesc=您想将开始菜单快捷方式添加到哪里？
SelectStartMenuFolderLabel3=安装向导将在以下开始菜单文件夹中创建程序的快捷方式。
SelectStartMenuFolderBrowseLabel=点击“下一步”继续。如果您想选择其他位置，请点击“浏览”。
MustEnterGroupName=文件夹名称是必填项。
GroupNameTooLong=文件夹名称或路径太长。
InvalidGroupName=文件夹名称无效。
BadGroupName=文件夹名称不能包含下方的任何字符：%n%n%1
NoProgramGroupCheck2=不要在开始菜单中添加快捷方式(&D)

; *** "Ready to Install" wizard page
WizardReady=准备安装
ReadyLabel1=安装向导已准备好安装 [name] 到您的计算机中。
ReadyLabel2a=点击“安装”以进行安装。如果您想要查看或调整任何设置，请点击“上一步”。
ReadyLabel2b=点击“安装”以进行安装。
ReadyMemoUserInfo=用户信息：
ReadyMemoDir=安装目录：
ReadyMemoType=安装类型：
ReadyMemoComponents=所选组件：
ReadyMemoGroup=开始菜单文件夹：
ReadyMemoTasks=附加任务：

; *** TDownloadWizardPage wizard page and DownloadTemporaryFile
DownloadingLabel=正在下载附加文件...
ButtonStopDownload=停止下载(&S)
StopDownload=您确定要停止下载吗？
ErrorDownloadAborted=下载已中止
ErrorDownloadFailed=下载失败：%1 %2
ErrorDownloadSizeFailed=获取下载大小失败：%1 %2
ErrorFileHash1=校验文件哈希失败：%1
ErrorFileHash2=无效的文件哈希：%n预期 %1，实际 %2。%n获取的文件可能不完整。
ErrorProgress=无效的进度值：%1 / %2
ErrorFileSize=无效的文件大小：%n预期 %1，实际 %2。%n获取的文件可能不完整。

; *** "Preparing to Install" wizard page
WizardPreparing=开始安装
PreparingDesc=安装向导正在安装 [name] 到您的计算机中。
PreviousInstallNotCompleted=先前程序的安装/卸载尚未完成。%n%n您需要重新启动计算机，然后再次运行安装程序以完成 [name] 的安装。
CannotContinue=安装向导遇到问题，无法继续进行安装。请点击“取消”退出。
ApplicationsFound=以下应用程序正在占用需要由安装向导更新的文件。建议您允许安装向导自动关闭这些应用程序。
ApplicationsFound2=以下应用程序正在占用需要由安装向导更新的文件。建议您允许安装向导自动关闭这些应用程序。安装完成后，安装向导将尝试重启这些应用程序。
CloseApplications=自动关闭这些应用程序(&A)
DontCloseApplications=不要关闭这些应用程序(&D)
ErrorCloseApplications=安装向导无法自动关闭所有应用程序。在继续之前，建议您手动关闭所有正在占用相关文件的应用程序。
PrepareToInstallNeedsRestart=安装向导请求重新启动计算机。重启后，请您再次运行安装程序以完成 [name] 的安装。%n%n是否立即重新启动？

; *** "Installing" wizard page
WizardInstalling=正在安装
InstallingLabel=我们正在安装 [name] 到您的计算机上，请稍作等待...

; *** "Setup Completed" wizard page
FinishedHeadingLabel=[name] 安装完成
FinishedLabelNoIcons=已成功安装 [name]。
FinishedLabel=已在您的计算机上安装了 [name]，此应用程序可以通过点击快捷方式运行。
ClickFinish=点击“完成”以关闭安装向导。祝您使用愉快！
FinishedRestartLabel=要完成 [name] 的安装，我们必须重新启动您的计算机。是否立即重新启动？
FinishedRestartMessage=要完成 [name] 的安装，我们必须重新启动您的计算机。%n%n是否立即重新启动？
ShowReadmeCheck=是，我想查阅自述文件
YesRadio=是，立即重新启动计算机(&Y)
NoRadio=否，稍后重新启动计算机(&N)
; used for example as 'Run MyProg.exe'
RunEntryExec=立即启动 %1
; used for example as 'View Readme.txt'
RunEntryShellExec=查阅 %1

; *** "Setup Needs the Next Disk" stuff
ChangeDiskTitle=安装程序需要下一张磁盘
SelectDiskLabel2=请插入磁盘 %1 并点击“确定”。%n%n如果此磁盘中的文件可以在下列文件夹之外的文件夹中找到，请输入正确的路径或点击“浏览”。
PathLabel=路径(&P)：
FileNotInDir2=文件 "%1" 未位于 "%2" 中。请您插入该存储设备，或选择其他安装路径。
SelectDirectoryLabel=请指定下一张磁盘的位置。

; *** Installation phase messages
SetupAborted=安装程序未完成安装。%n%n请修正此问题并重新运行安装程序。
AbortRetryIgnoreSelectAction=选择操作
AbortRetryIgnoreRetry=重试(&T)
AbortRetryIgnoreIgnore=忽略错误并继续(&I)
AbortRetryIgnoreCancel=取消安装

; *** Installation status messages
StatusClosingApplications=正在关闭应用程序...
StatusCreateDirs=正在创建目录...
StatusExtractFiles=正在解压缩文件...
StatusCreateIcons=正在创建快捷方式...
StatusCreateIniEntries=正在创建 INI 条目...
StatusCreateRegistryEntries=正在创建注册表条目...
StatusRegisterFiles=正在注册文件...
StatusSavingUninstall=正在保存卸载信息...
StatusRunProgram=马上就好...
StatusRestartingApplications=正在重启应用程序...
StatusRollback=正在撤销更改...

; *** Misc. errors
ErrorInternal2=内部错误：%1
ErrorFunctionFailedNoCode=%1 失败
ErrorFunctionFailed=%1 失败；错误代码 %2
ErrorFunctionFailedWithMessage=%1 失败；错误代码 %2；%n%3
ErrorExecutingProgram=无法执行此文件：%n%1

; *** Registry errors
ErrorRegOpenKey=打开注册表项时出错：%n%1\%2
ErrorRegCreateKey=创建注册表项时出错：%n%1\%2
ErrorRegWriteKey=写入注册表项时出错：%n%1\%2

; *** INI errors
ErrorIniEntry=在文件 "%1" 中创建 INI 条目时出错。

; *** File copying errors
FileAbortRetryIgnoreSkipNotRecommended=跳过这个文件(&S)（不推荐）
FileAbortRetryIgnoreIgnoreNotRecommended=忽略错误并继续(&I)（不推荐）
SourceIsCorrupted=源文件已损坏
SourceDoesntExist=源文件 "%1" 不存在
ExistingFileReadOnly2=无法替换已存在的文件，因为它具有只读属性。
ExistingFileReadOnlyRetry=移除只读属性并重试(&R)
ExistingFileReadOnlyKeepExisting=保留此文件(&K)
ErrorReadingExistingDest=尝试读取已存在的文件时出错：
FileExistsSelectAction=选择操作
FileExists2=文件已经存在。
FileExistsOverwriteExisting=覆盖已存在的文件(&O)
FileExistsKeepExisting=保留已存在的文件(&K)
FileExistsOverwriteOrKeepAll=为所有冲突项执行此操作(&D)
ExistingFileNewerSelectAction=选择操作
ExistingFileNewer2=文件已经存在，并且其新于将要安装的文件。
ExistingFileNewerOverwriteExisting=覆盖已存在的文件(&O)
ExistingFileNewerKeepExisting=保留已存在的文件(&K)（推荐）
ExistingFileNewerOverwriteOrKeepAll=为所有冲突项执行此操作(&D)
ErrorChangingAttr=尝试修改文件属性时出错：
ErrorCreatingTemp=尝试在目标目录中创建文件时出错：
ErrorReadingSource=尝试读取源文件时出错：
ErrorCopying=尝试复制文件时出错：
ErrorReplacingExistingFile=尝试替换文件时出错：
ErrorRestartReplace=重启替换失败：
ErrorRenamingTemp=尝试重命名目标目录中的文件时出错：
ErrorRegisterServer=无法注册 DLL/OCX：%1
ErrorRegSvr32Failed=RegSvr32 注册失败，返回代码 %1
ErrorRegisterTypeLib=无法注册类型库：%1

; *** Uninstall display name markings
; used for example as 'My Program (32-bit)'
UninstallDisplayNameMark=%1 (%2)
; used for example as 'My Program (32-bit, All users)'
UninstallDisplayNameMarks=%1 (%2, %3)
UninstallDisplayNameMark32Bit=32位
UninstallDisplayNameMark64Bit=64位
UninstallDisplayNameMarkAllUsers=所有用户
UninstallDisplayNameMarkCurrentUser=当前用户

; *** Post-installation errors
ErrorOpeningReadme=尝试打开自述文件时出错。
ErrorRestartingComputer=安装向导无法重新启动计算机，请您手动重启。

; *** Uninstaller messages
UninstallNotFound=文件 "%1" 已丢失，无法继续进行卸载。
UninstallOpenError=文件 "%1" 无法访问，无法继续进行卸载
UninstallUnsupportedVer=此版本的卸载程序无法识别卸载档案文件 "%1" 的格式，无法继续进行卸载。
UninstallUnknownEntry=在卸载档案中遇到一个未知的条目 (%1)
ConfirmUninstall=您确定想要卸载 %1 及其所有组件吗？
UninstallOnlyOnWin64=该程序只能在 64位 Windows 中进行卸载。
OnlyAdminCanUninstall=该程序需要用户以 管理员身份 进行卸载。
UninstallStatusLabel=正在从您的计算机中卸载 %1，请稍作等待...
UninstalledAll=%1 已顺利地完成了卸载。%n%n期待与您的再次相见。
UninstalledMost=%1 的卸载已完成。%n%n有一些内容无法被删除，您可以手动删除它们。
UninstalledAndNeedsRestart=要完成 [name] 的安装，我们必须重新启动您的计算机。%n%n是否立即重新启动？
UninstallDataCorrupted=文件 "%1" 已损坏，无法继续进行卸载

; *** Uninstallation phase messages
ConfirmDeleteSharedFileTitle=是否删除共享文件？
ConfirmDeleteSharedFile2=以下共享文件可能已经不再被其他程序使用。您想要删除这些文件吗？%n%n如果仍有程序需要使用这些文件，那么在删除文件后，这些程序可能无法正常运行。如果您不确定，请选择“否”，把这些文件保留在系统中并没有什么坏处。
SharedFileNameLabel=文件名称：
SharedFileLocationLabel=位置：
WizardUninstalling=卸载状态
StatusUninstalling=正在卸载 %1 ...

; *** Shutdown block reasons
ShutdownBlockReasonInstallingApp=仍在安装 %1 ...
ShutdownBlockReasonUninstallingApp=仍在卸载 %1 ...

; The custom messages below aren't used by Setup itself, but if you make
; use of them in your scripts, you'll want to translate them.

[CustomMessages]

NameAndVersion=%1 版本 %2
AdditionalIcons=附加快捷方式：
CreateDesktopIcon=创建桌面快捷方式(&D)
CreateQuickLaunchIcon=创建快速启动图标(&Q)
ProgramOnTheWeb=%1 网站
UninstallProgram=卸载 %1
LaunchProgram=立即启动 %1
AssocFileExtension=将文件扩展名 %2 关联到 %1 (&A)
AssocingFileExtension=正在将文件扩展名 %2 关联到 %1 ...
AutoStartProgramGroupDescription=启动组：
AutoStartProgram=自动启动 %1
AddonHostProgramNotFound=无法在所选文件夹中找到 %1。%n%n您仍然要继续吗？
