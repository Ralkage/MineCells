package com.github.mim1q.minecells.client.render;

import com.github.mim1q.minecells.MineCells;
import com.github.mim1q.minecells.client.render.model.TimeKeeperEntityModel;
import com.github.mim1q.minecells.entity.boss.TimeKeeperEntity;
import com.github.mim1q.minecells.registry.MineCellsRenderers;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class TimeKeeperEntityRenderer extends MobEntityRenderer<TimeKeeperEntity, TimeKeeperEntityModel> {
  private static final Identifier TEXTURE = MineCells.createId("textures/entity/time_keeper/time_keeper.png");

  public TimeKeeperEntityRenderer(EntityRendererFactory.Context context) {
    super(context, new TimeKeeperEntityModel(context.getPart(MineCellsRenderers.TIME_KEEPER_LAYER)), 0.55F);
  }

  @Override
  public Identifier getTexture(TimeKeeperEntity entity) {
    return TEXTURE;
  }

  @Override
  protected boolean hasLabel(TimeKeeperEntity entity) {
    return false;
  }
}
