package net.apmod.areapickaxe.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.apmod.areapickaxe.item.ModItems;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * Registers "/areapickaxe give [<targets>]".
 *
 * This is the (recommended) intended way to obtain the pickaxe. It is
 * restricted to server operators by default, since this item is not
 * supposed to be player-obtainable through normal survival gameplay. Note
 * that vanilla "/give <player> areapickaxe:area_pickaxe" also works once the
 * item is registered - there is simply no recipe, loot table, villager
 * trade, or creative-tab entry for it anywhere in this mod, so a command
 * really is the only route to it.
 *
 * NOTE: Minecraft 1.21.11 replaced the old simple integer permission levels
 * (ServerCommandSource#hasPermissionLevel(int)) with a much larger,
 * registry-based permission-node system (net.minecraft.command.permission).
 * Rather than depend on that new API's exact shape - which may still move
 * around before 26.1 - this command checks the server's operator list
 * directly via PlayerManager#isOperator(GameProfile), which has been a
 * stable, unchanged API since very early Minecraft versions.
 */
public final class GivePickaxeCommand {

	private GivePickaxeCommand() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
								 net.minecraft.command.CommandRegistryAccess registryAccess,
								 net.minecraft.server.command.CommandManager.RegistrationEnvironment environment) {
		dispatcher.register(literal("areapickaxe")
				.requires(GivePickaxeCommand::isOpOrConsole)
				.then(literal("give")
						// /areapickaxe give -> gives to the command's own executor, if a player
						.executes(GivePickaxeCommand::giveToSelf)
						// /areapickaxe give <targets> -> gives to one or more specified players
						.then(argument("targets", EntityArgumentType.players())
								.executes(GivePickaxeCommand::giveToTargets))));
	}

	/**
	 * True for the console/command blocks (no player attached), or for a
	 * player who is a server operator.
	 *
	 * NOTE: as of Minecraft 1.21.9+, PlayerManager#isOperator(...) takes a
	 * PlayerConfigEntry rather than a raw GameProfile (part of the same
	 * player-list rework that replaced the old ban/whitelist/op-list
	 * handling). PlayerEntity#getPlayerConfigEntry() gives us that directly.
	 */
	private static boolean isOpOrConsole(ServerCommandSource source) {
		ServerPlayerEntity player = source.getPlayer();
		if (player == null) {
			return true;
		}
		return source.getServer().getPlayerManager().isOperator(player.getPlayerConfigEntry());
	}

	private static int giveToSelf(CommandContext<ServerCommandSource> context) {
		ServerPlayerEntity player = context.getSource().getPlayer();
		if (player == null) {
			context.getSource().sendError(Text.literal("This command must be run by, or target, a player."));
			return 0;
		}
		givePickaxe(player);
		context.getSource().sendFeedback(() ->
				Text.literal("Gave the area pickaxe to " + player.getName().getString()), true);
		return 1;
	}

	private static int giveToTargets(CommandContext<ServerCommandSource> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		var targets = EntityArgumentType.getPlayers(context, "targets");
		for (ServerPlayerEntity target : targets) {
			givePickaxe(target);
		}
		context.getSource().sendFeedback(() ->
				Text.literal("Gave the area pickaxe to " + targets.size() + " player(s)"), true);
		return targets.size();
	}

	private static void givePickaxe(ServerPlayerEntity player) {
		ItemStack stack = new ItemStack(ModItems.AREA_PICKAXE);
		stack.set(net.minecraft.component.DataComponentTypes.CUSTOM_NAME, Text.literal("Area Pickaxe"));

		if (!player.getInventory().insertStack(stack)) {
			player.dropItem(stack, false);
		}
	}
}
