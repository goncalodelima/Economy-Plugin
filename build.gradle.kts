/*
 *
 *  * This file is part of Economy-Plugin - https://github.com/goncalodelima/Economy-Plugin
 *  * Copyright (c) 2026 goncalodelima and contributors
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow") version "9.0.0-rc1"
}

allprojects {
    group = "pt.gongas"
    version = "1.2.9"
    description = "Economy plugin system"
    ext.set("id", "economyplugin")
    ext.set("website", "https://github.com/goncalodelima/Economy-Plugin")
    ext.set("author", "ReeachyZ_")
}

subprojects {

    plugins.apply("maven-publish")

    plugins.withId("com.gradleup.shadow") {
        tasks.withType<ShadowJar>().configureEach {
            archiveClassifier.set("") // remove -all
        }
    }

    tasks.withType<Jar>().configureEach {
        archiveVersion.set(project.version.toString())
        archiveBaseName.set("EconomyPlugin-${project.name}")
    }

    repositories {
        mavenCentral()
        maven("https://repo.extendedclip.com/releases") // PlaceholderAPI
        maven("https://repo.aikar.co/content/groups/aikar/") // Aikar
        maven("https://repo.papermc.io/repository/maven-public/") // paperweight, Velocity
        maven("https://repo.codemc.org/repository/nms/") // CraftBukkit + NMS
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://hub.spigotmc.org/nexus/content/groups/public/") // BStats
        maven("https://repo.piggypiglet.me/releases")
    }

    project.afterEvaluate {

        val javaExt = extensions.getByType<JavaPluginExtension>()

        val sourcesJar = tasks.register<Jar>("sourcesJar") {

            archiveClassifier.set("sources")
            from(javaExt.sourceSets["main"].allSource)

            if (project.name == "paper") { // Include the shared project in paper-sources.jar
                val sharedProject = project(":shared")
                val sharedJava = sharedProject.extensions.getByType<JavaPluginExtension>()
                from(sharedJava.sourceSets["main"].allSource)
            }

        }

        extensions.configure<PublishingExtension>("publishing") {

            publications {

                create<MavenPublication>("mavenModules") {
                    artifactId = "EconomyPlugin-${project.name}"
                    from(components["java"])
                    artifact(sourcesJar)
                }

            }

            repositories {

                val url = "https://repo.codemc.io/repository/goncalodelima/"

                val mavenUsername = System.getenv("goncalodelima_username") ?: return@repositories
                val mavenPassword = System.getenv("goncalodelima_password") ?: return@repositories

                maven(url) {
                    credentials {
                        username = mavenUsername
                        password = mavenPassword
                    }
                }

            }

        }

    }

}