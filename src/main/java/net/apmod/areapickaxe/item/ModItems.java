package net.apmod.areapickaxe.item;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/**
 * Registers this mod's custom items.
 *
 * IMPORTANT: The area pickaxe is intentionally NOT added to any
 * {@link net.minecraft.item.ItemGroup} / creative tab, and no recipe exists
 * for it anywhere in this mod. The only way to obtain it is via the
 * "/areapickaxe give" command (see command/GivePickaxeCommand) or a vanilla
 * "/give <player> areapickaxe:area_pickaxe" command.
 */
public final class ModItems {

	private ModItems() {
	}

	/**
	 * Custom tool material for the area pickaxe.
	 * Reuses netherite's "incorrect blocks" tag so it can mine anything a
	 * netherite pickaxe can, with a faster mining speed and bonus damage.
	 */
	public static final ToolMaterial AREA_PICKAXE_MATERIAL = new ToolMaterial(
			BlockTags.INCORRECT_FOR_NETHERITE_TOOL, // blocks this tool CANNOT get proper drops from (none, top tier)
			2031,                                    // durability
			12.0f,                                   // mining speed
			4.0f,                                     // attack damage bonus
			15,                                        // enchantability
			ItemTags.NETHERITE_TOOL_MATERIALS         // repair material tag
	);

	public static final RegistryKey<Item> AREA_PICKAXE_KEY = RegistryKey.of(
			RegistryKeys.ITEM, Identifier.of("areapickaxe", "area_pickaxe"));

	// As of Minecraft 1.21.11 there is no dedicated PickaxeItem class - pickaxe
	// behavior (mining speed, effective-block tag, attack stats, the "Tool"
	// data component, etc.) is configured entirely through this Item.Settings
	// builder method instead of a subclass constructor.
	public static final AreaMiningPickaxeItem AREA_PICKAXE = new AreaMiningPickaxeItem(
			new Item.Settings()
					.registryKey(AREA_PICKAXE_KEY)
					.maxCount(1)
					.rarity(Rarity.EPIC)
					.pickaxe(AREA_PICKAXE_MATERIAL, 1.0f, -2.8f)
	);

	public static void register() {
		Registry.register(Registries.ITEM, AREA_PICKAXE_KEY, AREA_PICKAXE);

		// Deliberately NOT registered into any creative ItemGroup entry.
		// This keeps it out of the creative inventory / JEI-style browsers so
		// the command is the only discoverable way to get it. Uncomment the
		// block below only if you WANT it to show up in the creative Tools tab
		// (it would still have no crafting recipe either way).
		//
		// import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
		// import net.minecraft.item.ItemGroups;
		// ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(AREA_PICKAXE));
	}
}
