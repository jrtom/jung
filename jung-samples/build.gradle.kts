plugins {
    application
}

description = "Sample programs using JUNG"

val defaultMainClass = "edu.uci.ics.jung.samples.ShowLayouts"

application {
    mainClass.set(project.findProperty("mainClass") as String? ?: defaultMainClass)
}

dependencies {
    implementation(project(":jung-api"))
    implementation(project(":jung-graph-impl"))
    implementation(project(":jung-algorithms"))
    implementation(project(":jung-io"))
    implementation(project(":jung-visualization"))
    implementation(libs.logback.classic)
    implementation(libs.logback.core)
}

tasks.jar {
    manifest {
        attributes("Main-Class" to defaultMainClass)
    }
}
