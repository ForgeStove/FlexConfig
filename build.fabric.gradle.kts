import flexconfig.configureCommon
import flexconfig.configurePublishMods
import flexconfig.p

plugins {
	// loom 由 loom-back-compat 按 MC 版本引入，这里不再显式声明 fabric-loom
	id("dev.kikugie.loom-back-compat")
	id("me.modmuss50.mod-publish-plugin")
}
configureCommon("fabric")
dependencies {
	minecraft("com.mojang:minecraft:${p("mcVersion")}")
	// 混淆版本上应用 Mojang 映射；非混淆版本（>=26.1）上是 no-op
	loomx.applyMojangMappings()
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
