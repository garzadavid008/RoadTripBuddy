pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven {
            url = uri("https://repositories.tomtom.com/artifactory/maven")
        }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // TomTom repo ONLY for TomTom artifacts
        exclusiveContent {
            forRepository {
                maven { url = uri("https://repositories.tomtom.com/artifactory/maven") }
            }
            filter {
                includeGroupByRegex("com\\.tomtom.*")   // nothing else will look here
            }
        }
    }
}

rootProject.name = "RoadTripBuddy"
include(":app")
