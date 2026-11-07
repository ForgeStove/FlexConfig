import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources

allprojects {
	group = p("modGroupId")
}

subprojects {
	apply(plugin = "java")
	extensions.configure<JavaPluginExtension> {
		toolchain.languageVersion.set(JavaLanguageVersion.of(p("javaVersion")))
		withSourcesJar()
	}
	tasks.withType<JavaCompile>().configureEach {
		options.encoding = "UTF-8"
	}
	tasks.named<Jar>("jar") {
		from(rootProject.file("LICENSE"))
	}
	tasks.named<Test>("test") {
		failOnNoDiscoveredTests.set(false)
	}
	repositories {
		mavenLocal()
		mavenCentral()
	}

	if(path == ":common") return@subprojects

	configurations.create("commonJava") { isCanBeResolved = true }
	configurations.create("commonResources") { isCanBeResolved = true }
	dependencies {
		add("compileOnly", project(":common"))
		add("commonJava", project(path = ":common", configuration = "commonJava"))
		add("commonResources", project(path = ":common", configuration = "commonResources"))
	}
	tasks.named<JavaCompile>("compileJava") {
		dependsOn(configurations["commonJava"])
		source(configurations["commonJava"])
	}
	tasks.named<ProcessResources>("processResources") {
		dependsOn(configurations["commonResources"])
		from(configurations["commonResources"])
	}
	tasks.named<Jar>("sourcesJar") {
		dependsOn(configurations["commonJava"])
		from(configurations["commonJava"])
		dependsOn(configurations["commonResources"])
		from(configurations["commonResources"])
	}
}
