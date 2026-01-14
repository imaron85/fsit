plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.mod.publish)
    alias(libs.plugins.shadow)
}

private val gitVersion = providers.exec {
    executable = "git"
    args = listOf("describe", "--tags", "--always", "--dirty")
}.standardOutput.asText.map { it.trim().drop(1) }.get()

version = "$gitVersion+mc${stonecutter.current.project}"
group = "dev.rvbsm"
base.archivesName = rootProject.name

loom {
    splitEnvironmentSourceSets()
    mods.register(name) {
        sourceSet("main")
        sourceSet("client")
    }

    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run"
    }
}

sourceSets.test {
    compileClasspath += sourceSets["client"].compileClasspath
    runtimeClasspath += sourceSets["client"].runtimeClasspath
}

repositories {
    mavenCentral()

    maven("https://maven.terraformersmc.com/releases") {
        content {
            includeGroup("com.terraformersmc")
        }
    }

    maven("https://maven.isxander.dev/releases") {
        content {
            includeGroup("dev.isxander")
            includeGroup("org.quiltmc.parsers")
        }
    }

    maven("https://repo.opencollab.dev/main") {
        content {
            includeGroupByRegex("org\\.geysermc\\..*")
            includeGroup("org.cloudburstmc.math")
        }
    }
}

dependencies {
    minecraft("com.minecraft:minecraft:${property("minecraft.version")}")

    implementation(libs.fabric.loader)

    val fabricVersion = property("api.fabric-api").toString()
    setOf(
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-key-mapping-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1",
    ).map { fabricApi.module(it, fabricVersion) }.forEach(::implementation)

    implementation("com.terraformersmc:modmenu:${property("api.modmenu")}")
    //implementation("me.lucko:fabric-permissions-api:${property("api.fabric-permissions-api")}")
    implementation("me.lucko:fabric-permissions-api") {
        version {
            branch = "master"
        }
    }
    compileOnly(libs.geyser.api)

    implementation(libs.bundles.configurate)
    shadow(libs.bundles.configurate)

    testImplementation(libs.fabric.loader.junit)
}

tasks {
    processResources {
        val properties = mapOf(
            "version" to "$version",
            "minecraftVersion" to ">=${project.property("minecraft.version")}-",
            "javaVersion" to project.property("java.version"),
        )

        inputs.properties(properties)
        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    withType<JavaCompile> {
        options.release = project.property("java.version").toString().toInt()
    }

    shadowJar {
        archiveClassifier = "all"
        configurations = project.configurations.shadow.map(::listOf)

        enableAutoRelocation = true
        relocationPrefix = "dev.rvbsm.fsit.lib"

        exclude("META-INF")
        dependencies {
            exclude(dependency(":gson:.*"))
        }

        minimize()
    }

    jar {
        dependsOn(shadowJar)
        mustRunAfter(shadowJar)
        from(zipTree(shadowJar.flatMap { it.archiveFile }))
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(rootProject.file("LICENSE")) {
            into("META-INF")
        }
    }

    test {
        useJUnitPlatform()
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.toVersion(project.property("java.version")!!)
    targetCompatibility = JavaVersion.toVersion(project.property("java.version")!!)
}

stonecutter {}

publishMods {
    file = tasks.jar.map { it.archiveFile }.get()
    // additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile })
    changelog = providers.environmentVariable("CHANGELOG")
    type = when {
        "alpha" in gitVersion -> ALPHA
        "beta" in gitVersion -> BETA
        else -> STABLE
    }
    displayName = "[${stonecutter.current.project}] v$gitVersion"
    modLoaders.addAll("fabric", "quilt")

    dryRun = !providers.environmentVariable("MODRINTH_TOKEN").isPresent

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("mod.modrinth_id")
        featured = true

        minecraftVersionRange {
            start = stonecutter.current.project
            end = project.property("minecraft.version").toString()
        }

        requires("fabric-api")
        optional("modmenu", "yacl")
    }
}
