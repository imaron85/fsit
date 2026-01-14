import java.net.URI

rootProject.name = "fsit"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
    }
}

sourceControl {
    gitRepository(URI("https://github.com/reusteur73/fabric-permissions-api.git")) {
        producesModule("me.lucko:fabric-permissions-api")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.2"
}

stonecutter {
    create(rootProject) {
        versions("26.1")
        vcsVersion = "26.1"
    }
}
