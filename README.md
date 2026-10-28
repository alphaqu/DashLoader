<p align="center"><img src="./src/main/resources/dashloader/textures/icon.png" alt="DashLoader logo" width="128"></p>
<h1 align="center">DashLoader</h1>

<h1 align="center">
<a href="https://www.curseforge.com/minecraft/mc-mods/dashloader"><img alt="CurseForge" src="https://img.shields.io/badge/DashLoader-CurseForge?logo=curseforge&logoColor=%23F16436&label=CurseForge&color=%23F16436"></a>
<a href="https://modrinth.com/mod/dashloader"><img alt="Modrinth" src="https://img.shields.io/badge/DashLoader-Modrinth?logo=modrinth&logoColor=%2300AF5C&label=Modrinth&color=%2300AF5C"></a>
<a href="https://modrinth.com/mod/dashloader-unofficial-continuation"><img alt="Modrinth fork" src="https://img.shields.io/badge/DashLoader-Modrinth?logo=modrinth&logoColor=%2300AF5C&label=Modrinth%20(fork)&color=%2300AF5C"></a>
</h1>

> [!IMPORTANT]
> This is an unofficial continuation. The original project by [AlphaQ](https://github.com/alphaqu/DashLoader) stopped at Minecraft 1.21.4, so everything for 1.21.5 and newer lives here. Full credit for the original work goes to AlphaQ and all contributors, and this fork keeps the same LGPL-3.0-only licence.
>
> Looking for the original? It is archived at [alphaqu/DashLoader](https://github.com/alphaqu/DashLoader).

### Welcome to DashLoader!

Tired of watching the loading screen while every block model, texture, sprite and font glyph gets parsed from scratch, on every single launch?

DashLoader is a client-side cache for the resource reload. The first time you start the game it does its normal work and writes the result to disk. Every launch after that reads the cache back instead, so the world loads dramatically faster. No gameplay changes, no server-side component, nothing to configure. Install it, launch twice, and judge the difference yourself.

It invalidates itself the way you would expect: the cache key is derived from your mod list, the mod versions and your enabled resource packs, so adding a mod or swapping a pack produces a fresh cache on its own. Press `F3 + T` to force a rebuild while you are iterating on assets.

| Aspect | Detail |
|---|---|
| ![Java](https://img.shields.io/badge/java-21%20%2F%2025-8A2BE2) | 1.21.x needs Java 21, 26.x needs Java 25 |
| ![License](https://img.shields.io/badge/license-LGPL--3.0-blue) | LGPL-3.0-only, same as upstream |
| ![Client side](https://img.shields.io/badge/client%20side-yes-5CB85C) | No server install, single-player and servers alike |

## Supported versions

| Status | Minecraft | Java | Loaders |
|---|---|---|---|
| Stable | 1.21.4 - 1.21.11 | 21 | Fabric, Quilt |
| Beta | 26.1, 26.1.1, 26.1.2, 26.2, 26.3 | 25 | Fabric, Quilt |
| Beta | 26.2, 26.3, 1.21.11 | 21 / 25 | NeoForge |
| WIP | 1.16.5 | 8 | Forge, skeleton port, not feature equivalent |

Every Minecraft version lives on its own branch, named `fabric-<mcVersion>`, `neoforge-<mcVersion>` or `forge-<mcVersion>`. Check which branch you are on before opening an issue, because that decides what is even buildable.

## Things worth knowing

- **The first launch is the slow one.** That is when the cache gets written. Every launch after that is the fast one.
- **Uncacheable assets are skipped, not fatal.** A mod asset that cannot be represented is logged with a warning and then loaded the vanilla way, so one broken mod does not take the whole cache down with it.
- **GPU-side caches are gone on 1.21.5 and newer.** Mojang removed the hooks DashLoader used for stitched atlases and shader caches there. Model, sprite, font and translation caching still applies.
- **The Forge 1.16.5 port is a skeleton.** It shares no code with the other ports and does not include any of the fixes below. Use Fabric or NeoForge on 1.21.4 or newer.

## What the recent builds fix

The `5.1.0-beta.9.7` and `5.1.0-beta.9.8` builds addressed, among others:

- A native memory leak in the serialization path, which could surface as `OutOfMemoryError: Direct buffer memory` on large modpacks even when the Java heap looked empty.
- A cache write that could be interrupted by the game closing, leaving a half written directory that the next launch would try and fail to load. The cache is now written to a temp directory and moved into place.
- A progress bar that could stall below 100% because the counter was updated from several threads at once.
- A data race that could make a module silently fall back to vanilla instead of using the cache.
- Every quad of a block model being registered twice during cache creation.
- The cache toast sometimes disappearing instantly instead of showing its result.

## Reporting issues

Please include your Minecraft version, loader, the full mod list, and the relevant part of `latest.log`. If the cache is involved, `dashloader-cache` in your instance folder is worth attaching as well.

## Building from source

```bash
./gradlew build
# jars land in build/libs/dashloader-<version>+<mcVersion>.jar
```

Hyphen and Taski have to be resolvable, either from `mavenLocal` or from the vendored `libs` folder, since `notalpha.dev` is frequently unreachable.

## Licence

LGPL-3.0-only, unchanged from upstream. See `LICENCE`.