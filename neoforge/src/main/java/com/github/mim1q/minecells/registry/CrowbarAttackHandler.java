package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.util.LegacyUtil;
import net.minecraft.registry.tag.BlockTags;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** NeoForge's left-click event for the crowbar's instant wooden-door break. */
public final class CrowbarAttackHandler {
  private CrowbarAttackHandler() { }

  public static void register() {
    NeoForge.EVENT_BUS.addListener(CrowbarAttackHandler::onLeftClickBlock);
  }

  private static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
    if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;
    var world = event.getLevel();
    var player = event.getEntity();
    var pos = event.getPos();
    var stack = event.getItemStack();
    if (!stack.isOf(MineCellsItems.CROWBAR) || !world.getBlockState(pos).isIn(BlockTags.WOODEN_DOORS)) return;
    if (world.isClient()) return;
    if (world.getBlockState(pos.down()).isIn(BlockTags.WOODEN_DOORS)) {
      world.breakBlock(pos.down(), false, player);
    }
    world.breakBlock(pos, false, player);
    LegacyUtil.getOrCreateNbt(stack).putLong("lastDoorBreakTime", world.getTime());
    event.setCanceled(true);
  }
}
