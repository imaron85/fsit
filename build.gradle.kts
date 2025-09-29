plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.publish)
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
    maven("https://maven.terraformersmc.com/releases")
    maven("https://maven.isxander.dev/releases")
}

dependencies {
    minecraft("com.minecraft:minecraft:${property("minecraft.version")}")
    mappings("net.fabricmc:yarn:${property("minecraft.version")}+build.${property("fabric.yarn_build")}:v2")

    modImplementation(libs.fabric.loader)

    val fabricVersion = property("fabric.api").toString()
    setOf(
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-key-binding-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1",
    ).map { fabricApi.module(it, fabricVersion) }.forEach(::modImplementation)

    modImplementation("com.terraformersmc:modmenu:${property("api.modmenu")}")
    modImplementation("dev.isxander:yet-another-config-lib:${property("api.yacl")}-fabric") {
        exclude("net.fabricmc.fabric-api", "fabric-api")
        modRuntimeOnly("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    }

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

    sourceCompatibility = JavaVersion.toVersion(project.property("java.version")!!)
    targetCompatibility = JavaVersion.toVersion(project.property("java.version")!!)
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
