package dev.mim1q.gimm1q;

import dev.mim1q.gimm1q.client.highlight.crosshair.CrosshairTipDrawerCallback;
import dev.mim1q.gimm1q.client.highlight.crosshair.CrosshairTipDrawerImpl;
import dev.mim1q.gimm1q.client.highlight.HighlightDrawerCallback;
import dev.mim1q.gimm1q.client.item.handheld.HandheldItemModelRegistryImpl;
import dev.mim1q.gimm1q.client.tooltip.TooltipResolverRegistry;
import dev.mim1q.gimm1q.network.s2c.CameraShakeS2CPacket;
import dev.mim1q.gimm1q.screenshake.ScreenShakeModifiers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.ArrayList;
import java.util.List;

/** Installs client callbacks supplied by the bundled Gimm1q API. */
public final class Gimm1qClientCompat {
    private static boolean initialized;
    private static int shakeRemaining;
    private static int shakeDuration;
    private static float shakeIntensity;
    private static float shakeYaw;
    private static float shakePitch;
    private static final java.util.Random SHAKE_RANDOM = new java.util.Random();

    private Gimm1qClientCompat() {}

    public static void register(IEventBus modBus) {
        // Additional models are registered by the host mod after it has
        // populated HandheldItemModelRegistry for this resource reload.
    }

    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        HandheldItemModelRegistryImpl.MODELS.values().forEach(ids -> {
            event.register(ModelIdentifier.standalone(ids.getLeft()));
            event.register(ModelIdentifier.standalone(ids.getRight()));
        });
    }

    public static void init() {
        if (initialized) return;
        initialized = true;
        NeoForge.EVENT_BUS.addListener(Gimm1qClientCompat::onTooltip);
        NeoForge.EVENT_BUS.addListener(Gimm1qClientCompat::onRenderGui);
        NeoForge.EVENT_BUS.addListener(Gimm1qClientCompat::onClientTick);
        NeoForge.EVENT_BUS.addListener(Gimm1qClientCompat::onCameraAngles);
        NeoForge.EVENT_BUS.addListener(Gimm1qClientCompat::onRenderLevel);
    }

    public static void applyShake(CameraShakeS2CPacket packet) {
        var intensity = packet.intensity();
        if (!packet.modifierName().isBlank()) {
            intensity *= ScreenShakeModifiers.getModifier(packet.modifierName());
        }
        if (intensity >= shakeIntensity || shakeRemaining <= 0) {
            shakeIntensity = intensity;
            shakeDuration = Math.max(1, packet.duration());
            shakeRemaining = shakeDuration;
        }
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        if (shakeRemaining <= 0) {
            shakeYaw = shakePitch = shakeIntensity = 0;
            return;
        }
        var attenuation = (float) shakeRemaining / shakeDuration;
        shakeYaw = (SHAKE_RANDOM.nextFloat() - 0.5f) * 2 * shakeIntensity * attenuation;
        shakePitch = (SHAKE_RANDOM.nextFloat() - 0.5f) * shakeIntensity * attenuation;
        shakeRemaining--;
    }

    private static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        event.setYaw(event.getYaw() + shakeYaw);
        event.setPitch(event.getPitch() + shakePitch);
    }

    private static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        var client = MinecraftClient.getInstance();
        if (client.player == null) return;
        var matrices = event.getPoseStack();
        var camera = event.getCamera().getPos();
        var consumers = client.getBufferBuilders().getEntityVertexConsumers();
        var buffer = consumers.getBuffer(RenderLayer.getLines());
        HighlightDrawerCallback.EVENT.invoker().drawHighlights((box, color, outline) -> {
            var argb = (outline >>> 24) != 0 ? outline : color;
            if ((argb >>> 24) == 0) return;
            matrices.push();
            matrices.translate(-camera.x, -camera.y, -camera.z);
            drawBoxLines(matrices.peek(), buffer, box, argb);
            matrices.pop();
        }, new HighlightDrawerCallback.HighlightDrawerContext(client.player));
        consumers.draw(RenderLayer.getLines());
    }

    private static void drawBoxLines(net.minecraft.client.util.math.MatrixStack.Entry matrix,
                                     VertexConsumer buffer, net.minecraft.util.math.Box box, int argb) {
        var x0 = (float) box.minX;
        var y0 = (float) box.minY;
        var z0 = (float) box.minZ;
        var x1 = (float) box.maxX;
        var y1 = (float) box.maxY;
        var z1 = (float) box.maxZ;
        float[][] points = {
            {x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}, {x0, y0, z1},
            {x0, y1, z0}, {x1, y1, z0}, {x1, y1, z1}, {x0, y1, z1}
        };
        int[][] edges = {
            {0, 1}, {1, 2}, {2, 3}, {3, 0},
            {4, 5}, {5, 6}, {6, 7}, {7, 4},
            {0, 4}, {1, 5}, {2, 6}, {3, 7}
        };
        for (var edge : edges) {
            for (var index : edge) {
                var point = points[index];
                buffer.vertex(matrix, point[0], point[1], point[2])
                    .color(argb)
                    .normal(matrix, 0.0f, 1.0f, 0.0f);
            }
        }
    }

    private static void onTooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null) return;
        var resolver = TooltipResolverRegistry.getInstance().getResolver(event.getItemStack().getItem());
        if (resolver == null) return;

        var helper = new TooltipHelperImpl();
        resolver.resolve(new TooltipResolverRegistry.TooltipResolverContext(
            event.getItemStack(), event.getEntity(), event.getContext()), helper);

        var client = MinecraftClient.getInstance();
        var altDown = InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_ALT);
        var hiddenExtra = false;
        for (var line : helper.lines) {
            if (line.extra && !altDown) {
                hiddenExtra = true;
                continue;
            }
            for (var split : TooltipResolverRegistry.TooltipHelper.splitTextIfExceeds(line.text, helper.width)) {
                event.getToolTip().add(split.getStyle().isEmpty() ? split.copy().setStyle(helper.style) : split);
            }
        }
        if (hiddenExtra) {
            event.getToolTip().add(Text.translatable("tooltip.gimm1q.alt_tooltip",
                Text.translatable("key.keyboard.left.alt").getString()).formatted(Formatting.DARK_GRAY));
        }
    }

    private static void onRenderGui(RenderGuiEvent.Post event) {
        var player = MinecraftClient.getInstance().player;
        if (player == null) return;
        CrosshairTipDrawerImpl.setDrawContext(event.getGuiGraphics());
        try {
            CrosshairTipDrawerCallback.EVENT.invoker().drawCrosshairTip(
                CrosshairTipDrawerImpl.INSTANCE,
                new CrosshairTipDrawerCallback.CrosshairTipDrawerContext(player));
        } finally {
            CrosshairTipDrawerImpl.setDrawContext(null);
        }
    }

    private record TooltipLine(Text text, boolean extra) {}

    private static final class TooltipHelperImpl implements TooltipResolverRegistry.TooltipHelper {
        private final List<TooltipLine> lines = new ArrayList<>();
        private Style style = Style.EMPTY;
        private int width = 1024;

        @Override
        public TooltipResolverRegistry.TooltipHelper addLine(Text text, boolean extra) {
            lines.add(new TooltipLine(text, extra));
            return this;
        }

        @Override
        public TooltipResolverRegistry.TooltipHelper defaultStyle(Style style) {
            this.style = style;
            return this;
        }

        @Override
        public TooltipResolverRegistry.TooltipHelper maxLineWidth(int width) {
            this.width = Math.max(1, width);
            return this;
        }
    }
}
