![lol](https://i.imgur.com/AVSOLcz.png)

# Alchemistry - Fabric Mod
_This is a Fabric port of the original Alchemistry mod by Dark_Arcana. It allows you to break down any item in the world into chemical components, which you can recombine into other items. Inspired by the MineChem mod, Alchemistry brings real life chemistry to Minecraft, with hundreds of chemical compounds to experiment with!_

# Minecraft 26.2 port

This checkout targets Minecraft **26.2**, Fabric Loader **0.19.5+**, Fabric API **0.160.0+26.2**, and **Java 25**. It requires the matching **ChemLib 1.0.2+mc26.2** port. The older 1.19.2 ChemLib release is not compatible.

The port includes the dissolver, combiner, compactor, atomizer, liquifier, fission and fusion reactors, energy/item/fluid automation, configurable machine costs, recipe locking and pausing, all 4,880 original recipes, advancements, textures and models. It also generates atomizer/liquifier conversions from ChemLib fluids. REI **26.2.821** is optional and displays all seven machine recipe types.

The handbook is now a built-in item because the old Patchouli dependency does not target 26.2. It preserves the original handbook text and crafting recipes and provides rotatable, layer-by-layer reactor diagrams and world placement overlays. It is granted on first join or crafted with a book and stone pressure plate. Use the guide on a placed reactor controller to project its structure; missing blocks appear blue and incorrect blocks appear red. Aim at a projected block to see valid materials. Sneak-use the guide on the controller, or choose Hide projection in the book, to dismiss it.

## Install

Place `Alchemistry-1.0.3+mc26.2.jar`, the matching ChemLib JAR and Fabric API in your Fabric 26.2 instance's `mods` folder. Energy API and Forge Config API Port are bundled in Alchemistry. To use REI, install its matching Fabric release and its required dependencies.

This is a local development port; the older releases on CurseForge and Modrinth are not this build. Existing 1.19.2 worlds have not been migration-tested.

## Build and test

Install JDK 25. Build the companion ChemLib port first in a sibling `ChemLib-Fabric` directory, then run:

```sh
./gradlew build
./gradlew runGameTest
./gradlew runClientGameTest
```

Alternatively, supply the matching ChemLib JAR explicitly:

```sh
./gradlew -Pchemlib_jar=/absolute/path/to/ChemLib-1.0.2+mc26.2.jar build runGameTest
```

Artifacts are written to `build/libs/`. The normal JAR is the mod; the `-sources.jar` is for development. Client tests launch Minecraft and require a graphical session. They capture machine, REI and handbook screens under `build/run/clientGameTest/screenshots/`.

Server tests check every original recipe, recipe network round trips, machine processing, save/reload, fluid capacity boundaries, bucket interactions, target slots, automation sides, and reactor structure/port behavior. Client tests check all item models, all machine menus, large energy/fluid values, controls, combiner recipe synchronization, every REI category, handbook pages and reactor world projections.

Until the ChemLib port is published to Maven, CI needs a direct ChemLib JAR download URL. Set the repository variable `CHEMLIB_26_2_JAR_URL` to enable push/PR builds, or supply the URL when manually running the build workflow. JitPack likewise requires that environment variable.

# Issues and Suggestions
To report issue, add a translation, or make a feature suggestion, please open a new issue in the `Issues` tab. This will require that you create a free GitHub account. **Please always include the version of the Alchemistry that you are using!**

# Screenshots
<img src="https://i.imgur.com/eEwQjtp.png" width="70%" height="70%" />

# Credits

* TechnoVision - Developer of Alchemistry-Fabric
* DarkArcana - Developer of Alchemistry
* Timbroglio - Model and Texture Artist

# License

Alchemistry is **All Rights Reserved** unless explicitly specified.
