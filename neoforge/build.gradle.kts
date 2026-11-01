plugins {
	id("net.neoforged.moddev") version "2.0.148" // https://plugins.gradle.org/m2/net/neoforged/moddev/net.neoforged.moddev.gradle.plugin/maven-metadata.xml
	id("me.modmuss50.mod-publish-plugin")
}
configure("neoforge")
neoForge {
	version = p("neoforgeVersion")
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
			// 演示模组目录来自 test 源集，先编译它，否则 fml.modFolders 会带上不存在的路径
			taskBefore(tasks.named("testClasses"))
		}
	}
	mods.create(p("modId")) { sourceSet(sourceSets.main.get()) }
	mods.create("${p("modId")}_demo") { sourceSet(sourceSets.test.get()) }
}
