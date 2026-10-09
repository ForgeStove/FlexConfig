/*
 * stonecutter 的 split buildscript 模式下，本文件不参与构建（root 的构建脚本被变体脚本顶替）：
 * 每个加载器由 `build.<loader>.gradle.kts` 独立构建，各带自己的 moddev/loom 插件。
 * 共享工具（p()）在 buildSrc；按版本/加载器变化的属性在 stonecutter.properties.toml。
 *
 * 源码布局：
 *   src/main/java      共享（配置树、序列化、锁、界面、Platform SPI）
 *   src/neoforge/java  NeoForge 专属（NeoForgePlatform、ConfigEvents）
 *   src/fabric/java    Fabric 专属（FabricPlatform、FabricPayloads、FabricEvents、FabricClientEvents）
 */
