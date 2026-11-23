pluginManagement {
	repositories {
		gradlePluginPortal()
		maven("https://maven.minecraftforge.net")
		maven("https://maven.neoforged.net/releases")
		maven("https://maven.fabricmc.net")
		maven("https://repo.spongepowered.org/repository/maven-public")
	}
}
plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
include(":common", ":forge", ":fabric")
