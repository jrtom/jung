description = "Core visualization support for the JUNG project"

dependencies {
    api(project(":jung-api"))
    api(project(":jung-algorithms"))
    implementation(libs.slf4j.api)

    testImplementation(project(":jung-graph-impl"))
    testImplementation(libs.junit)
    testRuntimeOnly(libs.logback.classic)
    testRuntimeOnly(libs.logback.core)
}
