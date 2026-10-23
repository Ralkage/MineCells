package com.github.mim1q.minecells.network;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public final class SoundPacketBridge {
  private SoundPacketBridge() { }

  public static void send(ServerPlayerEntity player, PlaySoundS2CPacket packet) {
    player.networkHandler.send(packet);
  }
}
