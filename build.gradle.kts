plugins {
    id("java")
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

group = "com.twins"
version = "1.0.0"

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://jitpack.io")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://github.com/goncalodelima/ms-vip")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
    compileOnly(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("me.devnatan:inventory-framework-platform-bukkit:3.0.8")
    implementation("co.aikar:acf-bukkit:0.5.1-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

bukkit {
    name = "twins-core"
    version = "${project.version}"
    main = "com.twins.core.CorePlugin"
    depend = listOf("InventoryFramework", "packetevents", "SwornGuns", "ServersNPC")
    author = "ReeachyZ_"
    website = "https://minecraft-solutions.com"
    description = "Core Plugin"
    commands {
        register("sound"){
            aliases = listOf("som", "sons", "sounds")
        }
        register("vehicle"){
            aliases = listOf("vehicles", "veiculo", "veiculos")
        }
        register("speed"){
            aliases = listOf("velocidade")
        }
        register("edititem"){
            aliases = listOf("editaritem")
        }
        register("kit"){
            aliases = listOf("kits")
        }
        register("viewkit"){
            aliases = listOf("verkit")
        }
        register("warp"){
            aliases = listOf("editaritem")
        }
        register("gamemode"){
            aliases = listOf("gm")
        }
        register("luz"){
            aliases = listOf("lanterna", "light", "lantern")
        }
        register("spawn")
        register("setspawn")
        register("fly"){
            aliases = listOf("voar")
        }
        register("tp")
        register("tpa")
        register("tpaccept")
        register("tpdeny")
        register("vanish"){
            aliases = listOf("v")
        }
        register("invsee")
        register("pull"){
            aliases = listOf("puxar")
        }
        register("looting")
        register("city")
        register("research"){
            aliases = listOf("pesquisa")
        }
        register("gang"){
            aliases = listOf("clan", "clans", "gangs")
        }
        register("time"){
            aliases = listOf("playtime", "tempo", "tempojogado")
        }
        register("remover"){
            aliases = listOf("remove", "erase")
        }
        register("craft"){
            aliases = listOf("craftar")
        }
        register("clear"){
            aliases = listOf("limpar")
        }
        register("skull"){
            aliases = listOf("cabeca")
        }
        register("hat"){
            aliases = listOf("chapeu", "chapeus", "hats")
        }
        register("bed"){
            aliases = listOf("beds", "cama", "camas")
        }
        register("cupboards"){
            aliases = listOf("cupboard", "armario", "armarios", "tc", "toolcup", "toolcupboard", "toolcupboards", "tcs")
        }
        register("language"){
            aliases = listOf("lang", "idioma", "idiomas", "lingua", "linguas")
        }
        register("tools"){
            aliases = listOf("ferramenta", "ferramentas")
        }
        register("beginner"){
            aliases = listOf("beginners", "iniciante", "iniciantes", "novato", "novatos")
        }
        register("buff"){
            aliases = listOf("buffs", "monster", "monsters", "mob", "mobs", "bonus", "booster", "boosters")
        }
        register("respawn"){
            aliases = listOf("respawnar", "matar", "suicidar", "suicidio")
        }
        register("recipe"){
            aliases = listOf("recipes", "receitas", "receita")
        }
    }

}