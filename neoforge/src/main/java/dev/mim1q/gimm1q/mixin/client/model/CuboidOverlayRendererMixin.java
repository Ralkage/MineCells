// Adapted from Gimm1q by Mim1q, MIT License.
// https://github.com/mim1q/Gimm1q/tree/1.21.1
package dev.mim1q.gimm1q.mixin.client.model;

import dev.mim1q.gimm1q.client.render.overlay.ModelOverlayVertexConsumer;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

import static net.minecraft.util.math.MathHelper.EPSILON;

/** Omits flat model cuboids from overlays when skipPlanes is requested. */
@Mixin(ModelPart.Cuboid.class)
public class CuboidOverlayRendererMixin {
    @Unique
    private boolean gimm1q$isPlane;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gimm1q$init(int u, int v,
                             float x, float y, float z,
                             float sizeX, float sizeY, float sizeZ,
                             float extraX, float extraY, float extraZ,
                             boolean mirror,
                             float textureWidth, float textureHeight,
                             Set<Direction> faces,
                             CallbackInfo ci) {
        gimm1q$isPlane = sizeX <= EPSILON || sizeY <= EPSILON || sizeZ <= EPSILON || faces.isEmpty();
    }

    @Inject(method = "renderCuboid", at = @At("HEAD"), cancellable = true)
    private void gimm1q$skipFlatOverlay(MatrixStack.Entry entry,
                                        VertexConsumer consumer,
                                        int light, int overlay, int color,
                                        CallbackInfo ci) {
        if (gimm1q$isPlane && consumer instanceof ModelOverlayVertexConsumer overlayConsumer
            && overlayConsumer.shouldSkipPlanes()) {
            ci.cancel();
        }
    }
}
