description = "Sample programs using JUNG"

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
        attributes("Main-Class" to "edu.uci.ics.jung.samples.NodeImageShaperDemo")
        attributes("Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name })
    }
}
