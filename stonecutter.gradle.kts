plugins {
    id("dev.kikugie.stonecutter")

    alias(libs.plugins.fabric.loom) apply false
    alias(libs.plugins.publish) apply false
    alias(libs.plugins.shadow) apply false
}

stonecutter active "1.21.9" /* [SC] DO NOT EDIT */
