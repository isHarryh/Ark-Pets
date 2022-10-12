# 更新日志 / CHANGELOG

## v1.3
#### 修复
1. [`#3`](https://github.com/isHarryh/Ark-Pets/issues/3) [`9648fe`](https://github.com/isHarryh/Ark-Pets/commit/9648fe3089bb7b11b7693e2f61eed54a598b2023) 修复了模型在空中自由落体时开始行走会使其做匀速直线运动的问题。
2. [`#5`](https://github.com/isHarryh/Ark-Pets/issues/5) [`9648fe`](https://github.com/isHarryh/Ark-Pets/commit/9648fe3089bb7b11b7693e2f61eed54a598b2023) 修复了小人在空中被右键后不会落下的问题。
3. [`#2`](https://github.com/isHarryh/Ark-Pets/issues/2) [`feaa6f`](https://github.com/isHarryh/Ark-Pets/commit/feaa6fa5ffad183d1bb14f6b8057a6c5e2ba31c6) 修复了基建小人在Sit和不可打断动作(例如Special)同时进行时，窗口的垂直位置表现不正常的问题。
4. [`feaa6f`](https://github.com/isHarryh/Ark-Pets/commit/feaa6fa5ffad183d1bb14f6b8057a6c5e2ba31c6) 修复了基建小人的Sit动作的窗口垂直偏移值不受图像缩放影响的问题。
5. [`#3`](https://github.com/isHarryh/Ark-Pets/issues/3) [`bcbe4c`](https://github.com/isHarryh/Ark-Pets/commit/bcbe4cbea63406ec15c74cd80e7dbaf7cf9ec0f0) 修复了多个模型堆叠时持续闪烁的问题。

## v1.2
#### 新增
1. [`ff1531`](https://github.com/isHarryh/Ark-Pets/commit/ff15314933cb17eab210475f326943911f5b3258) 新增了摔落动作，以搭配平面重力场使用，同时也移除了拖拽结束动作。
2. [`11b097`](https://github.com/isHarryh/Ark-Pets/commit/11b09770582d7e36548021ec844983627db2f163) 新增了平面重力场系统的模拟实现。
3. [`11b097`](https://github.com/isHarryh/Ark-Pets/commit/11b09770582d7e36548021ec844983627db2f163) 新增了主页面的“下边界距离”调节滑块。

## v1.1
#### 新增
1. [`4603ab`](https://github.com/isHarryh/Ark-Pets/commit/4603ab020d62f13592e36d771f8525721656970c) 新增了主界面的“使用手册”按钮，点击后可跳转到GitHub仓库主页。

#### 修复
1. [`ff79db`](https://github.com/isHarryh/Ark-Pets/commit/ff79dbdaa19e4e9abbadf23fec4e9d43e421bf6f) 修复了基建小人在Interact和Sit同时进行时，窗口的垂直位置表现不正常的问题。
2. [`ff79db`](https://github.com/isHarryh/Ark-Pets/commit/ff79dbdaa19e4e9abbadf23fec4e9d43e421bf6f) 修复了窗口可以离开屏幕右下边界的问题，现在窗口将被严格限制在屏幕内。
