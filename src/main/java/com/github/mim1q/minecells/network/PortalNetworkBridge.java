package com.github.mim1q.minecells.network;

import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.server.network.ServerPlayerEntity;

/** Platform hook for portal-state synchronization. Fabric uses Cardinal Components. */
public final class PortalNetworkBridge {
  private PortalNetworkBridge() {
  }

  public static void registerServerPackets(OwoNetChannel channel) {
  }

  public static void registerClientPackets(OwoNetChannel channel) {
  }

  public static void sync(ServerPlayerEntity player) {
  }
}
