package flexconfig

import org.gradle.api.Project

/**
 * 读取构建属性（gradle.properties 与 stonecutter.properties.toml 都会注入到 property container）。
 *
 * 放在 buildSrc 是为了让各 `build.<loader>.gradle.kts` 共用，避免每个脚本各写一份。
 */
fun Project.p(key: String): String = property(key).toString()
