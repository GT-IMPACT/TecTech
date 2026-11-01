plugins {
    id("minecraft-setup")
    id("publish-setup")
}

impactMod {
    modId.set("tectech")
    modName.set("TecTech - Tec Technology!")
    modGroup.set("com.github.technus")
    mavenGroup.set("space.impact")
    mavenUrl.set("https://maven.accident.space/repository/maven-releases/")
}

buildConfig {
    packageName("com.github.technus.tectech")
    useJavaOutput()
    buildConfigField(
        type = "String",
        name = "VERSION",
        expression = "\"${project.version}\"",
    )
}

sourceSets {
    main {
        java.srcDir("AVRcore/src")
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    api("com.github.GTNewHorizons:modularui:1.1.24:dev") {
        isTransitive = false
    }

    api("space.impact:ImpactStructures:1.1.3:dev")
    api("space.impact:Impact-Core:1.1.1.0:dev") {
        exclude(
            group = "curse.maven",
            module = "journeymap-32274",
        )
    }
    api("space.impact:gregtech-impact:5.09.36.3:dev") {
        isTransitive = false
    }
    compileOnly("space.impact:OpenComputers:1.7.5.7-impact")

    // maven impact
    api("com.github.GTNewHorizons:CodeChickenCore:1.4.21:dev")
    api("com.github.GTNewHorizons:NotEnoughItems:2.6.0-GTNH:dev")
    implementation(modUtils.deobfuscate("com.github.Azanor:Baubles:1.0.1.12"))
    implementation("com.github.GTNewHorizons:YAMCore:0.5.79")
    implementation("com.github.GTNewHorizons:waila:1.7.3:dev") {
        isTransitive = false
    }
    implementation("com.github.GTNewHorizons:EnderCore:0.2.7:dev") {
        isTransitive = false
    }
    implementation("com.github.GTNewHorizons:Applied-Energistics-2-Unofficial:rv3-beta-1082-GTNH:dev") {
        isTransitive = false
    }

    // other
    api("net.industrial-craft:industrialcraft-2:2.2.828-experimental:dev")

    compileOnly(modUtils.deobfuscate("curse.maven:thaumcraft-223628:2227552"))
    testImplementation(modUtils.deobfuscate("curse.maven:thaumcraft-223628:2227552"))
    compileOnly(fileTree("libs") {
        include("*.jar")
    })
}

tasks.test {
    useJUnit()
    // Forge 1.7.10 tests require the Java 8 URLClassLoader.
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    })
}
