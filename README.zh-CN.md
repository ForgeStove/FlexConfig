# Flex Config

一个轻量的、注解驱动的 Minecraft 模组配置库。 支持 NeoForge 与 Fabric，Minecraft 1.21.1。

[English](README.md) | [简体中文](README.zh-CN.md)

## 使用

以 GitHub Release 作为 ivy 仓库拉取：

```kotlin
repositories {
	ivy {
		url = uri("https://github.com/ForgeStove/FlexConfig/releases/download")
		patternLayout { artifact("[revision]/[artifact]-[revision](-[classifier]).jar") }
		metadataSources { artifact() }
	}
}

dependencies {
	implementation("io.github.forgestove:flexconfig:$mcVersion-$flexConfigVersion-$loader")?.let { jarJar(it) }
}
```

坐标形如 `io.github.forgestove:flexconfig:<MC版本>-<库版本>-<加载器>`，例如 `1.21.1-0.1.0-neoforge`
（版本号以 [Releases](https://github.com/ForgeStove/FlexConfig/releases) 页为准）。需要把它内嵌进 自己的产物时，NeoForge 用 `jarJar(...)`、Fabric
用 `include(...)` 替代 `implementation(...)`。

## 许可证

[MIT](LICENSE)
