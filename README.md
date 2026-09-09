# Area Pickaxe

A Fabric mod (Minecraft 1.21.11) that adds a special pickaxe which mines a **3x3 area** of blocks
at once, instead of just one block.

# If you just want the .jar just make sure it is installed on the client and server
you can get the mod at modrinth the curseforge mod is coming soon

## Getting the pickaxe

The pickaxe **cannot be crafted**, has no recipe, doesn't drop from anything, and is not in the
creative inventory. The only ways to get it:

```
/areapickaxe give              give it to yourself (must be run by a player)
/areapickaxe give <player>     give it to one or more other players
```

This requires permission level 2 (ops / command blocks), same as most admin commands. You can
still also use the plain vanilla command if you prefer:

```
/give <player> areapickaxe:area_pickaxe
```

Either way, since there's no recipe/loot table/trade for it anywhere, a command is the only route
to it in survival.

## How it works

- Break a block with the pickaxe in your main hand and it takes the 8 blocks around it (in the
  plane you're facing) with it, for a 3x3 patch in a single swing.
- **Sneak (hold shift) while mining** to mine just the single block, like a normal pickaxe.
- The extra blocks are broken through the normal survival block-break path, so drops, durability,
  tool-effectiveness checks, and stats all behave like a real player mined them.
- It's given as a top-tier (netherite-equivalent, slightly faster) pickaxe with high durability
  by default — that's just this mod's default flavor, easy to change (see below).

## Building

Requires JDK 21.

```
./gradlew build
```

The built mod jar will be in `build/libs/`. Drop it into your server or client's `mods/` folder
along with a matching [Fabric API](https://modrinth.com/mod/fabric-api) release for
1.21.11, on [Fabric Loader](https://fabricmc.net/use/) 0.16.0+.

## Notes / things you might want to tweak

- **Minecraft/mappings/loader versions**: `gradle.properties` pins specific Yarn mappings /
  Fabric API / loader build numbers current as of when this was generated. Minecraft updates
  fast — if the build fails on fetching mappings or Fabric API, check
  https://fabricmc.net/develop and https://modrinth.com/mod/fabric-api for current numbers and
  update `gradle.properties`.
- **No more `PickaxeItem` class**: as of 1.21.11, Mojang folded pickaxes and swords into the base
  `Item` class (configured via `Item.Settings#pickaxe(...)`) — only `AxeItem`, `HoeItem`, and
  `ShovelItem` still have their own classes, since they have extra right-click behavior. This mod
  already accounts for that (`AreaMiningPickaxeItem extends Item`), but if you're porting to a
  different Minecraft version, older versions do still have a `PickaxeItem` class you may want to
  extend instead.
- **No more `hasPermissionLevel(int)` / simple op levels**: 1.21.11 introduced a much larger
  registry-based permission-node system. Rather than depend on its exact (still-moving) shape,
  `GivePickaxeCommand` checks `PlayerManager#isOperator(GameProfile)` directly, which has been
  stable since very early Minecraft versions and does the same job for this use case.
- **Unbreakable component skipped**: earlier drafts of this mod tried to mark the pickaxe
  unbreakable via `DataComponentTypes.UNBREAKABLE` / `UnbreakableComponent`, but that record's
  exact shape has moved around several times and didn't resolve cleanly against 1.21.11's
  mappings. The pickaxe just uses its (very high, 2031) normal durability instead. If you want it
  truly unbreakable, check `net.minecraft.component.type.UnbreakableComponent` in your exact
  build's javadoc and add it back into `GivePickaxeCommand.givePickaxe()`.
- **Mining plane**: The mod picks the 3x3 plane from the direction the player is looking, not the
  exact face of the block clicked. This is simple and works well for normal head-on mining, but
  isn't a perfect raycast against the actual clicked face.
- **Tool stats**: edit `ModItems.AREA_PICKAXE_MATERIAL` in `ModItems.java` to change mining
  speed, durability, attack damage, or enchantability.
- **Texture**: `assets/areapickaxe/textures/item/area_pickaxe.png` is a simple placeholder
  16x16 icon — swap it out for your own art.

## Project layout

```
src/main/java/net/apmod/areapickaxe/
  AreaPickaxeMod.java              mod entrypoint + the 3x3 mining logic
  item/ModItems.java                item + tool material registration
  item/AreaMiningPickaxeItem.java   the pickaxe item class
  command/GivePickaxeCommand.java   "/areapickaxe give" command

src/main/resources/
  fabric.mod.json
  assets/areapickaxe/...            lang, item model/definition, texture
```

