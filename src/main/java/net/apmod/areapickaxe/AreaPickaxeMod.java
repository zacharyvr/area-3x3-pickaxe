package net.apmod.areapickaxe;

import net.apmod.areapickaxe.command.GivePickaxeCommand;
import net.apmod.areapickaxe.item.AreaMiningPickaxeItem;
import net.apmod.areapickaxe.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AreaPickaxeMod implements ModInitializer {

	public static final String MOD_ID = "areapickaxe";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Guards against the extra blocks we break re-triggering this same
	 * listener and chain-reacting outward (which would turn a 3x3 patch
	 * into an unbounded vein-miner). Only the original, player-initiated
	 * break is allowed to trigger the area-mining logic.
	 */
	private static boolean miningInProgress = false;

	@Override
	public void onInitialize() {
		ModItems.register();

		CommandRegistrationCallback.EVENT.register(GivePickaxeCommand::register);

		PlayerBlockBreakEvents.AFTER.register(AreaPickaxeMod::onBlockBroken);

		LOGGER.info("Area Pickaxe mod initialized");
	}

	private static void onBlockBroken(net.minecraft.world.World world, net.minecraft.entity.player.PlayerEntity player,
									   BlockPos pos, BlockState state, net.minecraft.block.entity.BlockEntity blockEntity) {
		if (world.isClient()) {
			return;
		}
		if (miningInProgress) {
			return; // this break was caused by us breaking an adjacent block - don't recurse
		}
		if (!(player instanceof ServerPlayerEntity serverPlayer)) {
			return;
		}

		ItemStack heldStack = player.getMainHandStack();
		if (!(heldStack.getItem() instanceof AreaMiningPickaxeItem)) {
			return;
		}

		// Sneaking mines just the single block, like a normal pickaxe.
		if (player.isSneaking()) {
			return;
		}

		// IMPORTANT: we are currently executing *inside* the server's
		// handling of the original block break (this event fires from
		// within ServerPlayerInteractionManager#tryBreakBlock). Calling
		// tryBreakBlock() again right here, synchronously, re-enters
		// that same in-progress call and corrupts its per-player mining
		// state - which is what was causing the original block to get
		// rolled back / "reappear" on the client. Deferring the extra
		// breaks with server.execute() runs them right after the
		// current call stack fully unwinds instead of nested inside it.
		((net.minecraft.server.world.ServerWorld) world).getServer().execute(() -> mineSurroundingArea(world, serverPlayer, pos));
	}

	/**
	 * Breaks the 8 blocks surrounding {@code pos} that lie in the same plane
	 * the player is facing, i.e. turns a single mined block into a 3x3
	 * patch. The plane is chosen from the player's look direction rather
	 * than the exact face clicked, which keeps this simple and works well
	 * for normal head-on mining.
	 */
	private static void mineSurroundingArea(net.minecraft.world.World world, ServerPlayerEntity player, BlockPos centerPos) {
		Vec3d look = player.getRotationVec(1.0f);
		Direction facing = Direction.getFacing(look.x, look.y, look.z);
		Direction.Axis axis = facing.getAxis();

		miningInProgress = true;
		try {
			for (int a = -1; a <= 1; a++) {
				for (int b = -1; b <= 1; b++) {
					if (a == 0 && b == 0) {
						continue; // center block was already broken by the vanilla break that triggered this
					}

					BlockPos targetPos = switch (axis) {
						case X -> centerPos.add(0, a, b);
						case Y -> centerPos.add(a, 0, b);
						case Z -> centerPos.add(a, b, 0);
					};

					try {
						breakIfPossible(world, player, targetPos);
					} catch (Exception e) {
						// One position failing (e.g. it's the block the player's
						// client just started legitimately mining into on its own,
						// or a chunk edge case) must never abort the rest of the
						// 3x3 - otherwise you get a partial patch (e.g. a 3x1 line)
						// instead of the full square.
						LOGGER.warn("Area pickaxe failed to break {}, skipping it", targetPos, e);
					}
				}
			}
		} finally {
			miningInProgress = false;
		}
	}

	private static void breakIfPossible(net.minecraft.world.World world, ServerPlayerEntity player, BlockPos pos) {
		if (!world.isChunkLoaded(pos)) {
			return;
		}
		BlockState state = world.getBlockState(pos);
		if (state.isAir()) {
			return;
		}
		if (state.getHardness(world, pos) < 0) {
			return; // unbreakable, e.g. bedrock
		}

		// tryBreakBlock runs the normal survival break: correct tool/drop
		// checks, durability, stats, advancement triggers, etc.
		player.interactionManager.tryBreakBlock(pos);
	}
}
