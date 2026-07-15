pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        if (providers.gradleProperty("useMavenLocal").orNull == "true" ||
            providers.environmentVariable("USE_MAVEN_LOCAL").orNull == "true") {
            mavenLocal()
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "Mountain Markers"
include(":app")
