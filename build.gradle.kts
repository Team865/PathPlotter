@file:Suppress("UnusedImport", "SpellCheckingInspection")

import org.javamodularity.moduleplugin.extensions.TestModuleOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    application
    kotlin("jvm") version "1.3.60"
    id("org.javamodularity.moduleplugin") version "1.6.0"
    id("org.openjfx.javafxplugin") version "0.0.9-SNAPSHOT"
    id("org.beryx.jlink") version "2.17.0"
}

repositories {
    mavenCentral()
    jcenter()
    maven { setUrl("https://jitpack.io") }
    maven { setUrl("https://frcmaven.wpi.edu/artifactory/release") }
}

group = "ca.warp7.planner2"
version = "2020.1.0-alpha-1"

application {
    mainClassName = "path.planner/ca.warp7.planner2.MainKt"
}

javafx {
    modules("javafx.controls")
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

tasks.withType<Test> {
    extensions.configure(TestModuleOptions::class.java) {
        runOnClasspath = true
    }
    useJUnitPlatform {
    }
}

val wpilibVersion = "2020.1.2"

dependencies {
    implementation(kotlin("stdlib"))

    implementation("edu.wpi.first.wpilibj:wpilibj-java:$wpilibVersion")
    implementation("edu.wpi.first.wpiutil:wpiutil-java:$wpilibVersion")
//    implementation(files("ejml-combined-0.38.jar"))

    testImplementation(kotlin("test"))
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.5.1")

    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.5.1")
    testRuntimeOnly(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.5.1")
}

jlink {
    options.addAll("--strip-debug", "--no-header-files",
            "--no-man-pages", "--strip-native-commands")
}