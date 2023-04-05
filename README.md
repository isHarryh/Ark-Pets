<!-- 欢迎阅读 Ark-Pets 说明文档 -->
<!-- 仓库：https://github.com/isHarryh/Ark-Pets -->

<div align="center">
   <h1> Ark-Pets </h1>
   <img height="64" src="https://raw.githubusercontent.com/isHarryh/Ark-Pets/v2.x/assets/icon.png" title="Icon" width="64"/>
   <p>
      Arknights Desktop Pets | 明日方舟桌宠 <br>
      <code><b> v2.0 </b></code> <br>
      <sub>
         <i> This project only supports Chinese docs. If you are an English user, feel free to contact us. </i>
      </sub>
   </p>
</div>

## 介绍 <sub>Intro</sub>
### 实现的功能
- 支持自定义选择、按关键词搜索明日方舟小人模型
- 支持在启动器中显示模型的详细信息，调整缩放和帧率等设置
- 实现了在启动器中检查模型更新、一键下载模型文件等联网功能
- 支持模拟游戏内 **干员基建小人** 的行为 <details><summary>查看详情</summary>
   1. 基建小人能够执行行走和坐下的动作
   2. 基建小人能够被鼠标交互 (执行戳一戳动作)
   3. 基建小人能够执行基建动作 (如有)
- 支持模拟游戏内 **敌方小人** 的行为 <details><summary>查看详情</summary>
   1. 敌方小人能够执行行走动作
   2. 敌方小人能够被鼠标交互 (执行攻击动作)
- 实现了模拟平面重力场 <details><summary>查看详情</summary>
   1. 桌宠在被拖拽到空中后能够自由落体
   2. 桌宠可以站立在打开的窗口的边缘上
   3. 桌宠会受地面摩擦力和空气阻力作用
   4. 桌宠会在其他桌宠靠近时被排斥推动
   5. 桌宠活动范围的[下边界距离](#额外说明)可以调整
- 实现了任务栏托盘图标 <details><summary>查看详情</summary>
   1. 托盘菜单可以选择是否保持当前动作
   2. 托盘菜单可以选择是否启用[透明模式](#额外说明)
   2. 托盘菜单可以用于退出桌宠
- 支持[开机自启动](#额外说明)

### 下一步计划
以下内容可能在接下来的数个版本内得到实现：
- 支持部分敌方首领的阶段形态切换

### 相关文档
- 更新日志 > [点击查看](CHANGELOG.md)
- 常见疑问解答 > [点击查看](docs/Q%26A.md)
- 命令行启动方法 > [点击查看](docs/CmdLine.md)


## 使用方法 <sub>Usage</sub>
> 由于程序中部分API不具备跨平台兼容性，本项目当前只支持在`Windows`系统上运行。

目前最新可用版本：
[<img alt="GitHub latest release" src="https://img.shields.io/github/v/release/isHarryh/Ark-Pets?display_name=tag&label=Version&sort=semver&include_prereleases">](https://github.com/isHarryh/Ark-Pets/releases)

### 快速上手
1. 请[前往页面](https://github.com/isHarryh/Ark-Pets/releases)下载最新的 **ArkPets-Setup.exe** 安装包。
2. 下载完成后，请运行所下载的安装包，根据安装向导的提示进行软件安装。（如果需要更新软件，无需手动卸载后再安装，直接运行安装包即可更新软件）
3. 安装完成后，请通过桌面快捷方式等途径，打开 ArkPets 启动器。
4. 打开启动器后，首次使用需要下载模型文件。请进入启动器“选项”页面，在“模型下载“处点击”全部下载“。（下载过程可能持续数分钟，下载总大小约为120+MB）
5. 最后，进入启动器“模型”页面即可检索并选中想要作为桌宠启动的角色，然后点击左下角“启动”按钮即可。

### 额外说明
- **检查模型库更新**：我们的模型库不定时更新，如果您想体验新实装进游戏的模型，可以进入启动器“选项“页面，在“模型下载“处点击”检查更新“。如果提示有更新，点击”全部下载“就能完成模型库更新。
- **开机自启动**：进入启动器“选项”页面可以设置开机自启动，设置后下一次电脑开机会自动生成最后一次启动的桌宠。
- **透明模式**：为防止用户在游戏、观看视频等情景下误触到桌宠，特增加了此模式。右键托盘后打开“透明模式”，即可屏蔽桌宠和鼠标的一切交互（点击、拖动操作都将穿透到下层窗口），并且桌宠的不透明度会降低。
- **下边界距离**：桌宠在部分用户的电脑上无法正常检测任务栏位置（桌宠会沉入任务栏），此时您可以手动设置任务栏高度。进入启动器“选项”页面可以调整下边界距离，通常会将其设置为15的正整数倍。

### 高级用法
在[快速上手](#快速上手)中介绍的是最简单和普遍的使用方法。除此之外：
- 您还可以下载 `zip` 版的程序压缩包解压，实现免安装使用。
- 如果您的电脑上存在 `JDK18+` 的 [Java](https://www.java.com) 运行环境，您还可以下载 `jar` 版的程序文件直接运行（但无法使用开机自启动功能）。
- 如果您想用命令行方式启动桌宠，[点击查看](docs/CmdLine.md)说明。


## 许可证 <sub>License</sub>
本项目基于**GPL3协议**。任何人都可以自由地使用和修改项目内的源代码，前提是要在源代码或版权声明中保留作者说明和原有协议，且使用相同的许可证进行开源。

### 参与贡献
您可以通过提交Issues、PR、CodeReview等方式参与本项目的贡献。
- 已有Issues摘要 > [点击查看](docs/Issues.md#已有议题)
- 提交Issues必看 > [点击查看](docs/Issues.md#议题规范)
- **[鸣谢](docs/Thanks.md)**
<!--- 开发者Wiki > [点击查看](https://github.com/isHarryh/Ark-Pets/wiki) -->
