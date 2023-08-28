plugins {
    val kotlinVersion: String by System.getProperties()

    java
    kotlin("jvm") version kotlinVersion
    id("fabric-loom") version "1.0.+"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

group = "com.nat3z.qoluxe"
// TODO: THIS IS WHERE YOU UPDATE THE VERSION
version = "v1.1"


val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

repositories {
    maven {
        url = uri("https://maven.terraformersmc.com/")
    }  // Repository for mod menu
    mavenCentral()
}
val minecraftVersion: String by project

dependencies {
    val kotlinVersion: String by System.getProperties()
    val yarnVersion: String by project
    val loaderVersion: String by project
    val fabricVersion: String by project
    val fabricKotlinVersion: String by project

    implementation(kotlin("stdlib-jdk8", kotlinVersion))
    implementation("org.yaml:snakeyaml:2.2")
    implementation("commons-io:commons-io:2.13.0")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")
    implementation("org.java-websocket:Java-WebSocket:1.5.3")

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$yarnVersion:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.9.5+kotlin.1.8.22")
    modImplementation("com.terraformersmc:modmenu:7.1.0") {
        exclude(group = "net.fabricmc.fabric-api", module = "fabric-api")
    }
}

tasks.withType<JavaCompile> {
    options.release.set(17)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks {
    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "version" to project.version
                )
            )
        }
    }
}


tasks.shadowJar {
    dependencies {
        include(dependency("org.yaml:snakeyaml:2.2"))
        include(dependency("commons-io:commons-io:2.13.0"))
        // include all dependencies starting with org.apache
        include(dependency("org.apache.httpcomponents.client5:httpclient5:5.2.1"))
        include(dependency("org.apache.httpcomponents.core5:httpcore5:5.2.2"))
        include(dependency("org.apache.httpcomponents.core5:httpcore5-h2:5.2.2"))
        include(dependency("org.apache.httpcomponents:httpclient:4.5.14"))
        include(dependency("org.apache.httpcomponents:httpcore:4.4.16"))

        relocate("org.apache.hc", "com.nat3z.qoluxe.hc")
        relocate("org.apache.commons.io", "com.nat3z.qoluxe.commons.io")
        relocate("org.yaml.snakeyaml", "com.nat3z.qoluxe.snakeyaml")
    }
    doLast {
        val version = project.version.toString()
        val file = File("$buildDir/resources/main/version.txt")
        file.writeText(version)
    }
}
tasks.remapJar {
    dependsOn(tasks.shadowJar)
    inputFile.set(tasks.shadowJar.get().archiveFile)
}
tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}
