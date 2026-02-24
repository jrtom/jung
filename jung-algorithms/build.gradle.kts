description = "Algorithms for the JUNG project"

dependencies {
    api(project(":jung-api"))
    implementation(libs.slf4j.api)

    testImplementation(project(":jung-graph-impl"))
    testImplementation(libs.junit)
    testRuntimeOnly(libs.logback.classic)
    testRuntimeOnly(libs.logback.core)
}
