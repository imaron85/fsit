plugins {
    id("net.fabricmc.fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
    id("com.gradleup.shadow")
}

version = "${rootProject.version}+mc${stonecutter.current.project}"
base {
    archivesName = rootProject.name
}

loom {
    splitEnvironmentSourceSets()

    mods.register(rootProject.name) {
        sourceSet("main")
        sourceSet("client")
    }

    runs.all {
        isIdeConfigGenerated = true
        runDir = "../../run"
        property("mixin.debug.export", "true")
    }
}

stonecutter {}

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

    maven("https://repo.opencollab.dev/main") {
        content {
            includeGroupByRegex("org\\.geysermc\\..*")
            includeGroup("org.cloudburstmc.math")
        }
    }
}

configurations.implementation {
    extendsFrom(configurations.shadow.get())
}

dependencies {
    minecraft("com.minecraft:minecraft:${property("version.minecraft")}")

    implementation("net.fabricmc:fabric-loader:${property("version.fabric-loader")}")
    setOf(
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-events-interaction-v0",
        "fabric-key-mapping-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1",
    ).map { fabricApi.module(it, property("version.fabric-api").toString()) }.forEach(::implementation)

    shadow("org.snakeyaml:snakeyaml-engine:${property("version.snakeyaml")}")

    implementation("com.terraformersmc:modmenu:${property("version.modmenu")}")
    //implementation("me.lucko:fabric-permissions-api:${property("version.fabric-permissions-api")}")
    implementation("me.lucko:fabric-permissions-api") { version { branch = "master" } }

    testImplementation("net.fabricmc:fabric-loader-junit:${property("version.fabric-loader")}")
}

tasks.processResources {
    val properties = mapOf(
        "version" to version,
        "java" to project.property("version.java"),
        "minecraft" to stonecutter.current.project,
    )

    inputs.properties(properties)
    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

tasks.shadowJar {
    configurations = project.configurations.shadow.map(::listOf)

    enableAutoRelocation = true
    relocationPrefix = "dev.rvbsm.fsit.lib"

    minimize()
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    from(zipTree(tasks.shadowJar.flatMap { it.archiveFile }))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(rootProject.file("LICENSE")) {
        into("META-INF")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = project.property("version.java").toString().toInt()
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing", "-parameters"))
    options.isFork = true
    options.forkOptions.jvmArgs = listOf("-Dline.separator=\n")
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.toVersion(project.property("version.java")!!)
    targetCompatibility = JavaVersion.toVersion(project.property("version.java")!!)
}

publishMods {
    dryRun = !providers.environmentVariable("MODRINTH_TOKEN").isPresent

    file = tasks.jar.flatMap { it.archiveFile }
    additionalFiles.from(tasks.named<Jar>("sourcesJar").map { it.archiveFile })

    displayName = "[${stonecutter.current.project}] v${rootProject.version}"
    changelog = readChangelog("CHANGELOG.md")
    type = version.map {
        when {
            "alpha" in it -> ALPHA
            "beta" in it -> BETA
            else -> STABLE
        }
    }
    modLoaders.addAll("fabric", "quilt")

    require(changelog.isPresent) { "No changelog for version '${rootProject.version}'" }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = providers.gradleProperty("mod.modrinth_id")
        featured = true

        minecraftVersions.addAll(providers.gradleProperty("mod.minecraft_versions").map { it.split(" ") })

        requires("fabric-api")
        optional("modmenu")
    }
}

fun readChangelog(name: String): Provider<String> = providers.provider {
    rootProject.file(name).useLines { lines ->
        val versionContent = StringBuilder("")
        var isWritingContent = false

        for (line in lines) when {
            line.startsWith("## v${rootProject.version}") -> isWritingContent = true
            isWritingContent && line.startsWith("## v") -> break
            isWritingContent -> versionContent.append(line)
        }

        versionContent.trim().toString()
    }
}
