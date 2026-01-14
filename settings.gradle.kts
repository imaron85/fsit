rootProject.name = "fsit"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8.2"
}

stonecutter {
    create(rootProject) {
        versions("1.21", "1.21.2", "1.21.4", "1.21.5", "1.21.6", "1.21.9")
        vcsVersion = "1.21.9"
    }
}
