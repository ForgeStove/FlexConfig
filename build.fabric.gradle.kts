import flexconfig.configureCommon
import flexconfig.configurePublishMods
import flexconfig.p

plugins {
	id("fabric-loom") version "1.11.+"
	id("me.modmuss50.mod-publish-plugin")
}
configureCommon("fabric")
dependencies {
	minecraft("com.mojang:minecraft:${p("mcVersion")}")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${p("fabricLoaderVersion")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${p("fabricApiVersion")}")
	fun impleclude(string: String) { implementation(string)?.let { include(it) } }
	impleclude("com.electronwill.night-config:core:${p("nightConfigVersion")}")
	impleclude("com.electronwill.night-config:toml:${p("nightConfigVersion")}")
}
repositories {
	maven("https://maven.fabricmc.net")
}
configurePublishMods("fabric")
