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
    accessWidenerPath = rootProject.file("src/main/resources/fsit.classtweaker")

    mods.register(rootProject.name) {
        sourceSet("main")
        sourceSet("client")
    }

    runs.all {
        isIdeConfigGenerated = true
        runDir = "../../run"
        property("mixin.debug.export", "true")
        vmArg("-Xmx1G")
    }

    log4jConfigs.from("$rootDir/log4j-dev.xml")
}

stonecutter {}

sourceSets.test {
    compileClasspath += sourceSets["client"].compileClasspath
    runtimeClasspath += sourceSets["client"].runtimeClasspath
}

repositories {
    mavenCentral()

    maven("https://maven.terraformersmc.com/releases") {
        name = "TerraformersMC"
        content {
            includeGroup("com.terraformersmc")
        }
    }

    maven("https://maven.isxander.dev/releases") {
        name = "Xander"
        content {
            includeGroup("dev.isxander")
        }
    }
}

dependencies {
    minecraft("com.minecraft:minecraft:${stonecutter.current.version}")

    implementation("net.fabricmc:fabric-loader:${property("version.fabric-loader")}")
    setOf(
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-events-interaction-v0",
        "fabric-key-mapping-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1",
        // TODO: https://github.com/FabricMC/fabric-api/pull/5226
        // "fabric-permission-api-v1",
    ).map { fabricApi.module(it, property("version.fabric-api").toString()) }.forEach(::implementation)
    implementation("me.lucko:fabric-permissions-api") { version { branch = "26.1-clean" } }

    implementation("org.snakeyaml:snakeyaml-engine:3.0.1")
    shadow("org.snakeyaml:snakeyaml-engine:3.0.1")

    runtimeOnly("net.fabricmc.fabric-api:fabric-api:${property("version.fabric-api")}")
    implementation("com.terraformersmc:modmenu:${property("version.modmenu")}") {
        isTransitive = false
    }
    implementation("dev.isxander:yet-another-config-lib:${property("version.yacl")}") {
        isTransitive = false
    }

    testImplementation("net.fabricmc:fabric-loader-junit:${property("version.fabric-loader")}")
}

tasks.processResources {
    val properties = mapOf(
        "version" to version,
        "java" to ">=${java.targetCompatibility}",
        "minecraft" to "~${stonecutter.current.project}-",
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

    exclude("META-INF/maven/org.snakeyaml/*/**")

    minimize()
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    from(zipTree(tasks.shadowJar.flatMap { it.archiveFile }))
    duplicatesStrategy = DuplicatesStrategy.WARN

    from("$rootDir/LICENSE") {
        into("META-INF")
    }
}

tasks.withType<Jar> {
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = java.targetCompatibility.ordinal + 1
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing", "-parameters"))
    options.isFork = true
    options.forkOptions.jvmArgs = listOf("-Dline.separator=\n")
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
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

        minecraftVersionRange {
            start = stonecutter.current.project
            end = stonecutter.current.version
        }

        requires("fabric-api")
        optional("modmenu", "yacl")
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
