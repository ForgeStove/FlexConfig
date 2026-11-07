import org.gradle.jvm.tasks.Jar

allprojects {
	group = p("modGroupId")
}
subprojects {
	tasks.withType<JavaCompile>().configureEach {
		options.encoding = "UTF-8"
	}
	repositories {
		mavenLocal()
		mavenCentral()
	}
	plugins.withId("java") {
		extensions.configure<JavaPluginExtension> {
			toolchain.languageVersion.set(JavaLanguageVersion.of(p("javaVersion")))
			withSourcesJar()
		}
		tasks.named<Jar>("jar") { from(rootProject.file("LICENSE")) }
		tasks.named<Test>("test") { failOnNoDiscoveredTests.set(false) }
		if(path == ":common") return@withId
		val commonJava = configurations.create("commonJava") { isCanBeResolved = true }
		val commonResources = configurations.create("commonResources") { isCanBeResolved = true }
		dependencies {
			add("compileOnly", project(":common"))
			add("commonJava", project(path = ":common", configuration = "commonJava"))
			add("commonResources", project(path = ":common", configuration = "commonResources"))
		}
		tasks.named<JavaCompile>("compileJava") {
			dependsOn(commonJava)
			source(commonJava)
		}
		tasks.named<ProcessResources>("processResources") {
			dependsOn(commonResources)
			from(commonResources)
		}
		tasks.named<Jar>("sourcesJar") {
			dependsOn(commonJava, commonResources)
			from(commonJava, commonResources)
		}
	}
}
