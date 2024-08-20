import tech.antibytes.gradle.dependency.settings.fullCache

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        val antibytesPlugins = "^tech\\.antibytes\\.[\\.a-z\\-]+"
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            setUrl("https://raw.github.com/bitPogo/maven-snapshots/main/snapshots")
            content {
                includeGroupByRegex(antibytesPlugins)
            }
        }
        maven {
            setUrl("https://raw.github.com/bitPogo/maven-rolling-releases/main/rolling")
            content {
                includeGroupByRegex(antibytesPlugins)
            }
        }
    }
}

plugins {
    id("tech.antibytes.gradle.dependency.settings") version "283c93a"
}

includeBuild("setup")

dependencyResolutionManagement {
    versionCatalogs {
        create("dependencyCatalog") {
            from(files("./gradle/dependency.version.toml"))
        }
        getByName("antibytesCatalog") {
            version("android-coil-core", "2.6.0")
            version("android-coil-compose", "2.6.0")
        }
    }
}

include(
    ":feature:recent:data"
)

buildCache {
    fullCache(rootDir)
}

rootProject.name = "XkcdDataHubClient"