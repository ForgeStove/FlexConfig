# Flex Config

A lightweight, annotation-driven configuration library for Minecraft mods.

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

```groovy
repositories {
    ivy {
        url = uri('https://github.com/ForgeStove/FlexConfig/releases/download')
        patternLayout { artifact '[revision]/[artifact]-[revision](-[classifier]).jar' }
        metadataSources { artifact() }
    }
}
dependencies {
    jarJar(implementation("io.github.forgestove:flexconfig:$mcVersion-$flexConfigVersion-$loader"))
}
```

## License

[MIT](LICENSE)

## Credits

**Punchy!**: UI design reference
