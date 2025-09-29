plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.publish)
    alias(libs.plugins.shadow)
}

private val gitVersion: String by rootProject.ext
private val minecraftProjectVersion = stonecutter.current.project
private val minecraftTargetVersion = stonecutter.current.version
private val isMinecraftVersionRange = stonecutter.eval(minecraftTargetVersion, ">$minecraftProjectVersion")

private val javaVersion = property("java.version").toString().toInt(10)
private val modrinthId = property("mod.modrinth_id").toString()

version = "$gitVersion+mc$minecraftProjectVersion"
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
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.nucleoid.xyz/")
}

dependencies {
    minecraft("com.minecraft:minecraft:$minecraftTargetVersion")
    mappings("net.fabricmc:yarn:$minecraftTargetVersion+build.${property("fabric.yarn_build")}:v2")

    modImplementation(libs.fabric.loader)

    setOf(
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-key-binding-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1",
    ).map { fabricApi.module(it, property("fabric.api").toString()) }.forEach(::modImplementation)
    modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:${property("fabric.api")}")

    modImplementation("com.terraformersmc:modmenu:${property("api.modmenu")}")
    modImplementation("dev.isxander:yet-another-config-lib:${property("api.yacl")}-fabric") {
        exclude("net.fabricmc.fabric-api", "fabric-api")
    }

    implementation(libs.bundles.configurate)
    shadow(libs.bundles.configurate)

    testImplementation(libs.fabric.loader.junit)
}

tasks {
    processResources {
        val properties = mapOf(
            "version" to "$version",
            "minecraftVersion" to ">=$minecraftProjectVersion-",
            "javaVersion" to javaVersion,
        )

        inputs.properties(properties)
        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    withType<JavaCompile> {
        options.release.set(project.property("java.version").toString().toInt())
    }

    shadowJar {
        from(jar)

        archiveClassifier = "all"

        configurations = project.configurations.shadow.map(::listOf)


        minimize()
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
    }

    jar {
        from(rootProject.file("LICENSE")) {
            into("META-INF")
        }
    }

    if (stonecutter.current.isActive) register("buildActive") {
        group = "project"
        dependsOn(build)
    }

    test {
        useJUnitPlatform()
    }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

}

publishMods {
    file = tasks.remapJar.map { it.archiveFile }.get()
    additionalFiles.from(tasks.remapSourcesJar.map { it.archiveFile })
    changelog = providers.environmentVariable("CHANGELOG")
    type = when {
        "alpha" in gitVersion -> ALPHA
        "beta" in gitVersion -> BETA
        else -> STABLE
    }
    displayName = "[$minecraftProjectVersion] v$gitVersion"
    modLoaders.addAll("fabric", "quilt")

    dryRun = !providers.environmentVariable("MODRINTH_TOKEN").isPresent

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = modrinthId
        featured = true

        minecraftVersionRange {
            start = minecraftProjectVersion
            end = minecraftTargetVersion
        }

        requires("fabric-api")
        optional("modmenu", "yacl")
    }
}
