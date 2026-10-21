plugins {
	id("net.neoforged.moddev") version "2.0.148" // https://plugins.gradle.org/m2/net/neoforged/moddev/net.neoforged.moddev.gradle.plugin/maven-metadata.xml
	id("me.modmuss50.mod-publish-plugin")
}
configure("neoforge")
neoForge {
	version = p("loaderVersion")
	parchment {
		mappingsVersion.set(p("parchmentVersion"))
		minecraftVersion.set(p("mcVersion"))
	}
	runs {
		create("client") {
			client()
			systemProperty("terminal.jline", "true")
		}
		create("server") {
			server()
			gameDirectory.set(file("run/server"))
			programArgument("--nogui")
		}
		configureEach {
			jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
			jvmArgument("-XX:+AllowEnhancedClassRedefinition")
		}
	}
	mods.create(p("modId")).sourceSet(sourceSets.main.get())
}
