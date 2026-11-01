plugins {
	id("fabric-loom") version "1.18.4"
	id("me.modmuss50.mod-publish-plugin")
}
configure("fabric")
loom {
	mods {
		create(p("modId")) { sourceSet("main") }
		create("${p("modId")}_demo") { sourceSet("test") }
	}
}
sourceSets.main.get().runtimeClasspath += sourceSets.test.get().output
dependencies {
	minecraft("com.mojang:minecraft:${p("mcVersion")}")
	mappings(loom.officialMojangMappings())
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
