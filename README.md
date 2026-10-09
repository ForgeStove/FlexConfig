# Flex Config

A lightweight, annotation-driven configuration library for Minecraft mods.
NeoForge and Fabric, Minecraft 1.21.1.

[English](README.md) | [简体中文](README.zh-CN.md)

## Building

```bash
./gradlew :1.21.1-neoforge:build
./gradlew :1.21.1-fabric:build
```

Artifacts land in `versions/<version>-<loader>/build/libs/`.

Versions are managed with [Stonecutter](https://stonecutter.kikugie.dev/): `src/main/java` is shared
(version differences use `//? <condition> {` comments), while `src/neoforge/java` and `src/fabric/java` are loader-specific.

## License

[MIT](LICENSE)
