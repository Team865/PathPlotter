pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        maven {
            setUrl("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }
}

rootProject.name = "Path-Planner"