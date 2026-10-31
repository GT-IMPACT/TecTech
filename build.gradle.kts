import settings.getVersionMod

plugins {
    alias(libs.plugins.setup.minecraft)
    alias(libs.plugins.setup.publish)
    id(libs.plugins.buildconfig.get().pluginId)
}

val modId: String by extra
val modName: String by extra
val modGroup: String by extra

extra.set("modVersion", getVersionMod())

sourceSets {
    main {
        java.srcDir("AVRcore/src")
    }
}

repositories {
    maven("https://maven.accident.space/repository/maven-public/") {
        mavenContent {
            includeGroup("space.impact")
            includeGroup("com.github.GTNewHorizons")
            includeGroup("com.github.Azanor")
            includeGroupByRegex("space\\.impact\\..+")
        }
        credentials {
            username = System.getenv("MAVEN_USER") ?: "NONE"
            password = System.getenv("MAVEN_PASSWORD") ?: "NONE"
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    api("com.github.GTNewHorizons:modularui:1.1.24:dev") {
        isTransitive = false
    }

    api("space.impact:ImpactStructures:1.1.0:dev")
    api("space.impact:Impact-Core:1.1.1.0:dev") {
        exclude(
            group = "curse.maven",
            module = "journeymap-32274",
        )
    }
    api("space.impact:gregtech-impact:5.09.36.1:dev") { isTransitive = false }
    compileOnly("space.impact:OpenComputers:1.7.5.7-impact")

    // maven impact
    api("com.github.GTNewHorizons:CodeChickenCore:1.4.21:dev")
    api("com.github.GTNewHorizons:NotEnoughItems:2.6.0-GTNH:dev")
    implementation(rfg.deobf("com.github.Azanor:Baubles:1.0.1.12"))
    implementation("com.github.GTNewHorizons:YAMCore:0.5.79")
    implementation("com.github.GTNewHorizons:waila:1.7.3:dev") { isTransitive = false }
    implementation("com.github.GTNewHorizons:EnderCore:0.2.7:dev") { isTransitive = false }
    implementation("com.github.GTNewHorizons:Applied-Energistics-2-Unofficial:rv3-beta-1082-GTNH:dev") { isTransitive = false }

    // other
    api("net.industrial-craft:industrialcraft-2:2.2.828-experimental:dev")

    compileOnly(rfg.deobf("curse.maven:thaumcraft-223628:2227552"))
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}
