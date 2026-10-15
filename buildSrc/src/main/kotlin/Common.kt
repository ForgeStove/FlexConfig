@file:Suppress("unused", "KotlinPrintToLogpoint")

import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.jvm.tasks.ProcessResources

fun Project.configure(loader: String) {
	extensions.getByType(BasePluginExtension::class.java).archivesName.set(p("modId"))
	group = p("modGroupId")
	version = "${p("mcVersion")}-${p("modVersion")}-$loader"
	extensions.getByType(JavaPluginExtension::class.java).apply {
		toolchain.languageVersion.set(JavaLanguageVersion.of(p("javaVersion")))
		withSourcesJar()
	}
	tasks.named<Jar>("jar") {
		from(rootProject.file("LICENSE"))
	}
	val generateMetadata = tasks.register<ProcessResources>("generateMetadata") {
		description = "Generate this project metadata from templates."
		val values = project.extra.properties.mapValues { it.value.toString() }
		inputs.properties(values)
		expand(values)
		from(rootProject.file("src/$loader/templates"))
		into(layout.buildDirectory.dir("generated/sources/modMetadata"))
	}
	extensions.getByType(SourceSetContainer::class.java).named("main") {
		java.srcDir(rootProject.file("src/$loader/java"))
		resources.srcDir(generateMetadata)
	}
	repositories.apply {
		mavenLocal()
		mavenCentral()
	}
	println(buildString {
		append("Java: ")
		append(System.getProperty("java.version"))
		append(", JVM: ")
		append(System.getProperty("java.vm.version"))
		append(" (")
		append(System.getProperty("java.vendor"))
		append("), Arch: ")
		append(System.getProperty("os.arch"))
	})
	extensions.getByType(ModPublishExtension::class.java).apply {
		file.set(tasks.named<Jar>("jar").flatMap { it.archiveFile })
		additionalFiles.from(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile })
		changelog.set(rootProject.file("CHANGELOG.md").readText())
		type.set(ReleaseType.BETA)
		version.set(project.version.toString())
		displayName.set("[$loader] ${p("modVersion")}")
		modLoaders.add(loader)
		github {
			repository.set("ForgeStove/FlexConfig")
			commitish.set("main")
			accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
		}
	}
}

fun Project.p(key: String) = extra[key].toString()
