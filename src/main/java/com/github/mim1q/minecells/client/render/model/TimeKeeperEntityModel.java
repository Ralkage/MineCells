package com.github.mim1q.minecells.client.render.model;

import com.github.mim1q.minecells.entity.boss.TimeKeeperEntity;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class TimeKeeperEntityModel extends EntityModel<TimeKeeperEntity> {
  private final ModelPart figure;
  private final ModelPart body;
  private final ModelPart head;
  private final ModelPart leftArm;
  private final ModelPart rightArm;
  private final ModelPart leftLeg;
  private final ModelPart rightLeg;
  private final ModelPart coat;
  private final ModelPart cape;

  public TimeKeeperEntityModel(ModelPart root) {
    this.figure = root.getChild("figure");
    this.body = figure.getChild("body");
    this.head = figure.getChild("head");
    this.leftArm = figure.getChild("left_arm");
    this.rightArm = figure.getChild("right_arm");
    this.leftLeg = figure.getChild("left_leg");
    this.rightLeg = figure.getChild("right_leg");
    this.coat = body.getChild("coat");
    this.cape = body.getChild("cape");
  }

  public static TexturedModelData getTexturedModelData() {
    ModelData data = new ModelData();
    ModelPartData figure = data.getRoot().addChild("figure", ModelPartBuilder.create(), ModelTransform.pivot(0.0F, 24.0F, 0.0F));

    ModelPartData body = figure.addChild("body",
      ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, -14.0F, -3.0F, 10.0F, 14.0F, 6.0F),
      ModelTransform.pivot(0.0F, -20.0F, 0.0F));
    body.addChild("clock_face",
      ModelPartBuilder.create().uv(52, 60).cuboid(-3.0F, -10.0F, -4.1F, 6.0F, 6.0F, 1.0F),
      ModelTransform.NONE);
    body.addChild("coat",
      ModelPartBuilder.create().uv(0, 70).cuboid(-5.5F, 0.0F, -3.0F, 11.0F, 13.0F, 1.0F),
      ModelTransform.NONE);
    body.addChild("cape",
      ModelPartBuilder.create().uv(0, 92).cuboid(-6.0F, -12.0F, 3.0F, 12.0F, 20.0F, 1.0F),
      ModelTransform.NONE);

    ModelPartData head = figure.addChild("head",
      ModelPartBuilder.create().uv(40, 0).cuboid(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F),
      ModelTransform.pivot(0.0F, -34.0F, 0.0F));
    head.addChild("hood_crown",
      ModelPartBuilder.create().uv(80, 0).cuboid(-5.0F, -12.0F, -5.0F, 10.0F, 3.0F, 10.0F),
      ModelTransform.NONE);
    head.addChild("hood_left",
      ModelPartBuilder.create().uv(80, 25).cuboid(3.0F, -9.0F, -5.0F, 2.0F, 9.0F, 10.0F),
      ModelTransform.NONE);
    head.addChild("hood_right",
      ModelPartBuilder.create().uv(80, 25).cuboid(-5.0F, -9.0F, -5.0F, 2.0F, 9.0F, 10.0F),
      ModelTransform.NONE);

    ModelPartData rightArm = figure.addChild("right_arm",
      ModelPartBuilder.create()
        .uv(24, 30).cuboid(-4.0F, -2.0F, -2.0F, 4.0F, 17.0F, 4.0F)
        .uv(42, 32).cuboid(-5.0F, -3.0F, -3.0F, 6.0F, 4.0F, 6.0F),
      ModelTransform.pivot(-5.0F, -31.0F, 0.0F));
    rightArm.addChild("blade",
      ModelPartBuilder.create().uv(72, 30).cuboid(-3.0F, 12.0F, -4.0F, 2.0F, 16.0F, 1.0F),
      ModelTransform.NONE);

    ModelPartData leftArm = figure.addChild("left_arm",
      ModelPartBuilder.create()
        .uv(24, 30).mirrored().cuboid(0.0F, -2.0F, -2.0F, 4.0F, 17.0F, 4.0F).mirrored(false)
        .uv(42, 32).mirrored().cuboid(-1.0F, -3.0F, -3.0F, 6.0F, 4.0F, 6.0F).mirrored(false),
      ModelTransform.pivot(5.0F, -31.0F, 0.0F));
    leftArm.addChild("blade",
      ModelPartBuilder.create().uv(72, 30).cuboid(1.0F, 12.0F, -4.0F, 2.0F, 16.0F, 1.0F),
      ModelTransform.NONE);

    figure.addChild("right_leg",
      ModelPartBuilder.create().uv(0, 35).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 20.0F, 4.0F),
      ModelTransform.pivot(-3.0F, -20.0F, 0.0F));
    figure.addChild("left_leg",
      ModelPartBuilder.create().uv(0, 35).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 20.0F, 4.0F).mirrored(false),
      ModelTransform.pivot(3.0F, -20.0F, 0.0F));

    return TexturedModelData.of(data, 128, 128);
  }

  @Override
  public void setAngles(TimeKeeperEntity entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
    figure.traverse().forEach(ModelPart::resetTransform);
    float walk = Math.min(limbDistance, 1.0F);
    float stride = MathHelper.cos(limbAngle * 0.6662F) * walk;
    rightLeg.pitch = stride * 0.85F;
    leftLeg.pitch = -stride * 0.85F;
    rightArm.pitch = -stride * 0.3F;
    leftArm.pitch = stride * 0.3F;
    head.yaw = headYaw * MathHelper.RADIANS_PER_DEGREE;
    head.pitch = headPitch * MathHelper.RADIANS_PER_DEGREE;
    body.pitch = 0.03F * MathHelper.sin(animationProgress * 0.08F);
    cape.pitch = 0.05F + 0.06F * MathHelper.sin(animationProgress * (entity.isSecondPhase() ? 0.2F : 0.12F));
    coat.pitch = -0.04F * MathHelper.sin(animationProgress * 0.15F);

    switch (entity.getAttackState()) {
      case TimeKeeperEntity.SLASH_CHARGE -> {
        rightArm.pitch = -2.3F;
        rightArm.yaw = -0.5F;
        leftArm.pitch = -0.7F;
        body.yaw = -0.25F;
      }
      case TimeKeeperEntity.SLASH_RELEASE -> {
        rightArm.pitch = 0.6F;
        rightArm.yaw = 0.8F;
        body.yaw = 0.35F;
      }
      case TimeKeeperEntity.DASH_CHARGE -> {
        body.pitch = 0.45F;
        rightArm.pitch = -0.8F;
        leftArm.pitch = -0.8F;
        rightArm.yaw = -0.8F;
        leftArm.yaw = 0.8F;
        cape.pitch = 0.7F;
      }
      case TimeKeeperEntity.DASH_RELEASE -> {
        body.pitch = 0.8F;
        rightArm.pitch = -1.4F;
        leftArm.pitch = -1.4F;
        cape.pitch = 1.0F;
      }
      case TimeKeeperEntity.CLOCK_CHARGE -> {
        rightArm.pitch = -2.7F;
        leftArm.pitch = -2.7F;
        rightArm.roll = 0.4F;
        leftArm.roll = -0.4F;
        body.pitch = -0.18F;
      }
      case TimeKeeperEntity.CLOCK_RELEASE -> {
        rightArm.pitch = -1.3F;
        leftArm.pitch = -1.3F;
        rightArm.roll = 1.1F;
        leftArm.roll = -1.1F;
      }
      default -> { }
    }
  }

  @Override
  public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
    figure.render(matrices, vertices, light, overlay, color);
  }
}
