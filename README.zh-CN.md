# Flex Config

一个轻量的、注解驱动的 Minecraft 模组配置库。
支持 NeoForge 与 Fabric，Minecraft 1.21.1。

[English](README.md) | [简体中文](README.zh-CN.md)

## 构建

```bash
./gradlew :1.21.1-neoforge:build
./gradlew :1.21.1-fabric:build
```

产物位于 `versions/<版本>-<加载器>/build/libs/`。

多版本由 [Stonecutter](https://stonecutter.kikugie.dev/) 管理：`src/main/java` 是共享源码
（版本差异用 `//? <条件> {` 注释切），`src/neoforge/java` 与 `src/fabric/java` 是加载器专属源码。

## 许可证

[MIT](LICENSE)
