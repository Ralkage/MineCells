// From Gimm1q by Mim1q, MIT License.
// https://github.com/mim1q/Gimm1q/tree/1.21.1
package dev.mim1q.gimm1q.accessor.client;

import dev.mim1q.gimm1q.client.render.overlay.ModelOverlayVertexConsumer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

@ApiStatus.NonExtendable
@ApiStatus.Internal
public interface EntityModelVertexConsumerOverrideAccessor {
    void gimm1q$setConsumerOverride(
        @Nullable BiFunction<LivingEntity, VertexConsumerProvider, ModelOverlayVertexConsumer> vertexConsumerPicker,
        @Nullable LivingEntity lastModelEntity
    );

    @Nullable
    VertexConsumer gimm1q$getConsumerOverride(VertexConsumerProvider vertexConsumerProvider);
}
