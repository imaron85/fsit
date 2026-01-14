plugins {
    id("dev.kikugie.stonecutter")

    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.mod.publish) apply false
    alias(libs.plugins.shadow) apply false
}

stonecutter active "26.1" /* [SC] DO NOT EDIT */
