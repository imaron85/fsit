rootProject.name = "fsit"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net") {
            name = "FabricMC"
        }
    }

    plugins {
        id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
        id("me.modmuss50.mod-publish-plugin") version "1.1.0"
        id("com.gradleup.shadow") version "9.3.1"
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.3"
}

stonecutter {
    create(rootProject) {
        versions("26.1")
        vcsVersion = "26.1"
    }
}

sourceControl {
    gitRepository(uri("https://github.com/reusteur73/fabric-permissions-api.git")) {
        producesModule("me.lucko:fabric-permissions-api")
    }
}
