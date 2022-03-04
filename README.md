# OlympaCore
## Olympa

Olympa was a Minecraft server. The full development took 2 years. The server closed in October 2021 and this is one of the archives of these many plugins. This repo is licensed under the MIT License.
More info about Olympa [here](https://github.com/OlympaMC/.github/blob/main/profile/README.md).

  
## Info
OlympaCore is the OlympaAPI application with server management. It is used by Spigot and Bungeecord. It contains OlympaAPI in its files, Gradle takes care of it during the compilation.


OlympaCore contains, among others :
 - AntiBot
 - First connection Queue and between serveurs
 - Bungee Ban/Mute
 - Maintenance mode
 - MOTD multi functions
 - Spigot servers monitors piloted by Bungee
 - Robust AntiVPN
 - Redis Cache and Channels
 - Custom API for MariaDB
 - Report system
 - Scoreboard very optimized
 - Basic AntiWorldownloader
 - Version handle with ProtocolSupport or/and ViaVersion or native version

The OlympaCore JAR is used for production usage. You should disable many feature to launch it without all the Olympa Infra.

## Dependencies

### Mandatory
- [Gradle](https://github.com/gradle/gradle) (Compilator)
- [Java JDK 16](https://github.com/openjdk/jdk16)
- [OlympaAPI](https://github.com/OlympaMC/olympa-api) Add repo at .\./olympa-api
- [Spigot](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse) 1.16
- [PaperSpigot](https://github.com/PaperMC/Paper) 1.16
- [Waterfall](https://github.com/PaperMC/Waterfall) 1.17
- [Jedis](https://github.com/redis/jedis) (Redis client for Java)
- [MariaDB Connector](https://github.com/mariadb-corporation/mariadb-connector-j) (Interact with DBMS)

### Optional Plugins Spigot

- [ProtocolSupport](https://github.com/ProtocolSupport/ProtocolSupport) (Adds players from previous versions) 
- [ViaVersion](https://github.com/ViaVersion/ViaVersion) (Adds players from the following versions)
