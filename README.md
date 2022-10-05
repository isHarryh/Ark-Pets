Ark-Pets
==========
Arknights Desktop Pets.  
明日方舟桌宠  
版本(Version)`v1.2`

This project only supports Chinese docs. If you are an English user, feel free to contact us.

## 介绍 <sub>Intro</sub>
#### 实现的功能
1. 支持自定义选择明日方舟小人模型
2. 支持在选择小人时展示其模型预览
3. 支持调整桌宠缩放倍率和最大帧率
4. 支持模拟游戏内基建小人的行为：
    1. 基建小人能够在水平方向行走
    2. 基建小人能够执行坐下的动作
    3. 基建小人能够被鼠标点击交互
    4. 基建小人能够被鼠标拖拽移动
    5. 基建小人能够执行基建动作(如有)
    5. 基建小人能够在摔落时播放交互动画
5. 支持模拟平面重力场(NEW)
    1. 桌宠在被拖拽到空中时能够自由落体
    2. 桌宠会受地面摩擦力和空气阻力作用
    3. 桌宠活动范围的下边界距离可以调整

#### 下一步计划
以下内容可能在接下来的数个版本内得到实现：
1. 支持模拟游戏内敌方小人的行为

#### 更新日志
[查看CHANGELOG](CHANGELOG.md)

#### 已有议题
提交Issue前必看[这里](docs/Issues.md)

## 使用方法 <sub>Usage</sub>
由于程序中部分API不具备跨平台兼容性，本项目当前只支持在`Windows`系统上运行。
#### 详细步骤
1. 配置运行环境：本项目使用`Java`开发，您首先需要配置`Java运行环境(JRE)`，如已配置请跳过此步。您可以网络搜索`JRE环境配置`找到相关教程，通常来说流程如下：
    1. 官网下载JRE安装包：[前往页面](https://www.java.com/download)；
    2. 运行安装包并根据提示完成安装。
2. 下载ArkPets：[前往页面](https://github.com/isHarryh/Ark-Pets/releases)下载`.jar`格式的程序文件。
3. 获取明日方舟Spine动画小人(下简称“模型”)文件：本项目不会内置这些模型文件，您需要自己下载或解包模型文件以供程序使用：
    1. 方法一(推荐)：
        1. 作者已将本项目支持的所有模型文件上传至GitHub仓库，您可以直接前往下载：[前往页面](https://github.com/isHarryh/Ark-Models)，该仓库不定期更新；
        2. 下载压缩包后，解压其中的文件。
    2. 方法二(较为复杂)：
        1. 使用[ArkUnpacker](https://github.com/isHarryh/Ark-Unpacker)之类的解包工具解包出游戏资源文件；
        2. 然后从里面筛选模型文件出来，一套模型文件包括`.png`、`.atlas`、`.skel`三种文件。
4. 将模型文件放到指定目录中：确保你的文件夹结构如下：
> 根目录  
> ├─ Models (存放模型文件)  
> │  ├─ 002_amiya (一套模型一个文件夹单独存放)  
> │  │  ├─ xxxx.atlas  
> │  │  ├─ xxxx.png  
> │  │  └─ xxxx.skel  
> │  ├─ 003_kalts  
> │  ├─ 009_12fce  
> │  ├─ ……  
> └─ ArkPets.jar (ArkPets主程序)  
5. 运行`.jar`文件即可。

#### 备用步骤
如果您使用上述步骤还是无法使用ArkPets，最快的解决方法是换一条路走：使用我们的`.exe`文件代替`.jar`（没错，这种方法无需安装`Java运行环境(JRE)`）。  
[前往页面](https://github.com/isHarryh/Ark-Pets/releases)下载`.exe`格式的程序文件。但毕竟是替代方案，其整体性能还是逊于原始方案的。

#### 常见问题解答
[前往页面](docs/Q%26A.md)

## 许可证 <sub>License</sub>
本项目基于**GPL3协议**。任何人都可以自由地使用和修改项目内的源代码，前提是要在源代码或版权声明中保留作者说明和原有协议，且使用相同的许可证进行开源。
