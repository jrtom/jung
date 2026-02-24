description = "IO support classes for JUNG"

dependencies {
    api(project(":jung-api"))
    api(project(":jung-algorithms"))

    testImplementation(project(":jung-graph-impl"))
    testImplementation(libs.junit)
}
