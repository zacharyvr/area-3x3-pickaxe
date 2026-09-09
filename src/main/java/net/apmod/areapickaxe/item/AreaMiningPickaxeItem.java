package net.apmod.areapickaxe.item;

import net.minecraft.item.Item;

/**
 * A pickaxe that, when it breaks a block, also breaks the 8 blocks around it
 * in the plane the player is facing (i.e. a 3x3 area). All of the actual
 * area-mining logic lives in AreaPickaxeMod's PlayerBlockBreakEvents.AFTER
 * listener - this class just needs to exist as a distinct, recognizable
 * item type so that listener (and the give command) can check
 * "is this item the area pickaxe?".
 *
 * NOTE: as of Minecraft 1.21.11 there is no more dedicated PickaxeItem class
 * (pickaxes and swords were folded into the base Item class, configured
 * through Item.Settings#pickaxe(...) - see ModItems). Only AxeItem, HoeItem
 * and ShovelItem still have their own classes, because they have extra
 * right-click behavior pickaxes don't need.
 */
public class AreaMiningPickaxeItem extends Item {

	public AreaMiningPickaxeItem(Item.Settings settings) {
		super(settings);
	}
}
