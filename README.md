# Flex Config

A lightweight, annotation-driven configuration library for Minecraft mods.
NeoForge and Fabric, Minecraft 1.21.1.

[English](README.md) | [简体中文](README.zh-CN.md)

## Usage

Consume it from the GitHub release as an ivy repository:

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

Coordinates follow `io.github.forgestove:flexconfig:<mcVersion>-<modVersion>-<loader>`, e.g.
`1.21.1-0.1.0-neoforge` (use the version from the
[Releases](https://github.com/ForgeStove/FlexConfig/releases) page). To embed it in your own artifact,
use `jarJar(...)` on NeoForge and `include(...)` on Fabric instead of `implementation(...)`.

## License

[MIT](LICENSE)
