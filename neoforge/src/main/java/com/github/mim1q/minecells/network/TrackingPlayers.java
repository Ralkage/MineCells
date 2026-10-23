package com.github.mim1q.minecells.network;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

/** Players near the tracked entity or chunk on a NeoForge server. */
public final class TrackingPlayers {
  private TrackingPlayers() {
  }

  public static List<ServerPlayerEntity> tracking(Entity entity) {
    if (!(entity.getWorld() instanceof ServerWorld world)) {
      throw new IllegalArgumentException("Tracking players requires a server-side entity");
    }
    double distance = entity.getType().getMaxTrackDistance();
    double distanceSquared = distance * distance;
    return world.getPlayers(player -> player.squaredDistanceTo(entity) <= distanceSquared);
  }

  public static List<ServerPlayerEntity> tracking(ServerWorld world, BlockPos pos) {
    double distance = world.getServer().getPlayerManager().getViewDistance() * 16.0;
    double distanceSquared = distance * distance;
    return world.getPlayers(player -> player.squaredDistanceTo(
      pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5
    ) <= distanceSquared);
  }
}
