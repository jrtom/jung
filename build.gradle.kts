plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless) apply false
}

allprojects {
    group = "net.sf.jung"
    version = "3.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }

    tasks.withType<Test> {
        useJUnit()
    }

    dependencies {
        "implementation"(rootProject.libs.kotlin.stdlib)
    }

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            googleJavaFormat(libs.versions.google.java.format.get())
        }
        kotlin {
            ktlint()
        }
    }
}
