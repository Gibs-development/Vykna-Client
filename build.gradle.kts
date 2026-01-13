@file:OptIn(ExperimentalStdlibApi::class)

plugins {
    java
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:none")
    options.isFork = true
}

repositories {
    mavenCentral {
        metadataSources {
            mavenPom()
            artifact()
            ignoreGradleMetadataRedirection()
        }
    }
    maven("https://jitpack.io")
    maven("https://repo.runelite.net")
}

sourceSets {
    named("main") {
        java.srcDirs("src/main/java")
        resources.srcDirs(
            "runelite/http-api/src/main/resources",
            "runelite/runelite-client/src/main/resources"
        )
    }
}

/**
 * Hard safety: never allow legacy lwjgl-platform to appear.
 */
configurations.all {
    exclude(group = "org.lwjgl", module = "lwjgl-platform")
}

/**
 * Optional: hard-lock LWJGL version if anything tries to pull another one.
 * (Safe to keep; helps avoid surprises.)
 */
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.lwjgl") {
            useVersion("3.3.3")
        }
    }
}

dependencies {

    /* Core */
    implementation("com.thoughtworks.xstream:xstream:1.4.7")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("com.google.guava:guava:31.1-jre")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("com.google.code.gson:gson:2.9.0")

    /* Jackson */
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.3")

    /* Desktop / UI */
    implementation("com.dorkbox:Notify:3.7")
    implementation("com.intellij:forms_rt:7.0.3")

    /* Reflection */
    implementation("net.oneandone.reflections8:reflections8:0.11.7")

    /* Runelite */
    implementation("net.runelite.pushingpixels:trident:1.5.00")
    implementation("net.runelite.pushingpixels:substance:8.0.02")
    implementation("net.runelite:discord:1.1")
    implementation("com.google.inject:guice:4.2.2")
    implementation("com.squareup.okhttp3:okhttp:4.3.0")

    /* Apache extras */
    implementation("org.apache.commons:commons-csv:1.7")
    implementation("org.apache.commons:commons-text:1.8")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")

    /* Lombok */
    compileOnly("org.projectlombok:lombok:1.18.8")
    annotationProcessor("org.projectlombok:lombok:1.18.8")

    /* GPU Presenter (LWJGL) */
    val lwjglVersion = "3.3.3"

    implementation("org.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-opengl:$lwjglVersion")
    implementation("org.lwjgl:lwjgl-jawt:$lwjglVersion") // NOTE: no natives exist for jawt

    // AWT bridge (exclude LWJGL so it can't reintroduce ${lwjgl.natives} issues)
    implementation("org.lwjglx:lwjgl3-awt:0.2.3") {
        exclude(group = "org.lwjgl")
    }

    // Windows natives ONLY for modules that actually ship natives
    runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:natives-windows")
    runtimeOnly("org.lwjgl:lwjgl-opengl:$lwjglVersion:natives-windows")

    /* Testing */
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.test {
    useJUnitPlatform()
}
