plugins {
    id("net.fabricmc.fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
    id("com.gradleup.shadow")
}

version = "${rootProject.version}+mc${sc.current.project}"
base.archivesName = rootProject.name

loom {
    splitEnvironmentSourceSets()
    accessWidenerPath = rootProject.file("src/main/resources/fsit.classtweaker")

    mods.register(rootProject.name) {
        sourceSet("main")
        sourceSet("client")
    }

    runs.all {
        generateRunConfig = true
        runDirectory = rootProject.file("run")
        systemProperties.put("mixin.debug.export", "true")
        jvmArguments.addAll("-Xmx1G")
    }

    log4jConfigs.from("$rootDir/log4j-dev.xml")
}

stonecutter {
    replacements.string(current.parsed >= "26.2") {
        replace("net.minecraft.advancements.CriteriaTriggers", "net.minecraft.advancements.triggers.CriteriaTriggers")
        replace("EntityType", "EntityTypes")
    }

    replacements.string(current.parsed >= "26.3") {
        replace("InputConstants.Type.KEYSYM", "InputConstants.Type.KEYBOARD")
    }
}

sourceSets.test {
    compileClasspath += sourceSets["client"].compileClasspath
    runtimeClasspath += sourceSets["client"].runtimeClasspath
}

configurations.implementation {
    extendsFrom(configurations.shadow.get())
}

repositories {
    mavenCentral()

    // thx @KikuGie
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }

    strictMaven("https://maven.terraformersmc.com", "TerraformersMC", "com.terraformersmc")
    strictMaven("https://maven.isxander.dev/releases", "Xander", "dev.isxander")
    strictMaven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1", "DevAuth", "me.djtheredstoner")
}

dependencies {
    minecraft("com.minecraft:minecraft:${sc.current.version}")

    implementation("net.fabricmc:fabric-loader:${property("version.fabric-loader")}")
    setOf(
        "fabric-api-base",
        "fabric-command-api-v2",
        "fabric-events-interaction-v0",
        "fabric-key-mapping-api-v1",
        "fabric-lifecycle-events-v1",
        "fabric-networking-api-v1",
        "fabric-permission-api-v1",
    ).map { fabricApi.module(it, sc.properties["version.fabric-api"]) }.forEach(::implementation)

    shadow("org.snakeyaml:snakeyaml-engine:3.2")

    implementation("com.terraformersmc:modmenu:${property("version.modmenu")}") { isTransitive = false }
    implementation("dev.isxander:yet-another-config-lib:${property("version.yacl")}") { isTransitive = false }

    localRuntime("net.fabricmc.fabric-api:fabric-api:${property("version.fabric-api")}")
    localRuntime("me.djtheredstoner:DevAuth-fabric:${property("version.devauth")}")

    testImplementation("net.fabricmc:fabric-loader-junit:${property("version.fabric-loader")}")
}

tasks.processResources {
    val properties = mapOf(
        "version" to version,
        "java" to ">=${java.targetCompatibility}",
        "minecraft" to "~${sc.current.project}-",
    )

    inputs.properties(properties)
    filesMatching("fabric.mod.json") {
        expand(properties)
    }
}

tasks.withType<Jar> {
    destinationDirectory = rootProject.layout.buildDirectory.dir("libs")
}

tasks.jar {
    archiveClassifier = "dev"
    destinationDirectory = rootProject.layout.buildDirectory.dir("devlibs")
}


tasks.shadowJar {
    dependsOn(tasks.jar)
    archiveClassifier = null

    configurations = project.configurations.shadow.map(::listOf)
    enableAutoRelocation = true

    relocationPrefix = "dev.rvbsm.fsit.lib"
    exclude("META-INF/maven/*/**")
    minimize()

    from("$rootDir/LICENSE") {
        into("META-INF")
    }
}

loom.nestJars(tasks.shadowJar, configurations.include)

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 25
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

    displayName = "[${sc.current.project}] v${rootProject.version}"
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
            start = sc.current.project
            end = sc.current.version // will break if start > end
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
            isWritingContent -> versionContent.appendLine(line)
        }

        versionContent.trim().toString()
    }
}
