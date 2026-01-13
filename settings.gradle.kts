rootProject.name = "tarnish-client"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.runelite.net")
        jcenter()
        maven("https://jitpack.io")
        maven("https://repo.spongepowered.org/maven")
    }
}

pluginManagement {
    repositories {
        maven("https://plugins.gradle.org/m2/")
    }
    plugins {
        id("com.github.johnrengelman.shadow") version "8.1.1"
    }
}
