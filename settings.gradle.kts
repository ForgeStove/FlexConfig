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
}
stonecutter {
	create(rootProject) {
		fun match(version: String, vararg loaders: String) =
			loaders.forEach { version("$version-$it", version).buildscript = "build.$it.gradle.kts" }
		match("1.21.1", "fabric", "neoforge")
		vcsVersion = "1.21.1-neoforge"
	}
}
