plugins {
    java
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.lombok") version "2.1.10"
    id("io.freefair.lombok") version "8.10.2"
    id("com.gradleup.shadow") version "9.0.0-beta4"
}

repositories {
    mavenLocal()
//    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://maven.aliyun.com/nexus/content/groups/public/")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots"
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    compileOnly(fileTree(baseDir = "lib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
}

group = "cn.irina"
version = "1.0"
description = "SkyPVPTools"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    jvmToolchain(11)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-nowarn")
    }

    shadowJar {
        // Delete kotlin metadata
        exclude("**/*.kotlin_metadata")
        exclude("**/*.kotlin_module")
        exclude("**/*.kotlin_builtins")
        exclude("io/netty/**")

        minimize()
        minimize {
            exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:.*"))
        }

        relocate("kotlin", "cn.irina.thirdparty.kotlin")

        archiveFileName.set("SkyPVPTools.jar")
    }

    jar {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }
}
