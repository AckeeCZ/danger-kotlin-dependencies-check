import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.nexus.staging)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt-config.yml"))
    ignoreFailures = false
}

dependencies {
    detektPlugins(libs.detekt.formatting)

    implementation(libs.danger.kotlin.sdk)
    implementation(libs.jackson.xml)
    implementation(libs.jackson.kotlinModule)

    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.framework.engine)
    testRuntimeOnly(libs.kotest.runner.junit5)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(20)

    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-api=strict")
    }
}

sourceSets {
    getByName("main") {
        java.srcDir("src/main/kotlin")
    }
}

ext {
    val properties = Properties().apply { load(file("${rootDir.absolutePath}/lib.properties").inputStream()) }
    set("libProperties", properties)
}

apply(from = "${rootProject.projectDir}/gradle/mavencentral/publish.gradle")
