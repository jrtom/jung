pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "jung-parent"

include(
    "jung-api",
    "jung-graph-impl",
    "jung-algorithms",
    "jung-io",
    "jung-visualization",
    "jung-samples"
)
