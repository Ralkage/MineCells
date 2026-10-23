package com.github.mim1q.minecells.network;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

/** Players currently receiving updates for an entity or a chunk. */
public final class TrackingPlayers {
  private TrackingPlayers() {
  }

  public static Collection<ServerPlayerEntity> tracking(Entity entity) {
    return PlayerLookup.tracking(entity);
  }

  public static Collection<ServerPlayerEntity> tracking(ServerWorld world, BlockPos pos) {
    return PlayerLookup.tracking(world, pos);
  }
}
