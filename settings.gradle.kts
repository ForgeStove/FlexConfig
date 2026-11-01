pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://maven.neoforged.net/releases")
	}
}
plugins {
	// Java toolchain 缺失时由 Gradle 自动下载
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "FlexConfig"
include(":forge")
