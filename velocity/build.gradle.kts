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
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-rc1"
}

tasks {

    named<ShadowJar>("shadowJar"){
        relocate("org.bstats", "pt.gongas.economy.lib.bstats")
        exclude("META-INF/versions/**")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    build {
        dependsOn(shadowJar)
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

group = "pt.gongas"
version = "1.2.9"
description = "Velocity module for Economy Plugin"

dependencies {
    api(project(":shared"))
    compileOnly(fileTree(mapOf("dir" to rootDir.resolve("libs"), "include" to listOf("*.jar"))))
    compileOnly("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
    implementation("org.bstats:bstats-velocity:3.1.0")
    annotationProcessor("com.velocitypowered:velocity-api:3.4.0-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}