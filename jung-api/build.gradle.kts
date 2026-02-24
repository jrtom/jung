description = "Graph interfaces used by the JUNG project"

dependencies {
    api(libs.guava)

    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.guava.testlib)
}

// Expose test classes for consumption by other modules (equivalent to Maven's test-jar)
val testJar by tasks.registering(Jar::class) {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
}

configurations {
    create("testArtifacts") {
        extendsFrom(configurations.testImplementation.get())
    }
}

artifacts {
    add("testArtifacts", testJar)
}
