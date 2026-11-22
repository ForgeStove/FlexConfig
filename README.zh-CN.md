# Flex Config

一个轻量的、注解驱动的 Minecraft 模组配置库。

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
	implementation("io.github.forgestove:flexconfig:$loader-$mcVersion-$flexConfigVersion")?.let { jarJar(it) }
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
    jarJar(implementation("io.github.forgestove:flexconfig:$loader-$mcVersion-$flexConfigVersion"))
}
```

## 许可证

[MIT](LICENSE)

## 鸣谢

**Punchy!**: UI设计参照
