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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://repo.aikar.co/content/groups/aikar/") // Aikar
        maven("https://repo.purpurmc.org/snapshots") // Purpur
        maven("https://repo.papermc.io/repository/maven-public/") // paperweight, Velocity
        maven("https://repo.codemc.org/repository/nms/") // CraftBukkit + NMS
        maven("https://repo.codemc.org/repository/maven-public/") // Item-NBT-API
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://repo.codemc.io/repository/maven-releases/") // PacketEventsAPI
        maven("https://repo.codemc.io/repository/maven-snapshots/") // PacketEventsAPI
        maven("https://hub.spigotmc.org/nexus/content/groups/public/") // BStats
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "EconomyPlugin"

include(":shared")
include(":bukkit")
include(":velocity")