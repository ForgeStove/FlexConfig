package flexconfig

import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.named

/** 配置 mod-publish-plugin 的发布信息，供各 `build.<loader>.gradle.kts` 共用。 */
@Suppress("unused")
fun Project.configurePublishMods(loader: String) {
	val modVersion = p("modVersion")
	extensions.getByType(ModPublishExtension::class.java).apply {
		file.set(tasks.named<Jar>("jar").flatMap { it.archiveFile })
		additionalFiles.from(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile })
		changelog.set(rootProject.file("CHANGELOG.md").readText())
		type.set(ReleaseType.BETA)
		version.set(project.version.toString())
		displayName.set("[$loader] $modVersion")
		modLoaders.add(loader)
		github {
			repository.set("ForgeStove/FlexConfig")
			commitish.set("main")
			accessToken.set(providers.environmentVariable("GITHUB_TOKEN"))
		}
	}
}
