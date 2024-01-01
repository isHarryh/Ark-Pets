<!-- 欢迎阅读 Ark-Pets 说明文档 -->
<!-- 仓库：https://github.com/isHarryh/Ark-Pets -->

<!--suppress HtmlDeprecatedAttribute -->
<div align="center" style="text-align:center">
   <h1> Ark-Pets </h1>
   <img alt="ArkPets icon" width="64" src="https://raw.githubusercontent.com/isHarryh/Ark-Pets/v2.x/assets/icons/icon.png"/>
   <p>
      Arknights Desktop Pets | 明日方舟桌宠 <br>
      <code><b> v3.0 </b></code>
   </p>
   <p>
      <img alt="GitHub Top Language" src="https://img.shields.io/github/languages/top/isHarryh/Ark-Pets?label=Java">
      <img alt="GitHub License" src="https://img.shields.io/github/license/isHarryh/Ark-Pets?label=License"/>
      <img alt="GitHub Workflow Status" src="https://img.shields.io/github/actions/workflow/status/isHarryh/Ark-Pets/build.yml?label=Build">
   </p>
   <sub>
      <i> This project only supports Chinese docs. If you are an English user, feel free to contact us. </i>
   </sub>
</div>

## 介 绍 <sub>Intro</sub>

### 实现的功能

- 支持自定义选择、按关键词搜索明日方舟小人模型
- 支持在启动器中显示模型的详细信息，调整缩放和帧率等设置
- 实现了在启动器中下载模型文件、检查模型库更新等联网功能
- 支持模拟游戏内 **干员基建小人** 的行为 <details><summary>查看详情</summary>
    1. 能够执行行走和坐下的动作
    2. 能够被鼠标交互 (执行戳一戳动作)
    3. 能够执行基建动作 (如有)
- 支持模拟游戏内 **敌方小人** 的行为 <details><summary>查看详情</summary>
    1. 能够执行行走动作
    2. 能够被鼠标交互 (执行攻击动作)
- 实现了模拟平面重力场 <details><summary>查看详情</summary>
    1. 桌宠在被拖拽到空中后能够自由落体
    2. 桌宠可以站立在打开的窗口的边缘上
    3. 桌宠会受地面摩擦力和空气阻力作用
    4. 桌宠会在其他桌宠靠近时被排斥推动
    5. 桌宠活动范围的[下边界距离](#额外说明)可以调整
    6. 桌宠可以被拖拽到扩展显示屏上
- 实现了托盘和右键菜单 <details><summary>查看详情</summary>
    1. 右键系统托盘或桌宠本体均可以弹出菜单
    2. 菜单可以选择是否保持当前动作
    3. 菜单可以选择是否启用[透明模式](#额外说明)
    4. 菜单可以切换多形态角色的形态
    5. 菜单可以用于退出桌宠
- 支持[开机自启动](#额外说明)

### 效果预览图

<table style="margin-left: auto; margin-right: auto;">
    <tr>
        <td> <img alt="demo1" width="250" src="https://raw.githubusercontent.com/isHarryh/Ark-Pets/v2.x/docs/imgs/demo_1.png"> </td>
        <td> <img alt="demo2" width="250" src="https://raw.githubusercontent.com/isHarryh/Ark-Pets/v2.x/docs/imgs/demo_2.png"> </td>
        <td> <img alt="demo3" width="250" src="https://raw.githubusercontent.com/isHarryh/Ark-Pets/v2.x/docs/imgs/demo_3.png"> </td>
    </tr>
</table>

### 下一步计划

以下内容将在 ArkPets v3.x 系列实现：

- 支持高级筛选和收藏夹功能
- 支持将动态立绘作为桌宠启动
- 全面更新依赖库的版本

以下内容仍需进一步的探索：

- 覆写默认着色器和光照系统
- 支持透明模式等配置的记忆
- 支持一键关闭所有已被启动的桌宠

### 相关文档

- 更新日志 > [点击查看](CHANGELOG.md)
- 常见问题解答 > [点击查看](docs/FAQ.md)

## 使用方法 <sub>Usage</sub>

***目前最新可用版本：***
[<img alt="GitHub latest release" src="https://img.shields.io/github/v/release/isHarryh/Ark-Pets?display_name=tag&label=Version&sort=semver&include_prereleases">](https://github.com/isHarryh/Ark-Pets/releases)

### 快速上手

1. 请[**前往此页面**](https://github.com/isHarryh/Ark-Pets/releases)下载最新的 **ArkPets-Setup.exe** 安装包。
2. 下载完成后，请运行所下载的安装包以进行软件的安装。
3. 安装完成后，请通过桌面快捷方式等途径，打开 ArkPets 启动器。
4. 打开启动器后，首次使用需要 **下载模型文件** 。请进入启动器“选项”页面，在“模型下载”处点击“全部下载”。
5. 最后，进入启动器“模型”页面即可检索并选中想要作为桌宠启动的角色，然后点击左下角“启动”按钮即可。

> 提示：
> - 本程序当前只支持在`Windows`系统上运行。
> - 如需更新软件到 v2.x 的更高版本，无需预先手动卸载，直接运行安装包即可。
> - 如需关闭已启动的桌宠，请右键点击系统托盘中的 ArkPets 图标，然后选择“退出”。

### 额外说明

- *检查模型库更新* ：我们的模型库不定期更新，如果您想体验新实装进游戏的模型，可以进入启动器“选项”页面，在“模型下载”处点击“检查更新”。如果提示有更新，点击“全部下载”就能完成模型库更新。
- *开机自启动* ：进入启动器“选项”页面可以设置开机自启动，设置后下一次电脑开机会自动生成最后一次启动的桌宠。
- *透明模式* ：为防止用户在游戏、观看视频等情景下误触到桌宠，特增加了此模式。右键托盘后打开“透明模式”，即可屏蔽桌宠和鼠标的一切交互（点击、拖动操作都将穿透到下层窗口），并且桌宠的不透明度会降低。
- *下边界距离* ：桌宠在部分用户的电脑上无法正常检测任务栏位置（桌宠会沉入任务栏），此时您可以手动设置任务栏高度。进入启动器“选项”页面可以调整下边界距离，通常会将其设置为15的正整数倍。

### 高级用法

在[快速上手](#快速上手)中介绍的是最简单和普遍的使用方法。除此之外：

- 您还可以下载 `zip` 版的程序压缩包解压，实现免安装使用。
- 如果您的电脑上存在 `JDK17` 的 [Java](https://www.java.com) 运行环境，您还可以下载 `jar` 版的程序文件直接运行（但无法使用开机自启动功能）。
- 如果您想用命令行启动桌宠，[点击查看](docs/CmdLine.md)说明。
- 如果您想添加自定义的模型，[点击查看](docs/CustomModel.md)说明。

## 关 于 <sub>About</sub>

### 鸣谢

感谢所有曾经为 ArkPets 的开发提供过各种形式的帮助的个人和组织。

- 另见 [鸣谢和第三方库说明](docs/Thanks.md)

### 许可证

本项目基于**GPL3协议**。任何人都可以自由地使用和修改项目内的源代码，前提是要在源代码或版权声明中保留作者说明和原有协议，且使用相同的许可证进行开源。

### 参与贡献

您可以通过提交 [Issues](https://github.com/isHarryh/Ark-Pets/issues) 等各种方式参与本项目的贡献。 提交 Issues 前，请确认您的议题与已有的议题不重复。提交 Issues 时，请您完整地填写议题模板。

- 另见 ~~很久没更新的~~ [开发者 Wiki](https://github.com/isHarryh/Ark-Pets/wiki)
