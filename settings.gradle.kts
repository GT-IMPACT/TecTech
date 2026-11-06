pluginManagement {
    includeBuild("remote-build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://nexus.gtnewhorizons.com/repository/public/") {
            content {
                includeGroupByRegex("com\\.gtnewhorizons(\\..*)?")
            }
        }
        maven("https://maven.minecraftforge.net")
    }
}

plugins {
    id("settings-setup")
}

rootProject.name = "TecTech"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("remote-build-logic/libs.versions.toml"))
        }
    }
}

// Compile against the same GT and Core sources used by the local game launch.
val gregtechProject = file(providers.gradleProperty("gregtechProjectDir").getOrElse("../gt-worktree"))
if (gradle.parent == null && gregtechProject.resolve("settings.gradle.kts").isFile) {
    includeBuild(gregtechProject) {
        name = "gregtech"
        dependencySubstitution {
            substitute(module("space.impact:gregtech-impact"))
                .using(project(":"))
        }
    }
}

val impactCoreProject = file(providers.gradleProperty("impactCoreProjectDir").getOrElse("../impact-core-worktree"))
if (gradle.parent == null && impactCoreProject.resolve("settings.gradle.kts").isFile) {
    includeBuild(impactCoreProject) {
        name = "impact-core"
        dependencySubstitution {
            substitute(module("space.impact:Impact-Core"))
                .using(project(":"))
        }
    }
}
