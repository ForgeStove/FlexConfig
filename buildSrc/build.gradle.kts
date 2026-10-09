plugins {
	`kotlin-dsl`
}
repositories {
	mavenCentral()
	gradlePluginPortal()
}
dependencies {
	// 供 configurePublishMods() 引用发布插件的扩展类型；isTransitive=false 避免拖进其 kotlinx-serialization 依赖
	implementation("me.modmuss50:mod-publish-plugin:+") { isTransitive = false }
}
