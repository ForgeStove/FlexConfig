plugins {
	id("fabric-loom") version "1.18.4"
	id("me.modmuss50.mod-publish-plugin")
}
configure("fabric")
loom {
	mods {
		create(p("modId")) { sourceSet("main") }
	}
	runs {
		named("client") {
			client()
			displayName.set("fabric - Client")
			appendProjectPathToDisplayName.set(false)
		}
		named("server") {
			server()
			runDirectory.set(file("run/server"))
			displayName.set("fabric - Server")
			appendProjectPathToDisplayName.set(false)
		}
		configureEach {
			generateRunConfig.set(true)
			preferGradleTask.set(false)
			jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")
			jvmArguments.add("-XX:+IgnoreUnrecognizedVMOptions")
		}
	}
}
dependencies {
	minecraft("com.mojang:minecraft:${p("mcVersion")}")
	@Suppress("UnstableApiUsage") mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-${p("mcVersion")}:${p("parchmentVersion")}@zip")
	})
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
	maven("https://maven.parchmentmc.org")
}
