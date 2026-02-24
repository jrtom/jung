description = "Graph implementations for the JUNG project"

dependencies {
    api(project(":jung-api"))

    testImplementation(project(path = ":jung-api", configuration = "testArtifacts"))
    testImplementation(libs.junit)
}
