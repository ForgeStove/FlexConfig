import flexconfig.configureCommon
import flexconfig.configurePublishMods
import flexconfig.p

plugins {
	id("net.neoforged.moddev") version "+"
	id("me.modmuss50.mod-publish-plugin")
}
configureCommon("neoforge")
neoForge {
	version = p("loaderVersion")
	parchment {
		mappingsVersion.set(p("parchmentVersion"))
		minecraftVersion.set(p("mcVersion"))
	}
	runs {
		create("client").client()
		create("server") {
			server()
			gameDirectory.set(file("run/server"))
			programArgument("--nogui")
		}
		configureEach {
			systemProperty("terminal.jline", "true")
			jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
			jvmArgument("-XX:+AllowEnhancedClassRedefinition")
		}
	}
	mods.create(p("modId")).sourceSet(sourceSets.main.get())
}
configurePublishMods("neoforge")
