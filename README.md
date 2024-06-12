<br />
<div align="center">
  <a href="https://discord.com/users/216487432667791360">
    <img src=".github/assets/EnergeticStorageReborn.png" alt="Logo" width="200" height="200">
</a>
</div>

<h3 align="center">Energetic Storage Reborn</h3>

Energetic Storage Reborn is a plugin that is heavily inspired by the forge mods
named [Applied Energistics 2](https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2)
and [Refined Storage](https://www.curseforge.com/minecraft/mc-mods/refined-storage).
If you don't know what Applied Energistics 2 is, it's a mod that allows the player to store large numbers of items,
sometimes hundreds of thousands, in just a few blocks.

Energetic Storage Reborn is a complete recode for Minecraft 1.20.6 of the
original [Energetic Storage](https://github.com/SeanOMik/EnergeticStorage).
It is significantly more modern,
designed exclusively for PaperMC, Java 21, Kotlin, and Minecraft 1.20.6+,
utilizing the latest features the game has to offer.
It is also designed to be more efficient and more user-friendly than the original while adding new features
and improvements.

If you run into any issues,
[create a GitHub issue!](https://github.com/CoasterFreakDE/EnergeticStorage-Reborn/issues/new)

---

## Features:

* [ ] Networking
    * [x] Cables (Data Transmissions)
    * [x] Network Cores
    * [x] DiskDrives
    * [x] Disks
    * [x] Terminals
        * [x] Terminal Sorting
        * [ ] Terminal Search
    * [ ] Importer
        * [ ] Hopper Input
    * [ ] Exporter
    * [x] Configurable Network Length
* [ ] Player/Region Whitelist
    * [ ] Lands Integration
* [ ] Custom Recipes
* [x] Support for 1.20.6
* [x] MySQL, MariaDB, SQLite, PostgreSQL, and H2 support

## Items And Blocks:

* 1k, 4k, 16k, and 64k, drives
    * The drives are needed to store items. 1k can store 1024 items, 4k can store 4096 and so on (1024 * how many
      thousand).
    * All drives have a type limit set at 64, 128, 256 or 512. This means that only this amount of different item types
      can be added to a single drive.
        * Can be changed in config.
* DiskDrive
* Terminal
* NetworkCore
* Importer
* Exporter
* *More info coming soon*

## Permissions

* `energeticstorage.give`: Gives permission to give an Energetic Storage item to themselves. Default: `op`
* `energeticstorage.give.others`: Gives permission to give an Energetic Storage item to others. Default: `op`
* `energeticstorage.debug`: Gives permission to toggle debug mode. Default: `op`

## Commands

* `/esdebug`: Toggles debug mode.
* `/esgive <item> [player]` : Gives an Energetic Storage item to the player. If no player is specified, it gives it to the
  command sender.

## Config:

Config.yml:

```yaml
drives:
  # Sets the drives max type limit.
  SMALL:
    diskName: "1k Disk"
    items: 1024
    types: 64
  MEDIUM:
    diskName: "4k Disk"
    items: 4096
    types: 128
  LARGE:
    diskName: "16k Disk"
    items: 16384
    types: 256
  XLARGE:
    diskName: "64k Disk"
    items: 65536
    types: 512

networks:
  maxLength: 128 # The maximum length of the network (connected blocks)

storage:
  # The storage type to use. Options: "mysql", "mariadb", "sqlite", "postgresql", "h2"
  # I highly recommend using MySQL/MariaDB or PostgreSQL for production environments.
  type: "sqlite"
  database: "energeticstorage.db"

  # MySQL/MariaDB/PostgreSQL configuration
  host: "localhost"
  port: 3306
  username: "root"
  password: "password"
```

---

<p align="center">
<br />
<br />
<a href="https://liamxsage.com">Website</a>
·
<a href="https://discord.com/users/216487432667791360"><strong>Contact</strong></a>
</p>

