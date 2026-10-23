package com.github.mim1q.minecells.network;

import com.github.mim1q.minecells.MineCells;
import com.github.mim1q.minecells.cc.MineCellsLevelCC;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.wispforest.owo.network.OwoNetChannel;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.network.ServerPlayerEntity;

/** Synchronizes each player's portal progression from persistent server data. */
public final class PortalNetworkBridge {
  private PortalNetworkBridge() {
  }

  public static void registerServerPackets(OwoNetChannel channel) {
    channel.registerClientboundDeferred(PortalSyncS2CPacket.class);
  }

  public static void registerClientPackets(OwoNetChannel channel) {
    channel.registerClientbound(PortalSyncS2CPacket.class, (packet, access) -> {
      try {
        MineCellsLevelCC.PortalsCC.applyClientSync(StringNbtReader.parse(packet.data()));
      } catch (CommandSyntaxException e) {
        MineCells.LOGGER.error("Failed to read synced portal data", e);
      }
    });
  }

  public static void sync(ServerPlayerEntity player) {
    var data = MineCellsLevelCC.PortalsCC.writePlayerNbt(player);
    ServerPacketHandler.CHANNEL.serverHandle(player).send(new PortalSyncS2CPacket(data.asString()));
  }

  public record PortalSyncS2CPacket(String data) {
  }
}
