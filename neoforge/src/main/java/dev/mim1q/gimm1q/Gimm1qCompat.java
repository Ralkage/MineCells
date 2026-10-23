package dev.mim1q.gimm1q;

import dev.mim1q.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.gimm1q.network.s2c.CameraShakeS2CPacket;
import dev.mim1q.gimm1q.network.s2c.ValueCalculatorSyncS2CPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Installs the common NeoForge bridge for the bundled Gimm1q APIs. */
public final class Gimm1qCompat {
    private static boolean initialized;

    private Gimm1qCompat() {}

    public static void init() {
        if (initialized) return;
        initialized = true;
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) ->
            event.addListener(ValueCalculatorResourceReloader.INSTANCE));
        NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) ->
            event.getRelevantPlayers().forEach(player ->
                PacketDistributor.sendToPlayer(player, ValueCalculatorResourceReloader.INSTANCE.createPacket())));
    }

    public static void init(IEventBus modBus) {
        init();
        modBus.addListener(Gimm1qCompat::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(CameraShakeS2CPacket.ID,
            CameraShakeS2CPacket.CODEC,
            (packet, context) -> Gimm1qClientCompat.applyShake(packet));
        event.registrar("1").playToClient(ValueCalculatorSyncS2CPacket.ID,
            ValueCalculatorSyncS2CPacket.CODEC,
            (packet, context) -> ValueCalculatorResourceReloader.INSTANCE.replaceWith(packet));
    }
}
