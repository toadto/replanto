package to.toad.replant;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class RePlant implements ModInitializer {
	public static final String MOD_ID = "replant";
	Queue<BlockPlaceContext> queue = new ArrayDeque<>();
	ServerPlayerGameMode se = null;
	ServerLevel ServerWorld = null;

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.

	@Override
	public void onInitialize() {

		PlayerBlockBreakEvents.AFTER.register(this::afterBlockBreak);

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			// process everything that was queued LAST tick
			while (!queue.isEmpty()) {
				BlockPlaceContext context = queue.poll();

				ItemStack stack = context.getItemInHand();

				if (stack.getItem() instanceof BlockItem blockItem) {
					blockItem.place(context);
				}

			}

		});


	}



	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	private void afterBlockBreak(Level world, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
		ServerPlayer sp = (ServerPlayer) player;
		se = sp.gameMode;
		ServerWorld = sp.level();
		ItemStack stack = player.getMainHandItem();

		InteractionHand hand = InteractionHand.MAIN_HAND;

		if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
			stack = player.getOffhandItem();
			hand = InteractionHand.OFF_HAND;
		}

		if (stack.getItem() instanceof BlockItem blockItem
				&& (blockItem.getBlock() instanceof CropBlock || blockItem.getBlock() instanceof NetherWartBlock || blockItem.getBlock() instanceof CocoaBlock)) {

			queue.add(new BlockPlaceContext(
					player,
					hand,
					stack,
					new BlockHitResult(
							Vec3.atCenterOf(pos),
							Direction.UP,
							pos,
							false
					)
			));
		}
	}
}
