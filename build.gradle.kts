@file:Suppress("UnusedImport", "SpellCheckingInspection")

import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    kotlin("jvm") version "1.3.60"
    id("org.beryx.jlink") version "2.17.0"
    id("com.github.gmazzo.buildconfig") version "1.6.2"
}

repositories {
    mavenCentral()
    jcenter()
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/release") }
}

group = "pathplotter"
version = "2020.2.0"

buildConfig {
    packageName("ca.warp7.pathplotter")
    buildConfigField("String", "kVersion", "\"${version}\"")
}

application {
    mainClassName = "ca.warp7.pathplotter.Main"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
                "-Xno-call-assertions",
                "-Xno-param-assertions"
        )
        kotlinOptions.jvmTarget = "11"
    }
}

tasks.compileKotlin {
    destinationDir = tasks.compileJava.get().destinationDir
}

tasks.withType<Test> {
    useJUnitPlatform {
    }
}

val wpilibVersion = "2020.2.2"

fun desktopArch(): String {
    val arch: String = System.getProperty("os.arch")
    return if (arch == "amd64" || arch == "x86_64") "x86-64" else "x86"
}

fun desktopOS(): String {
    return when {
        OperatingSystem.current().isWindows -> "windows"
        OperatingSystem.current().isMacOsX -> "osx"
        else -> "linux"
    }
}

val platform =  desktopOS() + desktopArch()

tasks.compileJava {
    doFirst {
        options.compilerArgs.addAll(listOf(
                "--module-path", classpath.asPath,
                "--add-modules", "pathplotter"
        ))
        classpath = files()
    }
}

tasks.jar {
    this.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("edu.wpi.first.wpilibj:wpilibj-java:$wpilibVersion")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:$wpilibVersion")
    implementation("edu.wpi.first.ntcore:ntcore-java:$wpilibVersion")
    implementation("edu.wpi.first.ntcore:ntcore-jni:$wpilibVersion:$platform")

    implementation("org.openjfx:javafx-base:13:win")
    implementation("org.openjfx:javafx-graphics:13:win")
    implementation("org.openjfx:javafx-controls:13:win")
    implementation("org.kordamp.ikonli:ikonli-javafx:11.3.5")
    implementation("org.kordamp.ikonli:ikonli-materialdesign-pack:11.3.5")
    implementation("org.json:json:20190722")

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.6.0")

    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.6.0")
    testRuntimeOnly(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.6.0")
}

jlink {
    this.launcher {
        this.jvmArgs = listOf("--add-reads", "pathplotter.merged.module=pathplotter")
    }
    options.addAll("--strip-debug", "--no-header-files",
            "--no-man-pages")
}