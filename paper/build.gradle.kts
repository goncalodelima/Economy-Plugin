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
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.gradleup.shadow") version "9.0.0-rc1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

tasks {

    named<ShadowJar>("shadowJar") {
        configurations = listOf(project.configurations.runtimeClasspath.get())
        relocate("org.bstats", "pt.gongas.economy.lib.bstats")
        relocate("co.aikar.commands", "pt.gongas.economy.lib.aikar.commands")
        relocate("co.aikar.locales", "pt.gongas.economy.lib.aikar.locales")
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
version = "1.2.0"
description = "Paper module for Economy Plugin"

paperweight {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    api(project(":shared"))
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly(fileTree(mapOf("dir" to rootDir.resolve("libs"), "include" to listOf("*.jar"))))
    compileOnly("net.kyori:adventure-text-minimessage:4.24.0")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

bukkit {
    name = "economy-plugin"
    version = "${project.version}"
    main = "pt.gongas.economy.platforms.paper.PaperEconomyPlugin"
    depend = listOf("database-api", "redis-plugin")
    author = "ReeachyZ_"
    website = "https://github.com/goncalodelima/Economy-Plugin"
    description = "Free Multi-economy plugin with multi-server support"
    apiVersion = "1.13"
}