package flexconfig

import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.language.jvm.tasks.ProcessResources

/** 开发期演示代码的包路径：只在 runClient 里生效，不参与打包。 */
private const val DEMO_PACKAGE = "io/github/forgestove/flexconfig/demo/**"

/**
 * 各加载器构建脚本的公共配置，供 `build.<loader>.gradle.kts` 调用。
 * [loader] 决定专属源码/模板目录（`src/<loader>/...`）。
 *
 * 用 `extensions.getByType(...)` 而非 Kotlin DSL 访问器：`.kt` 文件没有那些访问器。
 */
@Suppress("unused")
fun Project.configureCommon(loader: String) {
	extensions.getByType(BasePluginExtension::class.java).archivesName.set(p("modId"))
	group = p("modGroupId")
	version = "${p("mcVersion")}-${p("modVersion")}-$loader"
	extensions.getByType(JavaPluginExtension::class.java).apply {
		toolchain.languageVersion.set(JavaLanguageVersion.of(p("javaVersion")))
		withSourcesJar()
	}
	tasks.named<Jar>("jar") {
		from(rootProject.file("LICENSE"))
		exclude(DEMO_PACKAGE)
	}
	tasks.named<Jar>("sourcesJar") { exclude(DEMO_PACKAGE) }
	val generateMetadata = tasks.register<ProcessResources>("generateMetadata") {
		description = "Generate this project metadata from templates."
		val values = project.extensions.extraProperties.properties.mapValues { it.value.toString() }
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
}
