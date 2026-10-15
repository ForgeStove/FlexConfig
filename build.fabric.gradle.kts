plugins {
	id("dev.kikugie.loom-back-compat")
	id("me.modmuss50.mod-publish-plugin")
}
configure("fabric")
dependencies {
	minecraft("com.mojang:minecraft:${p("mcVersion")}")
	loomx.applyMojangMappings()
	modImplementation("net.fabricmc:fabric-loader:${p("fabricLoaderVersion")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${p("fabricApiVersion")}+${p("mcVersion")}")
	fun impleclude(string: String) { implementation(string)?.let { include(it) } }
	impleclude("com.electronwill.night-config:core:${p("nightConfigVersion")}")
	impleclude("com.electronwill.night-config:toml:${p("nightConfigVersion")}")
	modImplementation("com.terraformersmc:modmenu:${p("modmenuVersion")}")
}
repositories {
	maven("https://maven.fabricmc.net")
	maven("https://maven.terraformersmc.com/releases")
}
