plugins {
	id("net.neoforged.moddev.legacyforge") version "2.0.148" // https://plugins.gradle.org/c/plugin/net.neoforged.moddev.legacyforge
	id("me.modmuss50.mod-publish-plugin")
}
configure("forge")
legacyForge {
	version = p("forgeVersion")
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
	}
	mods.create(p("modId")) { sourceSet(sourceSets.main.get()) }
	mods.create("${p("modId")}_demo") { sourceSet(sourceSets.test.get()) }
}
