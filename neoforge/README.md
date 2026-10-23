# Mine Cells for NeoForge 1.21.1

This is a standalone Architectury Loom build that uses the upstream 1.21.1
Java sources and game assets, with NeoForge-specific replacements in
`src/main/java`. A replacement at the same relative path takes precedence over
the upstream Fabric class. Fabric data generators are omitted; EMI and REI use
native NeoForge plugin implementations.

The 876 generated client assets and 700 generated gameplay data resources
absent from the upstream source tree were recovered into
`src/main/resources` from the matching [Mine Cells 2.0.3 Fabric release](https://cdn.modrinth.com/data/9Qdvz1OV/versions/fFPxpM1K/minecells-2.0.3.jar)
(SHA-256: `125c20be9a97091d1c6a7f4c3e34bdaeacf6e3ac7ea7845e40eff05c6a76e990`).
The upstream Fabric datagen task fails in this checkout during server startup
because `RunicVinePlantBlock` loads a client-only particle class.

Use a Java 21 JDK. From the repository root, build with:

```sh
JAVA_HOME=/path/to/jdk-21 bash ./gradlew -p neoforge build
```

Alternatively, from this directory, run
`JAVA_HOME=/path/to/jdk-21 bash ../gradlew -p . build`.
The distributable jar is `neoforge/build/libs/minecells-neoforge-2.0.3.jar`.

Install it with Minecraft 1.21.1, NeoForge 21.1.262 or a newer compatible
21.1.x release, and the NeoForge build of oωo 0.12.15.6-beta.1+1.21.
The jar includes the required Gimm1q subset and Crunch library.
EMI and REI are optional recipe viewer integrations. Patchouli is optional for
the in-game guidebook.

For a development server, run `bash ./gradlew -p neoforge runServer` from the
repository root with the same Java 21 environment.

Overworld portal ruins generate in forests, jungles, taigas, and plains. With
commands enabled, `/locate structure minecells:overworld_portal` finds the nearest ruin.
Its chest contains a Prisoners' Quarters Doorway item; place that item on a
clear 3×3 solid wall in the Overworld to build an active portal. The doorway
can also be crafted or taken from the Mine Cells creative tab. Active portals
do not generate in the terrain by themselves. Updated structure placement only
affects newly generated chunks in existing worlds.

## Clock Tower feature branch

The Clock Tower follows the Black Bridge. Defeat the Concierge, pass through
the new doorway in the Black Bridge arena, cross the tower's combat rooms and
treasure detour, then fight the Time Keeper. Her blade, dash, and clock pulse
have visible windups; the second phase is faster. Defeating her opens the
Overworld exit. A doorway at the tower entrance returns to the Black Bridge.

Previously generated Black Bridge arenas do not gain the new doorway. In an
existing save, defeating the Concierge unlocks a Clock Tower Doorway recipe
in the Cell Crafter. Place that doorway on a clear 3×3 solid wall in the
Overworld to enter the route. A new world or a fresh Black Bridge run area
has the built-in arena doorway.

To test in a development client, run
`JAVA_HOME=/path/to/jdk-21 bash ./gradlew -p neoforge runClient` from the
repository root.
