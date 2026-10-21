plugins {
	`kotlin-dsl`
}
repositories {
	gradlePluginPortal()
}
dependencies {
	// https://plugins.gradle.org/m2/me/modmuss50/mod-publish-plugin/me.modmuss50.mod-publish-plugin.gradle.plugin/maven-metadata.xml
	implementation("me.modmuss50:mod-publish-plugin:2.2.1") { isTransitive = false }
}
