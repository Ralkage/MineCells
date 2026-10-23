package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.util.LegacyUtil;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;

/** Loader-specific hook for the crowbar's instant wooden-door break. */
public final class CrowbarAttackHandler {
  private CrowbarAttackHandler() { }

  public static void register() {
    AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
      var stack = player.getStackInHand(hand);
      if (!stack.isOf(MineCellsItems.CROWBAR) || !world.getBlockState(pos).isIn(BlockTags.WOODEN_DOORS)) {
        return ActionResult.PASS;
      }
      if (world.getBlockState(pos.down()).isIn(BlockTags.WOODEN_DOORS)) {
        world.breakBlock(pos.down(), false, player);
      }
      world.breakBlock(pos, false, player);
      LegacyUtil.getOrCreateNbt(stack).putLong("lastDoorBreakTime", world.getTime());
      return ActionResult.SUCCESS;
    });
  }
}
