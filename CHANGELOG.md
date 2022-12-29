# 更新日志 / CHANGELOG

以下收录了每个大版本分支所新增、修复、优化的条目。
> 若条目未注明子版本，默认是在`vX.X.0`版本实现的内容。

## v1.6
#### 新增
1. [`a4267c6`](https://github.com/isHarryh/Ark-Pets/commit/a4267c68ddca1bf1cf994e2748d0986ef38a2140) 新增了重力场系统对于点电荷静电斥力的支持。
2. [`#19`](https://github.com/isHarryh/Ark-Pets/issues/19) [`a4267c6`](https://github.com/isHarryh/Ark-Pets/commit/a4267c68ddca1bf1cf994e2748d0986ef38a2140) 由上一条实现：新增了多个小人重叠时可以被排斥开的特性。
3. [`40a57e7`](https://github.com/isHarryh/Ark-Pets/commit/40a57e7f279bfce96d2e6b651bc0b9ba5c0104bf) 新增了可操作的任务栏托盘图标。
4. [`#9`](https://github.com/isHarryh/Ark-Pets/issues/9) [`40a57e7`](https://github.com/isHarryh/Ark-Pets/commit/40a57e7f279bfce96d2e6b651bc0b9ba5c0104bf) 新增了右键托盘图标可锁定小人当前动作的功能。

#### 修复
1. [`4954639`](https://github.com/isHarryh/Ark-Pets/commit/4954639d623608dfd0443790e786176d57ff2212) 修复了在小人靠近屏幕左侧时无法站立在窗口上的问题。

#### 优化
1. [`3fd0d36`](https://github.com/isHarryh/Ark-Pets/commit/3fd0d36f0be38fa31563aebf7ef2303c4e917f42) 启动器中以“帮助”按钮替换掉了原来的“使用手册”按钮。
2. [`#3`](https://github.com/isHarryh/Ark-Pets/issues/3) [`4954639`](https://github.com/isHarryh/Ark-Pets/commit/4954639d623608dfd0443790e786176d57ff2212) 任务栏常驻程序窗口现在可以隐藏；桌宠可以跨桌面显示。
3. 微调了悬空状态和落地状态触发相关动作的判定阈值。

## v1.5
#### 新增
1. `v1.5.1` [`#12`](https://github.com/isHarryh/Ark-Pets/issues/12) [`d5f0bf0`](https://github.com/isHarryh/Ark-Pets/commit/d5f0bf0bae3f1589de5f71aeeb0b5aad82a234b0) 新增了命令行启动参数`--direct-start`用于直接启动桌宠而不打开启动器。
2. `v1.5.2` [`#19`](https://github.com/isHarryh/Ark-Pets/issues/19) [`76e6883`](https://github.com/isHarryh/Ark-Pets/commit/76e68832d6987ef2cb4fd65ad28dc754ef5e4b56) 新增了小人自动行走至窗口边缘后翻转朝向的特性。
3. `v1.5.2` [`#19`](https://github.com/isHarryh/Ark-Pets/issues/19) [`a7eba09`](https://github.com/isHarryh/Ark-Pets/commit/a7eba09b35b320ec24816eccf5d4413e175cc6ba) 新增了抛出小人后，使小人立即面向抛掷方向的特性。

#### 修复
1. [`#15`](https://github.com/isHarryh/Ark-Pets/issues/15) [`afa7bb9`](https://github.com/isHarryh/Ark-Pets/commit/afa7bb94cd46c1d51725f4bb58b7ac462d729bdc) 修复了小人在窗口上缘与屏幕上缘距离小于应用高度时显示异常的问题。
2. [`#13`](https://github.com/isHarryh/Ark-Pets/issues/13) [`61908a0`](https://github.com/isHarryh/Ark-Pets/commit/61908a0023980a7ff6affee3b8814a77c92585cf) 修复了在部分设备上由内存抖动引发的卡顿问题，大幅度降低了性能消耗。
3. `v1.5.1` [`c996b38`](https://github.com/isHarryh/Ark-Pets/commit/c996b383e4fddda0acf2362774ec0ecc2a1cb8a6) 修复了不支持一星小车的基建小人模型的问题。

#### 优化
1. [`67a0c66`](https://github.com/isHarryh/Ark-Pets/commit/67a0c66b9ec0f713d581d5062e9c0098226b39d0) 不再以较高频率获取窗口句柄，一定程度降低了性能消耗。
2. [`67a0c66`](https://github.com/isHarryh/Ark-Pets/commit/67a0c66b9ec0f713d581d5062e9c0098226b39d0) 不再在非必要时重设窗口的位置，一定程度降低了性能消耗。
3. 微调了小人的缩放相关常量，略微缓解了小人图像超出窗口边界的问题。
4. 微调了小人的自定义缩放设置项，已支持0.75/1.25/1.5倍的图像缩放。

## v1.4
#### 新增
1. [`ae979eb`](https://github.com/isHarryh/Ark-Pets/commit/ae979eb0031b401bc52d44c0d396f12eeba4a64d) 新增了重力场系统对于一维障碍物的支持。
2. [`d31f49b`](https://github.com/isHarryh/Ark-Pets/commit/d31f49bf116b836bac6b9d1a2db83f72c216e31a) 由上一条实现：新增了小人可以站立在电脑窗口边缘上的特性。

#### 修复
1. [`aeed29a`](https://github.com/isHarryh/Ark-Pets/commit/aeed29a9bf25db445ef15801a83172e1b84d1ccd) 修复了基建小人的Sit动作的渲染偏移受图像缩放的影响被放大的问题。
2. [`#10`](https://github.com/isHarryh/Ark-Pets/issues/10) [`aeed29a`](https://github.com/isHarryh/Ark-Pets/commit/aeed29a9bf25db445ef15801a83172e1b84d1ccd) 修复了基建小人的Sit动作的窗口垂直位置不能低于屏幕下边界，导致在某些情况下浮空的问题。

#### 优化
1. 不再使用[EVB](https://lifeinhex.com/tag/enigma-virtual-box)封装exe版的Release，现在采用[InnoSetup](https://jrsoftware.org/isinfo.php)来封装exe。
2. 微调了重力场系统的质点最大速度限制。
3. 微调了重力场系统的质点完全失重判定规则。

## v1.3
#### 修复
1. [`#3`](https://github.com/isHarryh/Ark-Pets/issues/3) [`9648fe3`](https://github.com/isHarryh/Ark-Pets/commit/9648fe3089bb7b11b7693e2f61eed54a598b2023) 修复了模型在空中自由落体时开始行走会使其做匀速直线运动的问题。
2. [`#5`](https://github.com/isHarryh/Ark-Pets/issues/5) [`9648fe3`](https://github.com/isHarryh/Ark-Pets/commit/9648fe3089bb7b11b7693e2f61eed54a598b2023) 修复了小人在空中被右键后不会落下的问题。
3. [`#2`](https://github.com/isHarryh/Ark-Pets/issues/2) [`feaa6fa`](https://github.com/isHarryh/Ark-Pets/commit/feaa6fa5ffad183d1bb14f6b8057a6c5e2ba31c6) 修复了基建小人在Sit和不可打断动作(例如Special)同时进行时，窗口的垂直位置表现不正常的问题。
4. [`feaa6fa`](https://github.com/isHarryh/Ark-Pets/commit/feaa6fa5ffad183d1bb14f6b8057a6c5e2ba31c6) 修复了基建小人的Sit动作的窗口垂直偏移值不受图像缩放影响的问题。
5. [`#3`](https://github.com/isHarryh/Ark-Pets/issues/3) [`bcbe4cb`](https://github.com/isHarryh/Ark-Pets/commit/bcbe4cbea63406ec15c74cd80e7dbaf7cf9ec0f0) 修复了多个模型堆叠时持续闪烁的问题。

## v1.2
#### 新增
1. [`ff15314`](https://github.com/isHarryh/Ark-Pets/commit/ff15314933cb17eab210475f326943911f5b3258) 新增了摔落动作，以搭配平面重力场使用，同时也移除了拖拽结束动作。
2. [`11b0977`](https://github.com/isHarryh/Ark-Pets/commit/11b09770582d7e36548021ec844983627db2f163) 新增了平面重力场系统的模拟实现。
3. [`11b0977`](https://github.com/isHarryh/Ark-Pets/commit/11b09770582d7e36548021ec844983627db2f163) 新增了主页面的“下边界距离”调节滑块。

## v1.1
#### 新增
1. [`4603ab0`](https://github.com/isHarryh/Ark-Pets/commit/4603ab020d62f13592e36d771f8525721656970c) 新增了主界面的“使用手册”按钮，点击后可跳转到GitHub仓库主页。

#### 修复
1. [`ff79dbd`](https://github.com/isHarryh/Ark-Pets/commit/ff79dbdaa19e4e9abbadf23fec4e9d43e421bf6f) 修复了基建小人在Interact和Sit同时进行时，窗口的垂直位置表现不正常的问题。
2. [`ff79dbd`](https://github.com/isHarryh/Ark-Pets/commit/ff79dbdaa19e4e9abbadf23fec4e9d43e421bf6f) 修复了窗口可以离开屏幕右下边界的问题，现在窗口将被严格限制在屏幕内。
