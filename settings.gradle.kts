rootProject.name = "fsit"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net") { name = "FabricMC" }
    }

    plugins {
        id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT" apply false
        id("me.modmuss50.mod-publish-plugin") version "1.1.0" apply false
        id("com.gradleup.shadow") version "9.3.1" apply false
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9"
}

stonecutter {
    // TODO: data-driven
    create(rootProject) {
        vcsVersion = "26.1"
        versions(
            "26.1",
        )
    }
}

sourceControl {
    gitRepository(uri("https://github.com/sakura-ryoko/fabric-permissions-api.git")) {
        producesModule("me.lucko:fabric-permissions-api")
    }
}
