package dev.mim1q.gimm1q.network.s2c;

import dev.mim1q.gimm1q.Gimm1q;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Sends server datapack calculator values to clients for tooltip calculations. */
public record ValueCalculatorSyncS2CPacket(String json) implements CustomPayload {
    public static final Id<ValueCalculatorSyncS2CPacket> ID = new Id<>(Gimm1q.id("value_calculator_sync"));
    public static final PacketCodec<RegistryByteBuf, ValueCalculatorSyncS2CPacket> CODEC = PacketCodec.of(
        (packet, buf) -> buf.writeString(packet.json(), 1_048_576),
        buf -> new ValueCalculatorSyncS2CPacket(buf.readString(1_048_576))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
