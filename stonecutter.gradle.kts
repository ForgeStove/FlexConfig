plugins {
	id("dev.kikugie.stonecutter")
}
stonecutter {
	active("1.21.1-neoforge")
	parameters {
		val (version, loader) = current.project.split('-', limit = 2)
		properties { tags(version, loader) }
	}
}
