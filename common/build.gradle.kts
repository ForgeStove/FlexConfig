plugins {
	id("net.neoforged.moddev.legacyforge") version "2.0.148"
}
version = "${p("mcVersion")}-${p("modVersion")}"
extensions.getByType(BasePluginExtension::class.java).archivesName.set("${p("modId")}-common")
legacyForge {
	mcpVersion = p("mcVersion")
	parchment {
		mappingsVersion.set(p("parchmentVersion"))
		minecraftVersion.set(p("mcVersion"))
	}
}
dependencies {
	compileOnly("com.electronwill.night-config:core:${p("nightConfigVersion")}")
	compileOnly("com.electronwill.night-config:toml:${p("nightConfigVersion")}")
}
configurations {
	create("commonJava") {
		isCanBeResolved = false
		isCanBeConsumed = true
	}
	create("commonResources") {
		isCanBeResolved = false
		isCanBeConsumed = true
	}
}
val mainSourceSet = extensions.getByType(SourceSetContainer::class.java)["main"]
artifacts {
	add("commonJava", mainSourceSet.java.sourceDirectories.singleFile)
	add("commonResources", mainSourceSet.resources.sourceDirectories.singleFile)
}
