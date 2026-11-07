import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

fun Project.configure(loader: String) {
	extensions.getByType(BasePluginExtension::class.java).archivesName.set(p("modId"))
	group = p("modGroupId")
	version = "${p("mcVersion")}-${p("modVersion")}-$loader"
	val generateMetadata = tasks.register<Copy>("generateMetadata") {
		description = "Expand mod metadata templates from gradle.properties."
		val values = project.extra.properties.mapValues { it.value.toString() }
		inputs.properties(values)
		expand(values)
		from(layout.projectDirectory.dir("src/main/templates"))
		into(layout.buildDirectory.dir("generated/sources/modMetadata"))
	}
	extensions.getByType(SourceSetContainer::class.java).named("main") {
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
		val publishJar = tasks.named(if(loader == "fabric") "remapJar" else "jar", AbstractArchiveTask::class.java)
		file.set(publishJar.flatMap { it.archiveFile })
		additionalFiles.from(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile })
		changelog.set(rootProject.file("CHANGELOG.md").readText())
		type.set(ReleaseType.BETA)
		version.set(project.version.toString())
		displayName.set("[$loader] ${p("modVersion")}")
		modLoaders.add(loader)
		github {
			repository.set("ForgeStove/FlexConfig")
			commitish.set(providers.environmentVariable("GITHUB_REF_NAME").orElse(p("mcVersion")))
			accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
		}
	}
}

fun Project.p(key: String) = property(key).toString()
