package to.toad.replant.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayDeque;
import java.util.Queue;

public class RePlantClient implements ClientModInitializer {
	Queue<BlockPlaceContext> queue = new ArrayDeque<>();
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerBlockBreakEvents.AFTER.register(this::afterBlockBreak);

		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			while (!queue.isEmpty()) {
				BlockPlaceContext context = queue.poll();

				ItemStack stack = context.getItemInHand();

				if (stack.getItem() instanceof BlockItem) {
                    assert mc.gameMode != null;
                    assert context.getPlayer() != null;
                    assert mc.player != null;
                    mc.gameMode.useItemOn(
							mc.player,
							context.getHand(),
							new BlockHitResult(
									Vec3.atCenterOf(context.getClickedPos()),
									Direction.UP,
									context.getClickedPos(),
									false
							)
					);
				}
			}
		});


	}

	private void afterBlockBreak(ClientLevel world, LocalPlayer player, BlockPos pos, BlockState blockHitResult) {
		InteractionHand hand = InteractionHand.MAIN_HAND;
		ItemStack stack = player.getItemInHand(hand);

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