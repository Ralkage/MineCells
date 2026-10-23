package com.github.mim1q.minecells.entity.boss;

import com.github.mim1q.minecells.MineCells;
import com.github.mim1q.minecells.entity.ai.goal.TargetRandomPlayerGoal;
import com.github.mim1q.minecells.entity.ai.goal.TimedActionGoal;
import com.github.mim1q.minecells.entity.ai.goal.TimedDashGoal;
import com.github.mim1q.minecells.entity.ai.goal.WalkTowardsTargetGoal;
import com.github.mim1q.minecells.registry.MineCellsParticles;
import com.github.mim1q.minecells.registry.MineCellsSounds;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.function.Consumer;

import static net.minecraft.entity.data.TrackedDataHandlerRegistry.BOOLEAN;
import static net.minecraft.entity.data.TrackedDataHandlerRegistry.INTEGER;

/** The Clock Tower's boss. All attack damage and phase changes are decided on the server. */
public class TimeKeeperEntity extends MineCellsBossEntity {
  public static final int IDLE = 0;
  public static final int SLASH_CHARGE = 1;
  public static final int SLASH_RELEASE = 2;
  public static final int DASH_CHARGE = 3;
  public static final int DASH_RELEASE = 4;
  public static final int CLOCK_CHARGE = 5;
  public static final int CLOCK_RELEASE = 6;

  private static final TrackedData<Integer> ATTACK_STATE = DataTracker.registerData(TimeKeeperEntity.class, INTEGER);
  private static final TrackedData<Boolean> SECOND_PHASE = DataTracker.registerData(TimeKeeperEntity.class, BOOLEAN);

  private int slashCooldown = 20;
  private int dashCooldown = 100;
  private int clockCooldown = 150;
  private int sharedRecovery = 40;

  public TimeKeeperEntity(EntityType<? extends HostileEntity> type, World world) {
    super(type, world);
  }

  @Override
  protected void initDataTracker(DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(ATTACK_STATE, IDLE);
    builder.add(SECOND_PHASE, false);
  }

  @Override
  protected void initGoals() {
    targetSelector.add(0, new TargetRandomPlayerGoal<>(this));
    targetSelector.add(1, new RevengeGoal(this));
    goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 32.0F));
    goalSelector.add(3, new WalkTowardsTargetGoal(this, 1.15, false, 2.5));

    goalSelector.add(0, new ClockPulseGoal(this));
    goalSelector.add(1, new TimedDashGoal<>(this, settings -> {
      settings.cooldownGetter = () -> dashCooldown + sharedRecovery;
      settings.cooldownSetter = ignored -> {
        dashCooldown = isSecondPhase() ? 105 : 150;
        sharedRecovery = isSecondPhase() ? 12 : 20;
      };
      settings.stateSetter = (state, active) -> setAttackState(DASH_CHARGE, DASH_RELEASE, state, active);
      settings.defaultCooldown = 150;
      settings.chargeSound = MineCellsSounds.TELEPORT_CHARGE;
      settings.releaseSound = MineCellsSounds.TELEPORT_RELEASE;
      settings.actionTick = 22;
      settings.alignTick = 15;
      settings.length = 38;
      settings.speed = 1.2F;
      settings.minDistance = 4.5;
      settings.maxDistance = 17;
      settings.overshoot = 1.5;
      settings.margin = 0.4;
      settings.damage = getDamage(2.0F);
      settings.particle = MineCellsParticles.SPECKLE.get(0xE6B966);
    }, entity -> entity.getAttackState() == IDLE
      && entity.getTarget() != null
      && entity.distanceTo(entity.getTarget()) >= 4.5F
      && entity.distanceTo(entity.getTarget()) <= 17.0F));
    goalSelector.add(2, new ClockSlashGoal(this));
  }

  @Override
  public void tick() {
    super.tick();
    if (getWorld().isClient) {
      spawnTelegraphParticles();
      return;
    }

    if (slashCooldown > 0) slashCooldown--;
    if (dashCooldown > 0) dashCooldown--;
    if (clockCooldown > 0) clockCooldown--;
    if (sharedRecovery > 0) sharedRecovery--;

    if (!isSecondPhase() && isAlive() && getHealth() <= getMaxHealth() * 0.5F) {
      dataTracker.set(SECOND_PHASE, true);
      sharedRecovery = Math.max(sharedRecovery, 30);
      playSound(MineCellsSounds.RISE, 1.5F, 1.3F);
      if (getWorld() instanceof ServerWorld serverWorld) {
        serverWorld.spawnParticles(MineCellsParticles.SPECKLE.get(0x73E2D1), getX(), getY() + 1.4, getZ(),
          80, 1.0, 1.2, 1.0, 0.1);
      }
    }
  }

  private void setAttackState(int charge, int release, TimedActionGoal.State state, boolean active) {
    int value = state == TimedActionGoal.State.CHARGE ? charge : release;
    if (active) dataTracker.set(ATTACK_STATE, value);
    else if (dataTracker.get(ATTACK_STATE) == value) dataTracker.set(ATTACK_STATE, IDLE);
  }

  private void spawnTelegraphParticles() {
    int state = getAttackState();
    if (state != SLASH_CHARGE && state != DASH_CHARGE && state != CLOCK_CHARGE) return;
    if (age % 3 != 0) return;

    var particle = MineCellsParticles.SPECKLE.get(isSecondPhase() ? 0x73E2D1 : 0xE6B966);
    double yaw = Math.toRadians(getYaw());
    if (state == CLOCK_CHARGE) {
      for (int i = 0; i < 20; i++) {
        double angle = 2.0 * Math.PI * i / 20.0 + age * 0.015;
        getWorld().addParticle(particle, getX() + Math.cos(angle) * 6.0, getY() + 0.15,
          getZ() + Math.sin(angle) * 6.0, 0.0, 0.02, 0.0);
      }
    } else if (state == DASH_CHARGE) {
      for (int i = 1; i <= 10; i++) {
        getWorld().addParticle(particle, getX() - Math.sin(yaw) * i, getY() + 0.15,
          getZ() + Math.cos(yaw) * i, 0.0, 0.02, 0.0);
      }
    } else {
      for (int i = -4; i <= 4; i++) {
        double angle = yaw + i * 0.14;
        getWorld().addParticle(particle, getX() - Math.sin(angle) * 3.5, getY() + 1.0,
          getZ() + Math.cos(angle) * 3.5, 0.0, 0.02, 0.0);
      }
    }
  }

  public int getAttackState() {
    return dataTracker.get(ATTACK_STATE);
  }

  public boolean isSecondPhase() {
    return dataTracker.get(SECOND_PHASE);
  }

  @Override
  public void onDeath(DamageSource source) {
    super.onDeath(source);
    if (getWorld().isClient || getServer() == null) return;
    var advancement = getServer().getAdvancementLoader().get(MineCells.createId("time_keeper"));
    if (advancement == null) return;
    for (var player : getWorld().getEntitiesByClass(ServerPlayerEntity.class,
      Box.of(getPos(), 128, 128, 128), candidate -> true)) {
      player.getAdvancementTracker().grantCriterion(advancement, "time_keeper_killed");
    }
  }

  @Override
  public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putInt("slashCooldown", slashCooldown);
    nbt.putInt("dashCooldown", dashCooldown);
    nbt.putInt("clockCooldown", clockCooldown);
    nbt.putInt("sharedRecovery", sharedRecovery);
    nbt.putBoolean("secondPhase", isSecondPhase());
  }

  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    if (nbt.contains("slashCooldown")) slashCooldown = nbt.getInt("slashCooldown");
    if (nbt.contains("dashCooldown")) dashCooldown = nbt.getInt("dashCooldown");
    if (nbt.contains("clockCooldown")) clockCooldown = nbt.getInt("clockCooldown");
    if (nbt.contains("sharedRecovery")) sharedRecovery = nbt.getInt("sharedRecovery");
    dataTracker.set(SECOND_PHASE, nbt.getBoolean("secondPhase") || getHealth() <= getMaxHealth() * 0.5F);
    dataTracker.set(ATTACK_STATE, IDLE);
  }

  public static DefaultAttributeContainer.Builder createTimeKeeperAttributes() {
    return createHostileAttributes()
      .add(EntityAttributes.GENERIC_MAX_HEALTH, 280.0D)
      .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0D)
      .add(EntityAttributes.GENERIC_ARMOR, 4.0D)
      .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28D)
      .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0D)
      .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.85D);
  }

  private static final class ClockSlashGoal extends TimedActionGoal<TimeKeeperEntity> {
    private ClockSlashGoal(TimeKeeperEntity entity) {
      super(entity, (Consumer<TimedActionSettings>) settings -> {
        settings.cooldownGetter = () -> entity.slashCooldown + entity.sharedRecovery;
        settings.cooldownSetter = ignored -> {
          entity.slashCooldown = entity.isSecondPhase() ? 34 : 50;
          entity.sharedRecovery = entity.isSecondPhase() ? 10 : 16;
        };
        settings.stateSetter = (state, active) -> entity.setAttackState(SLASH_CHARGE, SLASH_RELEASE, state, active);
        settings.defaultCooldown = 50;
        settings.chargeSound = MineCellsSounds.KATANA_CHARGE;
        settings.releaseSound = MineCellsSounds.KATANA_RELEASE;
        settings.actionTick = 14;
        settings.length = 26;
      }, e -> e.getAttackState() == IDLE && e.getTarget() != null && e.distanceTo(e.getTarget()) <= 4.5F);
      setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    protected void charge() {
      entity.getNavigation().stop();
      if (entity.getTarget() != null) entity.getLookControl().lookAt(entity.getTarget(), 360.0F, 360.0F);
    }

    @Override
    protected void runAction() {
      Vec3d forward = entity.getRotationVector().multiply(1, 0, 1).normalize();
      for (var target : entity.getWorld().getEntitiesByClass(PlayerEntity.class,
        entity.getBoundingBox().expand(4.0), player -> player.isAlive() && !player.isSpectator())) {
        Vec3d delta = target.getPos().subtract(entity.getPos()).multiply(1, 0, 1);
        if (delta.lengthSquared() > 4.5 * 4.5 || delta.lengthSquared() < 0.001) continue;
        if (delta.normalize().dotProduct(forward) < 0.25) continue;
        if (target.damage(entity.getDamageSources().mobAttack(entity), entity.getDamage(entity.isSecondPhase() ? 2.2F : 1.7F))) {
          target.takeKnockback(1.2, entity.getX() - target.getX(), entity.getZ() - target.getZ());
        }
      }
    }
  }

  private static final class ClockPulseGoal extends TimedActionGoal<TimeKeeperEntity> {
    private ClockPulseGoal(TimeKeeperEntity entity) {
      super(entity, (Consumer<TimedActionSettings>) settings -> {
        settings.cooldownGetter = () -> entity.clockCooldown + entity.sharedRecovery;
        settings.cooldownSetter = ignored -> {
          entity.clockCooldown = entity.isSecondPhase() ? 135 : 190;
          entity.sharedRecovery = entity.isSecondPhase() ? 16 : 30;
        };
        settings.stateSetter = (state, active) -> entity.setAttackState(CLOCK_CHARGE, CLOCK_RELEASE, state, active);
        settings.defaultCooldown = 190;
        settings.chargeSound = MineCellsSounds.CHARGE;
        settings.releaseSound = MineCellsSounds.SHOCK;
        settings.actionTick = 30;
        settings.length = 43;
      }, e -> e.getAttackState() == IDLE && e.getTarget() != null && e.distanceTo(e.getTarget()) <= 8.0F);
      setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    protected void charge() {
      entity.getNavigation().stop();
    }

    @Override
    protected void runAction() {
      Box area = entity.getBoundingBox().expand(6.0, 2.5, 6.0);
      for (var target : entity.getWorld().getEntitiesByClass(PlayerEntity.class, area,
        player -> player.isAlive() && !player.isSpectator())) {
        if (target.squaredDistanceTo(entity) > 6.5 * 6.5) continue;
        target.damage(entity.getDamageSources().magic(), entity.getDamage(entity.isSecondPhase() ? 1.5F : 1.0F));
        target.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, entity.isSecondPhase() ? 70 : 50, 1));
      }
      if (entity.getWorld() instanceof ServerWorld serverWorld) {
        serverWorld.spawnParticles(MineCellsParticles.SPECKLE.get(0x73E2D1), entity.getX(), entity.getY() + 1.0,
          entity.getZ(), 80, 5.0, 1.0, 5.0, 0.1);
      }
    }
  }
}
