package com.github.mim1q.minecells.client.render.item;

import com.github.mim1q.minecells.block.FlagBlock;
import com.github.mim1q.minecells.client.render.blockentity.FlagBlockEntityRenderer.BiomeBannerBlockEntityModel;
import com.github.mim1q.minecells.registry.MineCellsRenderers;
import com.github.mim1q.minecells.registry.MineCellsBlocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

public class BiomeBannerItemRenderer extends BuiltinModelItemRenderer {
  private final BiomeBannerBlockEntityModel model;
  private final FlagBlock flagBlock;

  public BiomeBannerItemRenderer(EntityModelLoader loader, FlagBlock flagBlock) {
    super(MinecraftClient.getInstance().getBlockEntityRenderDispatcher(), loader);
    this.flagBlock = flagBlock;
    this.model = new BiomeBannerBlockEntityModel(
      loader.getModelPart(flagBlock.large ? MineCellsRenderers.FLAG_LARGE_LAYER : MineCellsRenderers.FLAG_LAYER)
    );
  }

  public static void register(RegisterClientExtensionsEvent event) {
    for (var flag : MineCellsBlocks.FLAG_BLOCKS) {
      event.registerItem(new IClientItemExtensions() {
        private BiomeBannerItemRenderer renderer;

        @Override
        public BuiltinModelItemRenderer getCustomRenderer() {
          if (renderer == null) {
            renderer = new BiomeBannerItemRenderer(MinecraftClient.getInstance().getEntityModelLoader(), flag);
          }
          return renderer;
        }
      }, flag.asItem());
    }
  }

  @Override
  public void render(ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
    VertexConsumer consumer = vertexConsumers.getBuffer(model.getLayer(flagBlock.texture));

    matrices.push();

    float scale = 0.4F;
    float x = 1.2F;
    float y = -2.5F;
    float z = -1.0F;

    if (mode == ModelTransformationMode.THIRD_PERSON_LEFT_HAND || mode == ModelTransformationMode.THIRD_PERSON_RIGHT_HAND) {
      y = -1.25F;
    }

    matrices.scale(scale, -scale, -scale);
    matrices.translate(x, y, z);
    model.setupLargeItemModel();
    model.render(matrices, consumer, light, overlay, 0xFFFFFFFF);
    model.resetSegments();
    matrices.pop();
  }
}
