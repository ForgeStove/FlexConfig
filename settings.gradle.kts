pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://maven.neoforged.net/releases")
		maven("https://maven.fabricmc.net")
		maven("https://maven.kikugie.dev/snapshots")
		maven("https://maven.kikugie.dev/releases")
	}
}
plugins {
	id("dev.kikugie.stonecutter") version "0.9.8"
	id("dev.kikugie.loom-back-compat") version "0.4.2"
	// 1.20.1 变体需要 JDK 17，本机没有时由 Gradle 自动下载
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
stonecutter {
	create(rootProject) {
		fun match(version: String, vararg loaders: String) =
			loaders.forEach { version("$version-$it", version).buildscript = "build.$it.gradle.kts" }
		match("1.21.1", "fabric", "neoforge")
		match("1.20.1", "forge")
		vcsVersion = "1.21.1-neoforge"
	}
}
