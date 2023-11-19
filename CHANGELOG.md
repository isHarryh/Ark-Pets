# 更新日志 / CHANGELOG

***目前最新可用版本：***
[<img alt="GitHub latest release" src="https://img.shields.io/github/v/release/isHarryh/Ark-Pets?display_name=tag&label=Version&sort=semver&include_prereleases">](https://github.com/isHarryh/Ark-Pets/releases)

## v2.3
| **新增**      |                                          |
|:------------|:-----------------------------------------|
| [`a31afcf`] | 新增了右键桌宠本体即可弹出菜单的功能。                      |
| [`17d3fde`] | 新增了可以在菜单中切换桌宠形态的功能，现在可以切换拥有多个形态的敌方领袖的形态。 |

| **修复**                 |                                                  |
|:-----------------------|:-------------------------------------------------|
| [`#39`]<br>[`87c2263`] | 进一步修复了有概率出现桌宠本体程序在任务栏中无法隐藏的问题。                   |
| [`17d3fde`]            | 进一步修复了个别敌方角色的部分动作的选择逻辑异常的问题。<br>重构了动画名识别和行为控制系统。 |
| [`#48`]<br>[`b72421a`] | 修复了桌宠在保持坐下动作时拖动会导致异常浮动的问题。<br>重构了缓动控制系统。         |

| **优化**      |                                          |
|:------------|:-----------------------------------------|
| [`0fb103c`] | 优化了 Windows 安装程序的语言本地化（修订了简体中文，新增了繁体中文）。 |
| [`a31afcf`] | 优化了托盘菜单的外观表现。                            |
| [`e046e1c`] | 优化了动画队列的代码逻辑。                            |

| **补丁**                  |                    |
|:------------------------|:-------------------|
| `v2.3.1`<br>[`7a161d3`] | 修复了桌宠的行走动作表现异常的问题。 |

## v2.2
| **新增**      |                                         |
|:------------|:----------------------------------------|
| [`225463d`] | 新增了验证模型资源完整性的功能。                        |
| [`39c89a8`] | 新增了**物理引擎参数调整**的功能，现在可以自定义环境加速度、最移速等参数。 |
| [`8de6ff2`] | 新增了提示条组件，用于软件更新提示、数据集不兼容提示、存储空间不足提示。    |

| **修复**                 |                             |
|:-----------------------|:----------------------------|
| [`#39`]<br>[`e54c6ed`] | 修复了有概率出现桌宠本体程序在任务栏中无法隐藏的问题。 |

| **优化**                     |                                               |
|:---------------------------|:----------------------------------------------|
| [`557c09a`]                | 优化了模型资源列表的代码逻辑，移除了不安全的泛型用法。                   |
| [`39c89a8`]                | 优化了滑动条的外观和代码逻辑，添加了数值后单位的显示。                   |
| [`3f713ae`]<br>[`7da5bbf`] | 优化了模型资源管理的代码逻辑，适配了新版的模型库格式。<br>变更了配置文件中的部分字段。 |

| **补丁**                  |                           |
|:------------------------|:--------------------------|
| `v2.2.1`<br>[`2d76421`] | 修复了个别敌方角色的部分动作的选择逻辑异常的问题。 |
| `v2.2.1`<br>[`9c0edf9`] | 优化了提示条组件的外观和鼠标指针表现。       |
| `v2.2.1`<br>[`6fbcac2`] | 优化了按钮和侧边滚动条的鼠标指针表现。       |

## v2.1
| **新增**                |                                                                                                 |
|:----------------------|:------------------------------------------------------------------------------------------------|
| [`#4`]<br>[`5d024f9`] | 新增了对**多显示屏**的支持，现在可以将桌宠拖拽到扩展显示屏上。<br>重构了重力场系统。                                                  |
| [`627d16d`]           | 新增了首次使用软件时的一个提示弹窗。                                                                              |
| [`04a459c`]           | 新增了[思源黑体](https://github.com/adobe-fonts/source-han-sans)作为启动器界面的字体，不再使用系统默认字体，解决了部分设备上的字体渲染问题。 |
| [`2aef47e`]           | ~~新增了当软件有可用更新时，自动高亮显示“检查更新”按钮的特性。~~                                                             |

| **修复**      |                                 |
|:------------|:--------------------------------|
| [`352eca6`] | 修复了在特定情况下打开启动器卡在 Loading 界面的问题。 |
| [`86d7227`] | 修复了个别敌方角色的部分动作的选择逻辑异常的问题。       |

| **优化**      |                                                                                                   |
|:------------|:--------------------------------------------------------------------------------------------------|
| [`86a5450`] | 将 Java 版本从 JDK20 降级到 JDK17，以防止在[特定情况下](https://github.com/libgdx/libgdx/issues/7142)发生 JVM crash。 |
| [`352eca6`] | 优化了模型下载源选择的逻辑，现在会将发生过错误的下载源列入低优先级。                                                                |

| **补丁**                             |                                                                       |
|:-----------------------------------|:----------------------------------------------------------------------|
| `v2.1.1`<br>[`f35f678`]            | 移除了配置文件中的无用字段。                                                        |
| `v2.1.1`<br>[`da9e067`]            | 修复了进行“检查模型库更新”操作时，所有已启动的桌宠会异常退出的问题。<br>启动器中以“GitHub 仓库”替换掉了“开源信息”超链接。 |
| `v2.1.1`<br>[`#34`]<br>[`1e231c9`] | 修复了在多显示器的屏幕边缘未对齐的情况下，桌宠可以异常进入非屏幕区域的问题。                                |

## v2.0
| **新增**                                                   |                                                                                                                           |
|:---------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------|
| [`95e6a1a`]<br>[`2471d2a`]<br>[`95186b6`]<br>[`f92eb75`] | 新增了**全新的启动器界面**，使用 [JavaFX](https://openjfx.io) 重构了所有的 UI 。<br>新增了按照关键字**搜索模型**、随机选取模型的功能。<br>新增了联网检查模型库更新、**联网下载模型**的功能。 |
| [`fd185fe`]                                              | 新增了由 [@Auroal-dawn](https://github.com/bicaoluoshuang) 绘制的**全新的软件图标**。                                                    |
| [`99af0a7`]                                              | 新增了命令行日志等级可选参数 。                                                                                                          |
| [`#4`]<br>[`#12`]<br>[`b6ef359`]                         | 新增了**可重复启动桌宠**的特性，现在启动器不会在启动单个桌宠后立即关闭，可以多次启动桌宠。                                                                           |
| [`bdea621`]                                              | 新增了将绝大多数**敌方模型作为桌宠启动**的功能。                                                                                                |
| [`a6be480`]<br>[`8860930`]                               | 新增了自动选择模型库下载源的功能。                                                                                                         |
| [`01e962e`]<br>[`23dc3cc`]                               | 新增了在桌宠启动失败后弹出错误提示的功能。                                                                                                     |
| [`c0c3c33`]<br>[`5b5970c`]                               | 新增了按照角色类型**筛选模型**的功能。                                                                                                     |
| [`#4`]<br>[`23dc3cc`]                                    | 新增了**开机自启动的功能**，现在可以在开机时自动生成上一次启动的桌宠。<br>新增了加载中页面，UI操作更加舒适。                                                               |

| **修复**                               |                    |
|:-------------------------------------|:-------------------|
| [`#4`]<br>[`4aa567b`]<br>[`99423d4`] | 缓解了角色的渲染超出窗口边界的问题。 |

| **优化**                                    |                                                                                                                                                                                                                                          |
|:------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [`2bc0079`]<br>[`48ef339`]                | 优化了 Spine 运行时库的引入方式，从源码内置更改为 Gradle 引入。<br>优化了开发所用的 IDE ，从 VScode 更改为 [IntelliJ](https://www.jetbrains.com/idea) 。                                                                                                                       |
| [`afa9b03`]<br>[`a134dd4`]<br>[`abc3cad`] | 优化了**软件分发与自动化构建**，现在分发的二进制文件包括 `exe` `zip` `jar` 。运行时映像的打包方式从 exe4j 更改为 [jlink](https://docs.oracle.com/en/java/javase/18/docs/specs/man/jlink.html)+[jpackage](https://docs.oracle.com/en/java/javase/18/docs/specs/man/jpackage.html)。 |
| [`fa866d1`]<br>[`8860930`]                | 优化了**日志系统**，现在会自动写入日志到文件中，并且可记录日志等级。                                                                                                                                                                                                     |
| [`bf904b7`]                               | 微调了干员基建小人和敌方小人切换动作的随机触发权重。                                                                                                                                                                                                               |
| [`c9866a1`]                               | ~~将 Java 版本从 JDK18 升级到 JDK20 ；~~ 将 Gradle 版本从 7.5 升级到 8.1 。                                                                                                                                                                              |

| **补丁**                  |                                                |
|:------------------------|:-----------------------------------------------|
| `v2.0.1`<br>[`21f2eba`] | 进一步缓解了角色的渲染超出窗口边界的问题。<br>修复了特定情况下日志系统逻辑不正确的问题。 |
| `v2.0.1`<br>[`f6e87f9`] | 优化了托盘图标标题，现在标题包含角色名称。<br>变更了配置文件中的部分字段。        |

-----
## v1.6
#### 新增
1. [`a4267c6`] 新增了重力场系统对于点电荷静电斥力的支持。
2. [`#19`]/[`a4267c6`] 由上一条实现：新增了多个小人重叠时可以被排斥开的特性。
3. [`40a57e7`] 新增了可操作的任务栏**托盘图标**。
4. [`#9`]/[`40a57e7`] 新增了右键托盘图标可锁定小人当前动作的功能。

#### 修复
1. [`4954639`] 修复了在小人靠近屏幕左侧时无法站立在窗口上的问题。

#### 优化
1. [`3fd0d36`] ~~启动器中以“帮助”按钮替换掉了原来的“使用手册”按钮。~~
2. [`#3`]/[`4954639`] 任务栏常驻程序窗口现在可以隐藏；桌宠可以跨桌面显示。
3. 优化了悬空状态和落地状态触发相关动作的判定阈值。

-----
## v1.5
#### 修复
1. [`#15`]/[`afa7bb9`] 修复了小人在窗口上缘与屏幕上缘距离小于应用高度时显示异常的问题。
2. [`#13`]/[`61908a0`] 修复了在部分设备上由内存抖动引发的卡顿问题，大幅度降低了性能消耗。

#### 优化
1. [`67a0c66`] 不再以较高频率获取窗口句柄，一定程度降低了性能消耗。
2. [`67a0c66`] 不再在非必要时重设窗口的位置，一定程度降低了性能消耗。
3. 优化了小人的缩放相关常量，略微缓解了小人图像超出窗口边界的问题。
4. 优化了小人的自定义缩放设置项，已额外支持 0.75/1.25/1.5 倍的图像缩放。

#### 补丁
1. `v1.5.1`/[`#12`]/[`d5f0bf0`] 新增了命令行启动参数 `--direct-start` 用于直接启动桌宠而不打开启动器。
2. `v1.5.1`/[`c996b38`] 修复了不支持一星小车的基建小人模型的问题。
3. `v1.5.2`/[`#19`]/[`76e6883`] 新增了小人自动行走至窗口边缘后翻转朝向的特性。
4. `v1.5.2`/[`#19`]/[`a7eba09`] 新增了抛出小人后，使小人立即面向抛掷方向的特性。

-----
## v1.4
#### 新增
1. [`ae979eb`] 新增了重力场系统对于一维障碍物的支持。
2. [`d31f49b`] 由上一条实现：新增了小人可以**站立在打开的窗口的顶部**的特性。

#### 修复
1. [`aeed29a`] 修复了基建小人的 Sit 动作的渲染偏移受图像缩放的影响被放大的问题。
2. [`#10`]/[`aeed29a`] 修复了基建小人的 Sit 动作的窗口垂直位置不能低于屏幕下边界，导致在某些情况下浮空的问题。

#### 优化
1. 不再使用 [EVB](https://lifeinhex.com/tag/enigma-virtual-box) 封装 `exe` 版的Release，现在采用 [InnoSetup](https://jrsoftware.org/isinfo.php) 来封装 `exe`。
2. 微调了重力场系统的质点最大速度限制。
3. 微调了重力场系统的质点完全失重判定规则。

-----
## v1.3
#### 修复
1. [`#3`]/[`9648fe3`] 修复了模型在空中自由落体时开始行走会使其做匀速直线运动的问题。
2. [`#5`]/[`9648fe3`] 修复了小人在空中被右键后不会落下的问题。
3. [`#2`]/[`feaa6fa`] 修复了基建小人在 Sit 和不可打断动作 (例如 Special ) 同时进行时，窗口的垂直位置表现不正常的问题。
4. [`feaa6fa`] 修复了基建小人的 Sit 动作的窗口垂直偏移值不受图像缩放影响的问题。
5. [`#3`]/[`bcbe4cb`] 修复了多个模型堆叠时持续闪烁的问题。

-----
## v1.2
#### 新增
1. [`ff15314`] 新增了摔落动作，以搭配平面重力场使用，同时也移除了拖拽结束动作。
2. [`11b0977`] 新增了**平面重力场系统**的模拟实现。
3. [`11b0977`] 新增了主页面的“下边界距离”调节滑块。

-----
## v1.1
#### 新增
1. [`4603ab0`] ~~新增了主界面的“使用手册”按钮，点击后可跳转到 GitHub 仓库主页。~~

#### 修复
1. [`ff79dbd`] 修复了基建小人在 Interact 和 Sit 同时进行时，窗口的垂直位置表现不正常的问题。
2. [`ff79dbd`] 修复了窗口可以离开屏幕右下边界的问题，现在窗口将被严格限制在屏幕内。

<!-- Links to v1.x References -->
[`#2`]: https://github.com/isHarryh/Ark-Pets/issues/2
[`#3`]: https://github.com/isHarryh/Ark-Pets/issues/3
[`#5`]: https://github.com/isHarryh/Ark-Pets/issues/5
[`#9`]: https://github.com/isHarryh/Ark-Pets/issues/9
[`#10`]: https://github.com/isHarryh/Ark-Pets/issues/10
[`#12`]: https://github.com/isHarryh/Ark-Pets/issues/12
[`#13`]: https://github.com/isHarryh/Ark-Pets/issues/13
[`#15`]: https://github.com/isHarryh/Ark-Pets/issues/15
[`#19`]: https://github.com/isHarryh/Ark-Pets/issues/19
[`a4267c6`]: https://github.com/isHarryh/Ark-Pets/commit/a4267c68ddca1bf1cf994e2748d0986ef38a2140
[`40a57e7`]: https://github.com/isHarryh/Ark-Pets/commit/40a57e7f279bfce96d2e6b651bc0b9ba5c0104bf
[`3fd0d36`]: https://github.com/isHarryh/Ark-Pets/commit/3fd0d36f0be38fa31563aebf7ef2303c4e917f42
[`4954639`]: https://github.com/isHarryh/Ark-Pets/commit/4954639d623608dfd0443790e786176d57ff2212
[`afa7bb9`]: https://github.com/isHarryh/Ark-Pets/commit/afa7bb94cd46c1d51725f4bb58b7ac462d729bdc
[`61908a0`]: https://github.com/isHarryh/Ark-Pets/commit/61908a0023980a7ff6affee3b8814a77c92585cf
[`67a0c66`]: https://github.com/isHarryh/Ark-Pets/commit/67a0c66b9ec0f713d581d5062e9c0098226b39d0
[`d5f0bf0`]: https://github.com/isHarryh/Ark-Pets/commit/d5f0bf0bae3f1589de5f71aeeb0b5aad82a234b0
[`c996b38`]: https://github.com/isHarryh/Ark-Pets/commit/c996b383e4fddda0acf2362774ec0ecc2a1cb8a6
[`76e6883`]: https://github.com/isHarryh/Ark-Pets/commit/76e68832d6987ef2cb4fd65ad28dc754ef5e4b56
[`a7eba09`]: https://github.com/isHarryh/Ark-Pets/commit/a7eba09b35b320ec24816eccf5d4413e175cc6ba
[`ae979eb`]: https://github.com/isHarryh/Ark-Pets/commit/ae979eb0031b401bc52d44c0d396f12eeba4a64d
[`aeed29a`]: https://github.com/isHarryh/Ark-Pets/commit/aeed29a9bf25db445ef15801a83172e1b84d1ccd
[`d31f49b`]: https://github.com/isHarryh/Ark-Pets/commit/d31f49bf116b836bac6b9d1a2db83f72c216e31a
[`9648fe3`]: https://github.com/isHarryh/Ark-Pets/commit/9648fe3089bb7b11b7693e2f61eed54a598b2023
[`feaa6fa`]: https://github.com/isHarryh/Ark-Pets/commit/feaa6fa5ffad183d1bb14f6b8057a6c5e2ba31c6
[`bcbe4cb`]: https://github.com/isHarryh/Ark-Pets/commit/bcbe4cbea63406ec15c74cd80e7dbaf7cf9ec0f0
[`ff15314`]: https://github.com/isHarryh/Ark-Pets/commit/ff15314933cb17eab210475f326943911f5b3258
[`11b0977`]: https://github.com/isHarryh/Ark-Pets/commit/11b09770582d7e36548021ec844983627db2f163
[`ff79dbd`]: https://github.com/isHarryh/Ark-Pets/commit/ff79dbdaa19e4e9abbadf23fec4e9d43e421bf6f
[`4603ab0`]: https://github.com/isHarryh/Ark-Pets/commit/4603ab020d62f13592e36d771f8525721656970c

<!-- Links to v2.x References -->
[`#4`]: https://github.com/isHarryh/Ark-Pets/issues/4
[`#12`]: https://github.com/isHarryh/Ark-Pets/issues/12
[`#34`]: https://github.com/isHarryh/Ark-Pets/issues/34
[`#39`]: https://github.com/isHarryh/Ark-Pets/issues/39
[`#48`]: https://github.com/isHarryh/Ark-Pets/issues/48
[`2bc0079`]: https://github.com/isHarryh/Ark-Pets/commit/2bc0079b922684b1d4850f9211225dcf803e555c
[`48ef339`]: https://github.com/isHarryh/Ark-Pets/commit/48ef339dd78711e208ded8c5148569d8b89690b1
[`95e6a1a`]: https://github.com/isHarryh/Ark-Pets/commit/95e6a1ace8d047ac51314e7d5572ce4169fa9f84
[`fd185fe`]: https://github.com/isHarryh/Ark-Pets/commit/fd185fe612749d05c2eacfa8b8e285d44c8badfa
[`2471d2a`]: https://github.com/isHarryh/Ark-Pets/commit/2471d2ad236087c60df2bca722d29c7105781144
[`95186b6`]: https://github.com/isHarryh/Ark-Pets/commit/95186b6f92ab69bbe2376a159af3acb0b865fcaa
[`4aa567b`]: https://github.com/isHarryh/Ark-Pets/commit/4aa567ba9695db9f2904c46e3eea4f8cc65531c6
[`99af0a7`]: https://github.com/isHarryh/Ark-Pets/commit/99af0a75968a922cc26f76ec9aab218f28f8a708
[`b6ef359`]: https://github.com/isHarryh/Ark-Pets/commit/b6ef359dcae258cea9fab2fd9bac3fd199bc3ef6
[`bdea621`]: https://github.com/isHarryh/Ark-Pets/commit/bdea6210a6aadd128b24f1fe5e04200252d3710e
[`f92eb75`]: https://github.com/isHarryh/Ark-Pets/commit/f92eb7510d20ed7400645b930642da8dbad0e5b2
[`afa9b03`]: https://github.com/isHarryh/Ark-Pets/commit/afa9b0311eecc5938cf25b1bdd1143c18a8bb5af
[`a134dd4`]: https://github.com/isHarryh/Ark-Pets/commit/a134dd4b15862fa83fceeda93c3528e1303356c5
[`abc3cad`]: https://github.com/isHarryh/Ark-Pets/commit/abc3cadf5374e5a656715cbccf1c1bb704ef8df0
[`99423d4`]: https://github.com/isHarryh/Ark-Pets/commit/99423d46b1037a4e13578ffee30f1d3b8f9cf56d
[`fa866d1`]: https://github.com/isHarryh/Ark-Pets/commit/fa866d11911c10072ecd733828778efbbfc7024a
[`a6be480`]: https://github.com/isHarryh/Ark-Pets/commit/a6be4807aa52e72c49153573d37a59bbc45dc9a7
[`8860930`]: https://github.com/isHarryh/Ark-Pets/commit/88609305cfd61672238896b7ede87ee7377873b9
[`01e962e`]: https://github.com/isHarryh/Ark-Pets/commit/01e962e3ec308ffb46ea67bfff71141f4353b4a9
[`c0c3c33`]: https://github.com/isHarryh/Ark-Pets/commit/c0c3c33cd32752e5767e329ddb478777fd79bbb9
[`23dc3cc`]: https://github.com/isHarryh/Ark-Pets/commit/23dc3ccdcde933af8436ccbe82756d39e50a15a3
[`5b5970c`]: https://github.com/isHarryh/Ark-Pets/commit/5b5970c3094973f0a023d1d5f434c6e0e83a698d
[`bf904b7`]: https://github.com/isHarryh/Ark-Pets/commit/bf904b7614c3005d68dd746dcbb4c0c461cbd938
[`c9866a1`]: https://github.com/isHarryh/Ark-Pets/commit/c9866a16b7ea95b63a44c1d1fc41fce72e81ff27
[`21f2eba`]: https://github.com/isHarryh/Ark-Pets/commit/21f2eba1a775816aae4bcf44a9cbc35d68c8f35e
[`f6e87f9`]: https://github.com/isHarryh/Ark-Pets/commit/f6e87f9231664d040f51051ad5eaafc8ace82297
[`86a5450`]: https://github.com/isHarryh/Ark-Pets/commit/86a5450983a1f5b2487b407201b3f6a08cbdf1e1
[`352eca6`]: https://github.com/isHarryh/Ark-Pets/commit/352eca6a35340b4ee08c2b37c04a077249583af9
[`5d024f9`]: https://github.com/isHarryh/Ark-Pets/commit/5d024f911c033d6c94dba3c57652d11cfd83db5f
[`627d16d`]: https://github.com/isHarryh/Ark-Pets/commit/627d16d6978a30b711726d53febc45195cc3f946
[`04a459c`]: https://github.com/isHarryh/Ark-Pets/commit/04a459cb05709919aa5ab368716067a97327132b
[`2aef47e`]: https://github.com/isHarryh/Ark-Pets/commit/2aef47e4a48c23bbeaeb4501b79d54936aaf332e
[`86d7227`]: https://github.com/isHarryh/Ark-Pets/commit/86d722719b8183d30a23c52998d5073ac582e84a
[`f35f678`]: https://github.com/isHarryh/Ark-Pets/commit/f35f678af4cd26760e2d7ade3d6e6c14dc404156
[`da9e067`]: https://github.com/isHarryh/Ark-Pets/commit/da9e06782e04079b49e48d335dce53cb015d436f
[`1e231c9`]: https://github.com/isHarryh/Ark-Pets/commit/1e231c9c8d140548256f5e10f54d0cd9d5f66d48
[`557c09a`]: https://github.com/isHarryh/Ark-Pets/commit/557c09a04ae9363808a2e2948e9b8481aaca0583
[`225463d`]: https://github.com/isHarryh/Ark-Pets/commit/225463d347d1afc29e86153b1172dfb5aa753b9a
[`39c89a8`]: https://github.com/isHarryh/Ark-Pets/commit/39c89a8a66f841c2150a4407c4be7df7893e2543
[`3f713ae`]: https://github.com/isHarryh/Ark-Pets/commit/3f713ae4b9c5ff449c563e0b12790c4a3f0bb15b
[`7da5bbf`]: https://github.com/isHarryh/Ark-Pets/commit/7da5bbf55442b3cc357403cb83e01c8425b7534e
[`e54c6ed`]: https://github.com/isHarryh/Ark-Pets/commit/e54c6ed556a5abe59197e0dacf7717c0bc7a0120
[`8de6ff2`]: https://github.com/isHarryh/Ark-Pets/commit/8de6ff2ca4c9de0e59eb60380e5bfa2c7f296b82
[`2d76421`]: https://github.com/isHarryh/Ark-Pets/commit/2d76421016d7e4629afcb9699793a24c626c274c
[`9c0edf9`]: https://github.com/isHarryh/Ark-Pets/commit/9c0edf93dbc193b9e10f7c8caa7306d95fef873e
[`6fbcac2`]: https://github.com/isHarryh/Ark-Pets/commit/6fbcac231fd9964c5bd61089e6307d350c825f8d
[`0fb103c`]: https://github.com/isHarryh/Ark-Pets/commit/0fb103c0f9aa6e5181242a11bf616fc8e439e42e
[`87c2263`]: https://github.com/isHarryh/Ark-Pets/commit/87c226315f4fe84c300d3403258f5e68bed67f92
[`a31afcf`]: https://github.com/isHarryh/Ark-Pets/commit/a31afcfd38e5cbc8a7e1bcde6c3e3dd72e281ad3
[`17d3fde`]: https://github.com/isHarryh/Ark-Pets/commit/17d3fded56a6b92b4dabe62945897b8b7df1514b
[`e046e1c`]: https://github.com/isHarryh/Ark-Pets/commit/e046e1c67ccbd61cde7e50927eccf9c20c7ee736
[`b72421a`]: https://github.com/isHarryh/Ark-Pets/commit/b72421a90b9263c6f25fe76053f139ffa445a981
[`7a161d3`]: https://github.com/isHarryh/Ark-Pets/commit/7a161d304f4256d0dfa5f027fad1479ac0d06391
