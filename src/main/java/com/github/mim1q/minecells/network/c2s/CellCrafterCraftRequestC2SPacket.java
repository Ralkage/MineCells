package com.github.mim1q.minecells.network.c2s;

import com.github.mim1q.minecells.MineCells;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record CellCrafterCraftRequestC2SPacket(
  Identifier recipeId,
  BlockPos pos
) {
}
