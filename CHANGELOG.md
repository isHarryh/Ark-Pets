# 更新日志 / CHANGELOG

## v3.6
| **新增**                            |                                                       |
|:----------------------------------|:------------------------------------------------------|
| [`#20`]<br>[`aebb7f7`]            | 新增了对干员基建模型**躺下动作**的支持。<br>新增了可以开启或关闭躺下动作和基建特殊动作的行为选项。 |
| [`#88`]<br>[`#99`]<br>[`a43d795`] | 新增了依据**角色名称的汉语拼音**的全拼或首字母来搜索模型的功能。                    |
| [`#99`]<br>[`ce3051d`]            | 新增了中文简体与繁体互通搜索的功能。                                    |
| [`#99`]<br>[`90f0660`]            | 新增了依据**时装品牌名称**来搜索模型的功能。                              |
| [`e06dc6d`]                       | 新增了可以渲染桌宠**阴影**的功能，并可以自定义阴影的强度。                       |

| **修复**                 |                               |
|:-----------------------|:------------------------------|
| [`c95768a`]            | 修复了部分角色在进行坐下动作时，窗口上边界表现异常的问题。 |

| **优化**                 |                                                     |
|:-----------------------|:----------------------------------------------------|
| [`35d4d74`]            | 优化了缓动函数设置的选项显示，并增加了相关说明。                            |
| [`#99`]<br>[`a43d795`] | 优化了搜索模型时的搜索结果排序规则。                                  |
| [`c277dae`]            | 优化了桌宠的渲染偏移（OffsetY）参数，使得某些额外内容得以正常显示（例如对角色脚底的高亮描边）。 |

## v3.5
| **新增**                                |                                         |
|:--------------------------------------|:----------------------------------------|
| [`#86`]<br>[`e998e4a`]<br>[`7042699`] | 新增了**收藏模型**的功能，现在可以对模型进行收藏并在列表中单独显示它们。  |
| [`88ffa1e`]                           | 新增了启动器界面的**窗口圆角和窗口阴影**，使得启动器的外观更加现代。    |                                      
| [`#90`]<br>[`8183242`]<br>[`a38e737`] | 新增了命令行选项 `--load-lib` 用于载入外部库。          |
| [`#93`]<br>[`16b34aa`]                | 新增了命令行选项 `--enable-snapshot` 用于启用调试性截图。 |
| [`c36ec5e`]<br>[`f5c09bd`]            | 新增了可以调节桌宠的动画交叉过渡时长等过渡设置的功能。             |
| [`97095c9`]                           | 新增了启动器模型页面的列表中“没有符合条件的模型”时的一个提示。        |

| **修复**                            |                                           |
|:----------------------------------|:------------------------------------------|
| [`#91`]<br>[`#92`]<br>[`dadba6d`] | 修复了特定运行环境下由 Java Access Bridge 导致的软件崩溃问题。 |
| [`aeed07c`]                       | 修复了特定情况下因物理解算溢出而导致的桌宠异常浮空或位置卡死的问题。        |
| [`47798fe`]                       | 修复了个别敌方角色因预渲染检查未通过而无法启动的问题。               |

| **优化**      |                         |
|:------------|:------------------------|
| [`1072550`] | 优化了缓动函数的代码逻辑。           |
| [`82a5cee`] | 优化了高亮描边的过渡方式，使之表现得更加自然。 |

## v3.4
| **新增**                 |                                       |
|:-----------------------|:--------------------------------------|
| [`#83`]<br>[`c235a25`] | 新增了基于 LNK 的自启动，取代了原来的基于 VBS 的自启动。     |
| [`f89be4b`]            | 新增了当缩放倍率的设置值较大时的提示。                   |
| [`2a4de72`]            | 新增了“图标-弹窗”样式的选项警告提示，取代了原来的常驻式的选项警告提示。 |

| **修复**                            |                                      |
|:----------------------------------|:-------------------------------------|
| [`#81`]<br>[`#82`]<br>[`adb1b02`] | 修复了特定情况下由于骨骼未重置导致的动画错位或扭曲的问题。        |
| [`40a1d1f`]                       | 修复了在较大缩放倍率下桌宠尺寸会被强制裁切的问题。            |

| **优化**      |                                    |
|:------------|:-----------------------------------|
| [`a7db4b0`] | 优化了高亮描边特效在锐利边缘处的表现，现在采用了新的边缘检测滤波器。 |
| [`71b525c`] | 优化了透明缝合线填充在较大缩放倍率下的表现。             |

| **补丁**                             |                                                     |
|:-----------------------------------|:----------------------------------------------------|
| `v3.4.1`<br>[`#89`]<br>[`0d2e6b6`] | 修复了透明缝合线填充生效异常的问题。<br>修复了在 NVIDIA 显卡下角色发生白色线条闪烁的问题。 |

## v3.3
| **新增**                                    |                                            |
|:------------------------------------------|:-------------------------------------------|
| [`b34592f`]<br>[`91919e5`]<br>[`b8fbef1`] | 新增了可以渲染桌宠**高亮描边**的功能，并可以自定义开启条件、描边颜色和描边宽度。 |
| [`#78`]<br>[`44401ff`]                    | 新增了可以渲染桌宠**背景颜色**的功能，以满足绿幕等特殊需求。           |
| [`#71`]<br>[`cdb6a6f`]                    | 新增了可以自定义角色在正常模式和透明模式下的**不透明度**的功能。         |

| **修复**                 |                                      |
|:-----------------------|:-------------------------------------|
| [`#75`]<br>[`fb4fd87`] | 修复了不支持 JSON 格式的骨骼文件的问题。              |
| [`14ceaa6`]            | 修复了启动器在提示框弹出时无法与标题栏交互的问题。            |
| [`#76`]<br>[`62f0012`] | 缓解了角色的 Spine 组件拼合部位的透明缝合线现象。         |
| [`#79`]<br>[`50467bf`] | 修复了在特定情况下由于第三方程序干扰端口握手而导致的桌宠异常退出的问题。 |

| **优化**                 |                                     |
|:-----------------------|:------------------------------------|
| [`3399c76`]            | 优化了渲染时图像缩放的处理方法，现在采用骨骼缩放，而不是视窗缩放。   |
| [`#78`]<br>[`444e720`] | 优化了渲染时不透明度的处理方法，现在由着色器实现，而不是窗口系统实现。 |

## v3.2
| **新增**                            |                          |
|:----------------------------------|:-------------------------|
| [`#63`]<br>[`#70`]<br>[`6cbf7b2`] | 新增了在启动器抛出错误时的导出日志按钮。     |
| [`1bef435`]                       | 新增了可以设置桌宠的初始部署位置的功能。     |
| [`#68`]<br>[`1af28e6`]            | 新增了可以选择禁用将桌宠作为后台程序启动的功能。 |
| [`c7591f2`]                       | 新增了可以选择禁用将桌宠作为置顶窗口启动的功能。 |

| **优化**                     |                                                                     |
|:---------------------------|:--------------------------------------------------------------------|
| [`#62`]<br>[`0f48bc0`]     | 优化了部分显示设置项，已额外支持 2.5/3.0 倍的图像缩放和 120 帧的最大帧率，并且当设置的帧率高于显示器刷新率时会显示提示。 |
| [`fd880ee`]                | 优化了 GitHub Actions 脚本的依赖项版本。                                        |
| [`5117eca`]<br>[`9cf0b76`] | 优化了线程池和窗口标题相关的代码逻辑，避免了某些潜在的鲁棒性问题。                                   |
| [`fcb5111`]                | 优化了配置文件相关的代码逻辑，避免了在字段值缺失时默认填入空值或零值导致的鲁棒性问题。                         |

## v3.1
| **新增**                 |                               |
|:-----------------------|:------------------------------|
| [`cccb494`]            | 新增了启动器在被最小化、呼出或关闭时的窗口级别的动画效果。 |
| [`#37`]<br>[`6c4665b`] | 新增了可以**设置桌宠窗口边界的相对大小**的功能。    |

| **修复**                               |                                                                                       |
|:-------------------------------------|:--------------------------------------------------------------------------------------|
| [`#4`]<br>[`93d6975`]<br>[`c0c6333`] | 缓解了部分多形态敌人的窗口边界过大的问题，现在会在**阶段形态切换时自动调整窗口边界**。<br>缓解了角色的渲染超出窗口边界的问题，现在已增加预渲染采样时所用的帧数量。 |
| [`762970f`]                          | 修复了在特定情况下交互动画会被其他动画提前覆盖的问题。                                                           |
| [`0ec49d5`]                          | 修复了在特定情况下鼠标右键的竞态操作导致的桌宠程序忙等待（无响应）的问题。                                                 |
| [`686b2b8`]                          | 修复了某些操作发起的集成托盘通信会发生重复的问题。                                                             |

| **优化**                     |                                    |
|:---------------------------|:-----------------------------------|
| [`93d6975`]<br>[`c0c6333`] | 优化了窗口边界计算和摄像机配置的代码逻辑，扩增了窗口边界的最大尺寸。 |
| [`850f40d`]                | 优化了启动器中部分下拉框设置项的显示逻辑。              |
| [`d3a6ae5`]                | 优化了启动器模型页面的性能消耗。                   |

## v3.0
| **新增**                                                      |                                                                             |
|:------------------------------------------------------------|:----------------------------------------------------------------------------|
| [`#40`]<br>[`#59`]<br>[`#60`]<br>[`3253706`]<br>[`7b2e856`] | 新增了**集成托盘**功能和 Socket C/S 通信架构，现在可以通过一个集成托盘来管理已启动的桌宠。<br>新增了启动器**单实例化**的特性。 |
| [`#28`]<br>[`ff82a1e`]<br>[`17ceb23`]                       | 新增了**标签筛选**功能，现在可以通过选择角色标签来筛选模型列表。                                          |
| [`ff82a1e`]                                                 | 新增了可以**导出模型仓库的压缩包**的功能。<br>重构了模型页面，并将模型下载等功能集成为了**模型库管理**面板。                |
| [`ff82a1e`]                                                 | 新增了内置的启动器窗口**标题栏**，取代了默认的系统标题栏，以使观感更加统一。<br>新增了启动和退出启动器的**闪屏画面**。           |
| [`938ecbb`]<br>[`903fb96`]                                  | 新增了鼠标点击透明区域后，鼠标事件可以传递到某些下层窗口的功能。                                            |
| [`741cf00`]                                                 | 新增了关闭启动器界面时的确认弹窗。<br>新增了支持设置是否在退出启动器的同时退出已启动的桌宠的功能。                         |
| [`#57`]<br>[`741cf00`]                                      | 新增了模型库管理中的一个帮助链接。                                                           |
| [`0672739`]                                                 | 新增了应用于桌宠位置和图形变换的三次缓入（EaseOutCubic），取代了原来的线性缓动（Linear），以使观感更加丝滑。             |
| [`72784a6`]                                                 | 新增了对包含动态立绘的模型库的兼容性支持。                                                       |

| **优化**                                    |                                           |
|:------------------------------------------|:------------------------------------------|
| [`d47e424`]<br>[`e1e5439`]<br>[`12742d9`] | 优化了启动器界面的样式表，统一了复选框和滑动条组件的配色，微调了部分配色的颜色值。 |
| [`3d86cf6`]                               | 微调了网络连接超时的时间阈值。                           |
| [`f261c35`]                               | 优化了数据集不兼容的提示的显示逻辑和位置。                     |

## v2.4
| **新增**      |                                                     |
|:------------|:----------------------------------------------------|
| [`4754554`] | 新增了在下载对话框中显示下载速率的功能。                                |
| [`1eb6c08`] | 新增了**模型库下载源** ghproxy.harryh.cn，取代了原来的 ghproxy.com。 |
| [`a5c7b9a`] | 新增了可以**导入模型仓库的压缩包**以加载模型的功能。<br>新增了一些启动器页面跳转逻辑。     |
| [`727a34e`] | 新增了启动器的网络代理设置项。                                     |

| **修复**      |                              |
|:------------|:-----------------------------|
| [`cb06cba`] | 修复了启动器内弹出的对话框在关闭时未播放关闭动画的问题。 |

| **优化**                     |                                                |
|:---------------------------|:-----------------------------------------------|
| [`cb06cba`]                | 重构了**启动器前台任务**的代码逻辑。                           |
| [`1eb6c08`]                | 优化了控制台日志，输出流与错误流相分离。                           |
| [`#52`]<br>[`#55`]         | 优化了**自动化构建**，在 GitHub Actions 新增了 `build` 工作流。 |
| [`3b8f5fc`]<br>[`abc4743`] | 修订并公布了代码检查规则。                                  |
| [`f6139c3`]                | 重构了**模型资源**的代码逻辑。                              |
| [`#47`]<br>[`7db99c3`]     | 优化了 Windows 安装程序的默认安装目录和在控制面板卸载页面中的表现。         |

| **补丁**                  |                                             |
|:------------------------|:--------------------------------------------|
| `v2.4.1`<br>[`f2683f9`] | 修复了首次启动桌宠后的提示弹窗未生效的问题。                      |
| `v2.4.2`<br>[`5e15d00`] | 修复了渲染《明日方舟》2.1.41 及以后的新模型时 Alpha 图层表现异常的问题。 |

## v2.3
| **新增**      |                                              |
|:------------|:---------------------------------------------|
| [`a31afcf`] | 新增了**右键桌宠本体即可弹出菜单**的功能。                      |
| [`17d3fde`] | 新增了可以在菜单中**切换桌宠形态**的功能，现在可以切换拥有多个形态的敌方领袖的形态。 |

| **修复**                 |                                                  |
|:-----------------------|:-------------------------------------------------|
| [`#39`]<br>[`87c2263`] | 进一步修复了有概率出现桌宠本体程序在任务栏中无法隐藏的问题。                   |
| [`17d3fde`]            | 进一步修复了个别敌方角色的部分动作的选择逻辑异常的问题。<br>重构了动画名识别和行为控制系统。 |
| [`#48`]<br>[`b72421a`] | 修复了桌宠在保持坐下动作时拖动会导致异常浮动的问题。<br>重构了缓动控制系统。         |

| **优化**      |                                          |
|:------------|:-----------------------------------------|
| [`0fb103c`] | 优化了 Windows 安装程序的语言本地化（修订了简体中文，新增了繁体中文）。 |
| [`a31afcf`] | 优化了**托盘菜单**的外观表现。                        |
| [`e046e1c`] | 优化了**动画队列**的代码逻辑。                        |

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
| [`627d16d`]           | 新增了首次启动桌宠后的一个提示弹窗。                                                                              |
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
[`#47`]: https://github.com/isHarryh/Ark-Pets/issues/47
[`#48`]: https://github.com/isHarryh/Ark-Pets/issues/48
[`#52`]: https://github.com/isHarryh/Ark-Pets/issues/52
[`#55`]: https://github.com/isHarryh/Ark-Pets/issues/55
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
[`cb06cba`]: https://github.com/isHarryh/Ark-Pets/commit/cb06cba4e14838da89e4ea5c10cd29c402719985
[`4754554`]: https://github.com/isHarryh/Ark-Pets/commit/4754554ae9356f53f34313b8cfc1abc4fb57fd9b
[`1eb6c08`]: https://github.com/isHarryh/Ark-Pets/commit/1eb6c087b2b7587bfb9de38daabb9060dd0bfba7
[`3b8f5fc`]: https://github.com/isHarryh/Ark-Pets/commit/3b8f5fc2db55cd0916a6a9207b733f015951f746
[`f6139c3`]: https://github.com/isHarryh/Ark-Pets/commit/f6139c3890f1a23fc6abf71d62e8def0e17bb72e
[`abc4743`]: https://github.com/isHarryh/Ark-Pets/commit/abc4743ae4e4b2a854e405008de2bb7269ac6b17
[`7db99c3`]: https://github.com/isHarryh/Ark-Pets/commit/7db99c32f44d86ff23b9857fec21e5e024f8a9b8
[`a5c7b9a`]: https://github.com/isHarryh/Ark-Pets/commit/a5c7b9a99f4fd79d4f92497a7a855c71ba112dcb
[`727a34e`]: https://github.com/isHarryh/Ark-Pets/commit/727a34eed5d1a41ee3e6f153726f2bcf92a28958
[`f2683f9`]: https://github.com/isHarryh/Ark-Pets/commit/f2683f9d40bfb09fdacee719df9a001f55fa9d8f
[`5e15d00`]: https://github.com/isHarryh/Ark-Pets/commit/5e15d000bd77006da596696de2e41024fb4183d4

<!-- Links to v3.x References -->
[`#20`]: https://github.com/isHarryh/Ark-Pets/issues/20
[`#28`]: https://github.com/isHarryh/Ark-Pets/issues/28
[`#37`]: https://github.com/isHarryh/Ark-Pets/issues/37
[`#40`]: https://github.com/isHarryh/Ark-Pets/issues/40
[`#57`]: https://github.com/isHarryh/Ark-Pets/issues/57
[`#59`]: https://github.com/isHarryh/Ark-Pets/issues/59
[`#60`]: https://github.com/isHarryh/Ark-Pets/issues/60
[`#62`]: https://github.com/isHarryh/Ark-Pets/issues/62
[`#63`]: https://github.com/isHarryh/Ark-Pets/issues/63
[`#68`]: https://github.com/isHarryh/Ark-Pets/issues/68
[`#70`]: https://github.com/isHarryh/Ark-Pets/issues/70
[`#71`]: https://github.com/isHarryh/Ark-Pets/issues/71
[`#75`]: https://github.com/isHarryh/Ark-Pets/pull/75
[`#76`]: https://github.com/isHarryh/Ark-Pets/issues/76
[`#78`]: https://github.com/isHarryh/Ark-Pets/pull/78
[`#79`]: https://github.com/isHarryh/Ark-Pets/pull/79
[`#81`]: https://github.com/isHarryh/Ark-Pets/issues/81
[`#82`]: https://github.com/isHarryh/Ark-Pets/pull/82
[`#83`]: https://github.com/isHarryh/Ark-Pets/pull/83
[`#86`]: https://github.com/isHarryh/Ark-Pets/pull/86
[`#88`]: https://github.com/isHarryh/Ark-Pets/issues/88
[`#89`]: https://github.com/isHarryh/Ark-Pets/pull/89
[`#90`]: https://github.com/isHarryh/Ark-Pets/pull/90
[`#91`]: https://github.com/isHarryh/Ark-Pets/issues/91
[`#92`]: https://github.com/isHarryh/Ark-Pets/pull/92
[`#93`]: https://github.com/isHarryh/Ark-Pets/pull/93
[`#99`]:https://github.com/isHarryh/Ark-Pets/pull/99
[`3253706`]: https://github.com/isHarryh/Ark-Pets/commit/3253706fde859a316b3e08362dd57adb98c1df8c
[`7b2e856`]: https://github.com/isHarryh/Ark-Pets/commit/7b2e8562579ebabbb102b40122cf3130463f03bc
[`ff82a1e`]: https://github.com/isHarryh/Ark-Pets/commit/ff82a1e21ce396c345038b4cb340f10eeca89cf2
[`938ecbb`]: https://github.com/isHarryh/Ark-Pets/commit/938ecbbd4c010cd74088763c2124d68cee9d8042
[`741cf00`]: https://github.com/isHarryh/Ark-Pets/commit/741cf005040bcdc128d2a50bae0f314b3a6a54ad
[`0672739`]: https://github.com/isHarryh/Ark-Pets/commit/06727395c00116b15208b3201b31308727655ac7
[`d47e424`]: https://github.com/isHarryh/Ark-Pets/commit/d47e424cca356942d6033c5d8445dd4682975454
[`e1e5439`]: https://github.com/isHarryh/Ark-Pets/commit/e1e54390f9cb43d56cdbc68e7a5c8baf50f23764
[`3d86cf6`]: https://github.com/isHarryh/Ark-Pets/commit/3d86cf683296e6ab4ea5058c74a200150a4eb982
[`f261c35`]: https://github.com/isHarryh/Ark-Pets/commit/f261c353d4b5b9aa2c679096a406aeb0ccf6d66a
[`12742d9`]: https://github.com/isHarryh/Ark-Pets/commit/12742d9bc47adcd81f1392d6da256686f49d228a
[`17ceb23`]: https://github.com/isHarryh/Ark-Pets/commit/17ceb236f559916f59b83b59f24735023b8a0255
[`903fb96`]: https://github.com/isHarryh/Ark-Pets/commit/903fb9664c9e1c94ffaeb5a287e37c3adeb10d75
[`72784a6`]: https://github.com/isHarryh/Ark-Pets/commit/72784a6eee68b4e017ee4876553e82fff7616ed3
[`93d6975`]: https://github.com/isHarryh/Ark-Pets/commit/93d6975cd34ee9ca10165530b1de5283f68221f1
[`762970f`]: https://github.com/isHarryh/Ark-Pets/commit/762970facf2cf407731f39a840f10765a19819c9
[`0ec49d5`]: https://github.com/isHarryh/Ark-Pets/commit/0ec49d5a972fbbaf11fa869e53891f537da81f70
[`cccb494`]: https://github.com/isHarryh/Ark-Pets/commit/cccb494e539edea38374491e78930093d0c800d3
[`c0c6333`]: https://github.com/isHarryh/Ark-Pets/commit/c0c6333e80acca2316c2fe6d3f950fbe1053334a
[`686b2b8`]: https://github.com/isHarryh/Ark-Pets/commit/686b2b8552253f8012c237837d54784e891e3272
[`6c4665b`]: https://github.com/isHarryh/Ark-Pets/commit/6c4665b984aafae7240e1d219c0acd1d6ed26ee2
[`850f40d`]: https://github.com/isHarryh/Ark-Pets/commit/850f40de2528e7e01b680d21ab1127f35ad6b74f
[`d3a6ae5`]: https://github.com/isHarryh/Ark-Pets/commit/d3a6ae5c0984d16ccbc810a786d6a595f47a3c96
[`0f48bc0`]: https://github.com/isHarryh/Ark-Pets/commit/0f48bc0b3c31720385d4d3b1d4b47db84a9661c2
[`6cbf7b2`]: https://github.com/isHarryh/Ark-Pets/commit/6cbf7b2b2b02e4341e92f0cd085aa8202c721120
[`5117eca`]: https://github.com/isHarryh/Ark-Pets/commit/5117ecaecb48b45f6929efc1198b283bdd082804
[`fd880ee`]: https://github.com/isHarryh/Ark-Pets/commit/fd880ee2f8203d7ac7d4d2356fc994145182b26e
[`1bef435`]: https://github.com/isHarryh/Ark-Pets/commit/1bef4358730bf87e5f36c9ec52dceeba88ebef16
[`9cf0b76`]: https://github.com/isHarryh/Ark-Pets/commit/9cf0b76303ae65b05e26cc0005f3ea6451f197b2
[`fcb5111`]: https://github.com/isHarryh/Ark-Pets/commit/fcb51118f68c7765c4ae6171024a8968dc290933
[`1af28e6`]: https://github.com/isHarryh/Ark-Pets/commit/1af28e6a637e33c4da5e9eac0d2677abe2d53ccc
[`c7591f2`]: https://github.com/isHarryh/Ark-Pets/commit/c7591f249a6966fafe0111a4414a4a2daede6ad2
[`fb4fd87`]: https://github.com/isHarryh/Ark-Pets/commit/fb4fd8716ddde18ae22ac4d92ef9c2cf17b880d8
[`14ceaa6`]: https://github.com/isHarryh/Ark-Pets/commit/14ceaa6c118604f9a879042cad4746c5c0fa5d4d
[`b34592f`]: https://github.com/isHarryh/Ark-Pets/commit/b34592f12889ad30c53c617467e248882ca32a54
[`91919e5`]: https://github.com/isHarryh/Ark-Pets/commit/91919e5b74e466138a6304d854e0cac925ed6858
[`62f0012`]: https://github.com/isHarryh/Ark-Pets/commit/62f0012c10d8ff72fc6fc7fdbb40fccca1bbfc24
[`3399c76`]: https://github.com/isHarryh/Ark-Pets/commit/3399c765156574b108f344810e109ca0f36810a8
[`44401ff`]: https://github.com/isHarryh/Ark-Pets/commit/44401ff96fe34f3bf729afc441e7a526383e183c
[`444e720`]: https://github.com/isHarryh/Ark-Pets/commit/444e7208da094cbfabc5413f206e366b730996e8
[`50467bf`]: https://github.com/isHarryh/Ark-Pets/commit/50467bfa2cc662806df962600465aa6a55c425d2
[`cdb6a6f`]: https://github.com/isHarryh/Ark-Pets/commit/cdb6a6f5b86807ed6b68a22bcbc30972b7483fdb
[`b8fbef1`]: https://github.com/isHarryh/Ark-Pets/commit/b8fbef19f898db40039d0735b8a27e95bc8cb161
[`adb1b02`]: https://github.com/isHarryh/Ark-Pets/commit/adb1b02df79560c0a8a9501b86113e788cfa0622
[`c235a25`]: https://github.com/isHarryh/Ark-Pets/commit/c235a253c586a2911a24302ee7428ef873459382
[`40a1d1f`]: https://github.com/isHarryh/Ark-Pets/commit/40a1d1f9d01f37afc5d7ece4b0792ab35fee03ff
[`f89be4b`]: https://github.com/isHarryh/Ark-Pets/commit/f89be4b362cf8ca5b433e93923bbca8ec96e42b9
[`2a4de72`]: https://github.com/isHarryh/Ark-Pets/commit/2a4de72de8768bf9f0996e8a20ed8f6ae4dc4c5e
[`a7db4b0`]: https://github.com/isHarryh/Ark-Pets/commit/a7db4b0e9252e4d08ff1e6e9d4d8b8967ace36bb
[`71b525c`]: https://github.com/isHarryh/Ark-Pets/commit/71b525c77003175f05dc1805f19d038872846acf
[`0d2e6b6`]: https://github.com/isHarryh/Ark-Pets/commit/0d2e6b645717707a2c302ae844614c867fce74bc
[`e998e4a`]: https://github.com/isHarryh/Ark-Pets/commit/e998e4a374ab39424492ce9b1986f0eb35d6e568
[`7042699`]: https://github.com/isHarryh/Ark-Pets/commit/70426997992a969ae59bb05122d6ca2683d2186e
[`88ffa1e`]: https://github.com/isHarryh/Ark-Pets/commit/88ffa1eee50531da4427a4e14c55fa315a7e7743
[`8183242`]: https://github.com/isHarryh/Ark-Pets/commit/8183242e92cafd4fb3b8002f692fc84bb6f529db
[`a38e737`]: https://github.com/isHarryh/Ark-Pets/commit/a38e737b1911c24fe4ebef46f56c28e0c948e0c3
[`1072550`]: https://github.com/isHarryh/Ark-Pets/commit/10725500e19a86d902f192b9607cc3f7ef114e4a
[`16b34aa`]: https://github.com/isHarryh/Ark-Pets/commit/16b34aa38046199ff1c461e5d5f8f2760e311a95
[`dadba6d`]: https://github.com/isHarryh/Ark-Pets/commit/dadba6da2b1aad68e5937bb6bd739db58c97be56
[`aeed07c`]: https://github.com/isHarryh/Ark-Pets/commit/aeed07caebf11568cab09691dcfb8a2a7ad09ae2
[`c36ec5e`]: https://github.com/isHarryh/Ark-Pets/commit/c36ec5e8d65c42a3d07ae8f9957a29bea7d399f8
[`f5c09bd`]: https://github.com/isHarryh/Ark-Pets/commit/f5c09bdda535c75cdba9039d3d38f7cfba1da1df
[`97095c9`]: https://github.com/isHarryh/Ark-Pets/commit/97095c9f27505d556d7cacaafb2092e3766552c9
[`47798fe`]: https://github.com/isHarryh/Ark-Pets/commit/47798fecd17623a18d48c5b92b7246fd02e0b43a
[`82a5cee`]: https://github.com/isHarryh/Ark-Pets/commit/82a5ceedd48c83ee699a036d316f0e44dee39a77
[`aebb7f7`]: https://github.com/isHarryh/Ark-Pets/commit/aebb7f7ef035d7800f607ac43346a9af26c5caf9
[`35d4d74`]: https://github.com/isHarryh/Ark-Pets/commit/35d4d74f6b30140bd8957dcfb679166c3dd78523
[`a43d795`]: https://github.com/isHarryh/Ark-Pets/commit/a43d7956d2d4ec22418bf38639da38248bfe7d39
[`ce3051d`]: https://github.com/isHarryh/Ark-Pets/commit/ce3051d78707293a9aad7d6b30ef9fcb68c66519
[`90f0660`]: https://github.com/isHarryh/Ark-Pets/commit/90f066032122b39619466d8eb98233de44674d73
[`e06dc6d`]: https://github.com/isHarryh/Ark-Pets/commit/e06dc6db09c8d17a63fc85ebe47ba41b0cabf91b
[`c95768a`]: https://github.com/isHarryh/Ark-Pets/commit/c95768aa6200b4bf480519d5b467757277d1b122
[`c277dae`]: https://github.com/isHarryh/Ark-Pets/commit/c277dae858b1e27aeb38712d6e3d090420de3846
