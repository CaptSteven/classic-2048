# 2048

> 一个原生 Android 版 2048：滑动合并数字，挑战更高分数。

**技术栈：** Java、Android 自定义 View / Canvas、SharedPreferences · **平台：** Android API 23+

## 功能

手势移动与同值合并；显示当前和最佳分数；重新开始。

## 运行

1. 用 Android Studio 打开仓库根目录，等待 Gradle 同步。
2. 使用 JDK 17 和 Android SDK 35，连接设备或启动模拟器。
3. 运行 `app`；也可在项目根目录执行 `./gradlew assembleDebug` 构建调试包。

## 代码入口

- [`app/src/main/`](app/src/main/)：界面、交互和 Android 资源。
- [`app/build.gradle`](app/build.gradle)：SDK、语言和构建配置。

项目为应用原型；本次文档改写未重新构建或验证设备兼容性。

<!-- 更新备注：YANG KAISEN，2026-11-11；改写与核对依据见工作区变更记录。 -->
