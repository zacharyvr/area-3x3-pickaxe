# Area Pickaxe

A simple Fabric mod that adds a special pickaxe capable of mining a **3x3 area** in a single swing.

## Features

- **3x3 mining** — break one block and the 8 blocks around it (in the plane you're facing) come with it.
- **Sneak to mine normally** — hold shift while mining to break just a single block, like a vanilla pickaxe.
- **Command-only, not craftable** — there is no recipe, loot table, or trade for this item anywhere. The only way to get it is:
  ```
  /areapickaxe give
  /areapickaxe give <player>
  ```
  (requires operator permissions)
- **Real survival behavior** — every extra block broken goes through the normal survival break path, so tool-effectiveness checks, drops, and stats all behave exactly like a player mined them by hand.
- **Top-tier stats** — netherite-equivalent effective-block range with slightly faster mining speed and bonus attack damage.

## Requirements

- Fabric Loader
- Fabric API

## Notes

- This mod needs to be installed on **both the client and the server** (or single-player world) — it's not server-side only.
- Since the pickaxe isn't obtainable through survival play, it's intended for admins, adventure maps, or creative-style servers that want a controlled way to hand out fast mining.

## Source

Open source — feel free to fork, reskin the texture, or tweak the tool stats / area size to fit your server.
