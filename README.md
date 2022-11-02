Ark-Pets
==========
Arknights Desktop Pets.  
明日方舟桌宠  
版本(Version)`v1.5`

This project only supports Chinese docs. If you are an English user, feel free to contact us.

## 介绍 <sub>Intro</sub>
### 实现的功能
1. 支持自定义选择明日方舟小人模型
2. 支持在选择小人时展示其模型预览
3. 支持调整桌宠缩放倍率和最大帧率
4. 支持模拟游戏内基建小人的行为 <details><summary>查看详情</summary>
    1. 基建小人能够在水平方向行走
    2. 基建小人能够执行坐下的动作
    3. 基建小人能够被鼠标点击交互
    4. 基建小人能够被鼠标拖拽移动
    5. 基建小人能够执行基建动作(如有)
    5. 基建小人能够在摔落时播放交互动画
    </details>
5. 支持模拟平面重力场 <details><summary>查看详情</summary>
    1. 桌宠在被拖拽到空中时能够自由落体
    2. 桌宠可以站立在打开的窗口的边缘上(NEW)
    3. 桌宠会受地面摩擦力和空气阻力作用
    4. 桌宠活动范围的下边界距离可以调整
    </details>

### 下一步计划
以下内容可能在接下来的数个版本内得到实现：
1. 支持模拟游戏内敌方小人的行为
2. 支持搜索模型名称

### 相关文档
- 更新日志 > [点击查看](CHANGELOG.md)
- 常见疑问解答 > [点击查看](docs/Q%26A.md)
- 已有Issues摘要 > [点击查看](docs/Issues.md#已有议题)
- 提交Issues必看 > [点击查看](docs/Issues.md#议题规范)

## 使用方法 <sub>Usage</sub>
由于程序中部分API不具备跨平台兼容性，本项目当前只支持在`Windows`系统上运行。

### 1.下载程序
<details><summary>A.我想通过Java运行环境运行</summary>

1. **配置环境**：配置`Java运行环境(JRE)`，如已配置请跳过此步。您需要在[Java官网下载](https://www.java.com/download)JRE安装包，然后根据安装包的提示完成安装。
2. **下载ArkPets.jar**：[前往这里](https://github.com/isHarryh/Ark-Pets/releases)下载`.jar`格式的程序文件。
</details>

<details><summary>B.我不想安装Java运行环境或方法A不行</summary>

1. **下载ArkPets_Setup.exe**：[前往这里](https://github.com/isHarryh/Ark-Pets/releases)下载`.exe`格式的程序文件。
2. **安装**：根据上面下载的安装包的提示完成安装。
</details>

### 2.模型获取
下载安装后，您需要获取明日方舟Spine动画小人(下简称“模型”)文件：本项目不会内置这些模型文件，您需要自己获取模型文件以供程序使用，流程如下：
1. 作者已将本项目支持的所有模型文件上传至GitHub仓库，您可以直接前往下载：[前往仓库](https://github.com/isHarryh/Ark-Models)，该仓库不定期更新；
2. 下载压缩包后，解压其中的文件夹`models`。
3. 想要了解其他获取模型的方法，请看[这里](docs/GetModels.md)。
4. 将模型文件夹放到<u>程序所在目录</u>中，并<u>确保</u>你的文件夹结构如下：
> 根目录  
> ├─ Models (存放模型文件)  
> │  ├─ 002_amiya (一套模型由一个文件夹单独存放)  
> │  │  ├─ xxxx.atlas  
> │  │  ├─ xxxx.png  
> │  │  └─ xxxx.skel  
> │  ├─ 003_kalts  
> │  ├─ 009_12fce  
> │  └─ ……  
> └─ ArkPets.jar (ArkPets主程序)  
5. 运行程序文件即可开始使用，如有任何问题可[查阅文档](#相关文档)。

## 许可证 <sub>License</sub>
本项目基于**GPL3协议**。任何人都可以自由地使用和修改项目内的源代码，前提是要在源代码或版权声明中保留作者说明和原有协议，且使用相同的许可证进行开源。
