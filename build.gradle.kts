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

    modCompileOnly("me.lucko:fabric-permissions-api:${property("api.fabric-permissions")}") {
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
        archiveClassifier = "all"
        configurations = project.configurations.shadow.map(::listOf)

        enableAutoRelocation = true
        relocationPrefix = "dev.rvbsm.fsit.lib"

        dependencies {
            exclude(dependency(":gson:.*"))
        }

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

stonecutter {
    val trickyTrials = eval(current.version, ">=1.21")
    val bundlesOfBravery = eval(current.version, ">=1.21.2")
    val chaseTheSkies = eval(current.version, ">=1.21.6")
    val theCopperAge = eval(current.version, ">=1.21.9")

    replacements.string(bundlesOfBravery) {
        from = "PacketCodec.tuple(PacketCodecs.DOUBLE, Vec3d::getX, PacketCodecs.DOUBLE, Vec3d::getY, PacketCodecs.DOUBLE, Vec3d::getZ, Vec3d::new)"
        to = "Vec3d.PACKET_CODEC"
    }

    replacements.regex(bundlesOfBravery) {
        replace("PacketCodecs\\.BOOL" + "(?!EAN)", "PacketCodecs\\.BOOLEAN")
        reverse("PacketCodecs\\.BOOLEAN", "PacketCodecs\\.BOOL")
    }

    replacements.string(bundlesOfBravery) {
        from = "import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket"
        to = "import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket"
    }

    replacements.string(bundlesOfBravery) {
        from = "new EntityPositionS2CPacket(this)"
        to = "EntityPositionSyncS2CPacket.create(this)"
    }

    replacements.string(chaseTheSkies) {
        from = "ValueLists.createIdToValueFunction"
        to = "ValueLists.createIndexToValueFunction"
    }

    replacements.regex(theCopperAge) {
        replace("((?:entity|player|this)(?:\\.|::))" + "get(World|Pos)" + "(?!\\w)", "$1getEntity$2")
        reverse("((?:entity|player|this)(?:\\.|::))" + "getEntity(World|Pos)" + "(?!\\w)", "$1get$2")
    }

    replacements.regex(theCopperAge) {
        replace("KeyBinding\\.(\\w+)_CATEGORY", "KeyBinding.Category.$1")
        reverse("KeyBinding\\.Category.(\\w+)", "KeyBinding.$1_CATEGORY")
    }

    replacements.regex(theCopperAge) {
        replace("(\\w+Key)\\.getTranslationKey", "$1\\.getId")
        reverse("(\\w+Key)\\.getId", "$1\\.getTranslationKey")
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
