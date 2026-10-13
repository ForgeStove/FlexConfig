plugins {
	`kotlin-dsl`
}
repositories {
	gradlePluginPortal()
}
dependencies {
	implementation("me.modmuss50:mod-publish-plugin:2.2.1") { isTransitive = false }
}
