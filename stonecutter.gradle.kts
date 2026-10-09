plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.21.1-neoforge"
stonecutter parameters {
	val (version, loader) = current.project.split('-', limit = 2)
	// 让 stonecutter.properties.toml 里的 ["<MC版本>"] / [<loader>."<MC版本>"] 标签在该变体下生效
	properties { tags(version, loader) }
}