rootProject.name = "fsit"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net") { name = "FabricMC" }
    }

    plugins {
        id("net.fabricmc.fabric-loom") version "1.18-SNAPSHOT" apply false
        id("me.modmuss50.mod-publish-plugin") version "2.2.1" apply false
        id("com.gradleup.shadow") version "9.6.1" apply false
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.8"
}

stonecutter {
    create(rootProject) {
        load(file("versions.json"))
    }
}
