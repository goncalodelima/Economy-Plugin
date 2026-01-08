/*
 *
 *  * This file is part of Economy-Plugin - https://github.com/goncalodelima/Economy-Plugin
 *  * Copyright (c) 2025 goncalodelima and contributors
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
    version = "1.0.5"
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

    project.afterEvaluate {
        extensions.configure<PublishingExtension>("publishing") {

            publications {
                create<MavenPublication>("mavenJava") {
                    artifactId = "EconomyPlugin-${project.name}"
                    from(components["java"])
                }
            }

            repositories {
                val mavenUrl: String? by project
                val mavenSnapshotUrl: String? by project

                println("mavenUrl: $mavenUrl")

                (if(version.toString().endsWith("SNAPSHOT")) mavenSnapshotUrl else mavenUrl)?.let { url ->

                    maven(url) {
                        val mavenUsername: String? by project
                        val mavenPassword: String? by project
                        println("mavenUsername: $mavenUsername")
                        if(mavenUsername != null && mavenPassword != null) {
                            credentials {
                                username = mavenUsername
                                password = mavenPassword
                            }
                        }
                    }
                }
            }

        }
    }

}

val platforms = setOf(
    project(":bukkit"),
    project(":velocity")
)

val specials = setOf(
    project(":shared")
)