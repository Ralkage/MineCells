package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.MineCells;
import com.github.mim1q.minecells.accessor.LivingEntityAccessor;
import com.github.mim1q.minecells.client.render.*;
import com.github.mim1q.minecells.client.render.blockentity.*;
import com.github.mim1q.minecells.client.render.blockentity.BarrierControllerRenderer.BarrierControllerModel;
import com.github.mim1q.minecells.client.render.blockentity.FlagBlockEntityRenderer.BiomeBannerBlockEntityModel;
import com.github.mim1q.minecells.client.render.blockentity.portal.DoorwayPortalBlockEntityRenderer;
import com.github.mim1q.minecells.client.render.blockentity.portal.RiftBlockEntityRenderer;
import com.github.mim1q.minecells.client.render.blockentity.portal.TeleporterBlockEntityRenderer;
import com.github.mim1q.minecells.client.render.blockentity.portal.TeleporterBlockEntityRenderer.TeleporterModel;
import com.github.mim1q.minecells.client.render.blockentity.statue.DecorativeStatueBlockEntityRenderer;
import com.github.mim1q.minecells.client.render.blockentity.statue.KingStatueModel;
import com.github.mim1q.minecells.client.render.conjunctivius.ConjunctiviusEntityRenderer;
import com.github.mim1q.minecells.client.render.conjunctivius.ConjunctiviusEyeRenderer;
import com.github.mim1q.minecells.client.render.conjunctivius.ConjunctiviusSpikeRenderer;
import com.github.mim1q.minecells.client.render.conjunctivius.ConjunctiviusTentacleRenderer;
import com.github.mim1q.minecells.client.render.model.*;
import com.github.mim1q.minecells.client.render.model.conjunctivius.ConjunctiviusEntityModel;
import com.github.mim1q.minecells.client.render.model.nonliving.ElevatorEntityModel;
import com.github.mim1q.minecells.client.render.model.nonliving.ObeliskEntityModel;
import com.github.mim1q.minecells.client.render.model.nonliving.TentacleWeaponEntityModel;
import com.github.mim1q.minecells.client.render.model.nonliving.projectile.BigGrenadeEntityModel;
import com.github.mim1q.minecells.client.render.model.nonliving.projectile.ConjunctiviusProjectileEntityModel;
import com.github.mim1q.minecells.client.render.model.nonliving.projectile.DisgustingWormEggEntityModel;
import com.github.mim1q.minecells.client.render.model.nonliving.projectile.GrenadeEntityModel;
import com.github.mim1q.minecells.client.render.nonliving.*;
import com.github.mim1q.minecells.client.render.nonliving.projectile.*;
import com.github.mim1q.minecells.dimension.MineCellsDimension;
import com.github.mim1q.minecells.effect.MineCellsEffectFlags;
import com.github.mim1q.minecells.item.weapon.bow.CustomBowItem;
import com.github.mim1q.minecells.item.weapon.bow.CustomCrossbowItem;
import com.github.mim1q.minecells.screen.cellcrafter.CellCrafterScreen;
import com.github.mim1q.minecells.util.LegacyUtil;
import com.github.mim1q.minecells.world.FoggyDimensionEffects;
import com.github.mim1q.minecells.world.PromenadeDimensionEffects;
import dev.mim1q.gimm1q.client.render.overlay.ModelOverlayFeatureRenderer;
import dev.mim1q.gimm1q.client.render.overlay.ModelOverlayVertexConsumer;
import dev.mim1q.gimm1q.client.render.overlay.OverlayUvMapper;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MineCellsRenderers {
  private static final Map<EntityModelLayer, Supplier<TexturedModelData>> LAYERS = new LinkedHashMap<>();

  private static <T extends Entity> EntityRenderer<T> invisibleRenderer(Context ctx) {
    return new EntityRenderer<>(ctx) {
      @Override
      public Identifier getTexture(Entity entity) {
        return null;
      }
    };
  }

  public static final EntityModelLayer LEAPING_ZOMBIE_LAYER = new EntityModelLayer(MineCells.createId("leaping_zombie"), "main");
  public static final EntityModelLayer SHOCKER_LAYER = new EntityModelLayer(MineCells.createId("shocker"), "main");
  public static final EntityModelLayer GRENADIER_LAYER = new EntityModelLayer(MineCells.createId("grenadier"), "main");
  public static final EntityModelLayer DISGUSTING_WORM_LAYER = new EntityModelLayer(MineCells.createId("disgusting_worm"), "main");
  public static final EntityModelLayer INQUISITOR_LAYER = new EntityModelLayer(MineCells.createId("inquisitor"), "main");
  public static final EntityModelLayer KAMIKAZE_LAYER = new EntityModelLayer(MineCells.createId("kamikaze"), "main");
  public static final EntityModelLayer PROTECTOR_LAYER = new EntityModelLayer(MineCells.createId("protector"), "main");
  public static final EntityModelLayer UNDEAD_ARCHER_LAYER = new EntityModelLayer(MineCells.createId("undead_archer"), "main");
  public static final EntityModelLayer SHIELDBEARER_LAYER = new EntityModelLayer(MineCells.createId("shieldbearer"), "main");
  public static final EntityModelLayer MUTATED_BAT_LAYER = new EntityModelLayer(MineCells.createId("mutated_bat"), "main");
  public static final EntityModelLayer SEWERS_TENTACLE_LAYER = new EntityModelLayer(MineCells.createId("sewers_tentacle"), "main");
  public static final EntityModelLayer RANCID_RAT_LAYER = new EntityModelLayer(MineCells.createId("rancid_rat"), "main");
  public static final EntityModelLayer RUNNER_LAYER = new EntityModelLayer(MineCells.createId("runner"), "main");
  public static final EntityModelLayer SCORPION_LAYER = new EntityModelLayer(MineCells.createId("scorpion"), "main");
  public static final EntityModelLayer FLY_LAYER = registerLayer("fly", FlyEntityModel::getTexturedModelData);
  public static final EntityModelLayer SWEEPER_LAYER = registerLayer("sweeper", SweeperEntityModel::getTexturedModelData);

  public static final EntityModelLayer CONJUNCTIVIUS_MAIN_LAYER = new EntityModelLayer(MineCells.createId("conjunctivius"), "main");
  public static final EntityModelLayer CONJUNCTIVIUS_EYE_LAYER = new EntityModelLayer(MineCells.createId("conjunctivius"), "eye");
  public static final EntityModelLayer CONJUNCTIVIUS_TENTACLE_LAYER = new EntityModelLayer(MineCells.createId("conjunctivius"), "tentacle");
  public static final EntityModelLayer CONJUNCTIVIUS_SPIKE_LAYER = new EntityModelLayer(MineCells.createId("conjunctivius"), "spike");

  public static final EntityModelLayer CONCIERGE_LAYER = registerLayer("concierge", ConciergeEntityModel::getTexturedModelData);
  public static final EntityModelLayer TIME_KEEPER_LAYER = registerLayer("time_keeper", TimeKeeperEntityModel::getTexturedModelData);

  public static final EntityModelLayer GRENADE_LAYER = new EntityModelLayer(MineCells.createId("grenade"), "main");
  public static final EntityModelLayer BIG_GRENADE_LAYER = new EntityModelLayer(MineCells.createId("big_grenade"), "main");
  public static final EntityModelLayer DISGUSTING_WORM_EGG_LAYER = new EntityModelLayer(MineCells.createId("disgusting_worm_egg"), "main");
  public static final EntityModelLayer CONJUNCTIVIUS_PROJECTILE_LAYER = new EntityModelLayer(MineCells.createId("conjunctivius_projectile"), "main");

  public static final EntityModelLayer ELEVATOR_LAYER = new EntityModelLayer(MineCells.createId("elevator"), "main");
  public static final EntityModelLayer TENTACLE_WEAPON_LAYER = new EntityModelLayer(MineCells.createId("tentacle_weapon"), "main");
  public static final EntityModelLayer OBELISK_LAYER = new EntityModelLayer(MineCells.createId("obelisk"), "main");

  public static final EntityModelLayer BIG_DOOR_BARRIER_LAYER = registerLayer("barrier_controller", "big_door", BarrierControllerModel::getBigDoorTexturedModelData);

  public static final EntityModelLayer FLAG_LAYER = registerLayer("flag", () -> BiomeBannerBlockEntityModel.getTexturedModelData(false));
  public static final EntityModelLayer FLAG_LARGE_LAYER = registerLayer("flag", "large", () -> BiomeBannerBlockEntityModel.getTexturedModelData(true));
  public static final EntityModelLayer ARROW_SIGN_LAYER = registerLayer("arrow_sign", ArrowSignBlockEntityRenderer::getTexturedModelData);

  public static final EntityModelLayer KING_STATUE_LAYER = new EntityModelLayer(MineCells.createId("king_statue"), "main");
  public static final EntityModelLayer TELEPORTER_LAYER = registerLayer("teleporter", TeleporterModel::getTexturedModelData);

  private static EntityModelLayer registerLayer(String path, String name, Supplier<TexturedModelData> provider) {
    var layer = new EntityModelLayer(MineCells.createId(path), name);
    LAYERS.put(layer, provider);
    return layer;
  }

  private static EntityModelLayer registerLayer(String path, Supplier<TexturedModelData> provider) {
    return registerLayer(path, "main", provider);
  }

  public static void register(IEventBus modBus) {
    registerDefaultLayers();
    modBus.addListener(MineCellsRenderers::onRegisterLayers);
    modBus.addListener(MineCellsRenderers::onRegisterRenderers);
    modBus.addListener(MineCellsRenderers::onDimensionEffects);
    modBus.addListener(MineCellsRenderers::onMenuScreens);
    modBus.addListener(MineCellsRenderers::onBlockColors);
    modBus.addListener(MineCellsRenderers::onItemColors);
    modBus.addListener(MineCellsRenderers::onAddLayers);
    modBus.addListener(MineCellsRenderers::onClientSetup);
  }

  private static void registerLayerDefinition(EntityModelLayer layer, Supplier<TexturedModelData> provider) {
    LAYERS.put(layer, provider);
  }

  private static void registerDefaultLayers() {
    registerLayerDefinition(LEAPING_ZOMBIE_LAYER, LeapingZombieEntityModel::getTexturedModelData);
    registerLayerDefinition(SHOCKER_LAYER, ShockerEntityModel::getTexturedModelData);
    registerLayerDefinition(GRENADIER_LAYER, GrenadierEntityModel::getTexturedModelData);
    registerLayerDefinition(DISGUSTING_WORM_LAYER, DisgustingWormEntityModel::getTexturedModelData);
    registerLayerDefinition(INQUISITOR_LAYER, InquisitorEntityModel::getTexturedModelData);
    registerLayerDefinition(KAMIKAZE_LAYER, KamikazeEntityModel::getTexturedModelData);
    registerLayerDefinition(PROTECTOR_LAYER, ProtectorEntityModel::getTexturedModelData);
    registerLayerDefinition(UNDEAD_ARCHER_LAYER, UndeadArcherEntityModel::getTexturedModelData);
    registerLayerDefinition(SHIELDBEARER_LAYER, ShieldbearerEntityModel::getTexturedModelData);
    registerLayerDefinition(MUTATED_BAT_LAYER, MutatedBatEntityModel::getTexturedModelData);
    registerLayerDefinition(SEWERS_TENTACLE_LAYER, SewersTentacleEntityModel::getTexturedModelData);
    registerLayerDefinition(RANCID_RAT_LAYER, RancidRatEntityModel::getTexturedModelData);
    registerLayerDefinition(RUNNER_LAYER, RunnerEntityModel::getTexturedModelData);
    registerLayerDefinition(SCORPION_LAYER, ScorpionEntityModel::getTexturedModelData);

    registerLayerDefinition(CONJUNCTIVIUS_MAIN_LAYER, ConjunctiviusEntityModel::getTexturedModelData);
    registerLayerDefinition(CONJUNCTIVIUS_EYE_LAYER, ConjunctiviusEyeRenderer.ConjunctiviusEyeModel::getTexturedModelData);
    registerLayerDefinition(CONJUNCTIVIUS_TENTACLE_LAYER, ConjunctiviusTentacleRenderer.ConjunctiviusTentacleModel::getTexturedModelData);
    registerLayerDefinition(CONJUNCTIVIUS_SPIKE_LAYER, ConjunctiviusSpikeRenderer.ConjunctiviusSpikeModel::getTexturedModelData);

    registerLayerDefinition(GRENADE_LAYER, GrenadeEntityModel::getTexturedModelData);
    registerLayerDefinition(BIG_GRENADE_LAYER, BigGrenadeEntityModel::getTexturedModelData);
    registerLayerDefinition(DISGUSTING_WORM_EGG_LAYER, DisgustingWormEggEntityModel::getTexturedModelData);
    registerLayerDefinition(CONJUNCTIVIUS_PROJECTILE_LAYER, ConjunctiviusProjectileEntityModel::getTexturedModelData);

    registerLayerDefinition(ELEVATOR_LAYER, ElevatorEntityModel::getTexturedModelData);
    registerLayerDefinition(TENTACLE_WEAPON_LAYER, TentacleWeaponEntityModel::getTexturedModelData);
    registerLayerDefinition(OBELISK_LAYER, ObeliskEntityModel::getTexturedModelData);

    registerLayerDefinition(KING_STATUE_LAYER, KingStatueModel::getTexturedModelData);
  }

  private static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
    LAYERS.forEach(event::registerLayerDefinition);
  }

  private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
    event.registerEntityRenderer(MineCellsEntities.LEAPING_ZOMBIE, LeapingZombieEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.SHOCKER, ShockerEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.GRENADIER, GrenadierEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.DISGUSTING_WORM, DisgustingWormEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.INQUISITOR, InquisitorEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.KAMIKAZE, KamikazeEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.PROTECTOR, ProtectorEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.UNDEAD_ARCHER, UndeadArcherEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.SHIELDBEARER, ShieldbearerEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.MUTATED_BAT, MutatedBatEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.SEWERS_TENTACLE, SewersTentacleEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.RANCID_RAT, RancidRatEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.RUNNER, RunnerEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.SCORPION, ScorpionEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.BUZZCUTTER, ctx -> new FlyEntityRenderer<>(ctx, "buzzcutter"));
    event.registerEntityRenderer(MineCellsEntities.SWEEPER, SweeperEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.CONJUNCTIVIUS, ConjunctiviusEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.CONCIERGE, ConciergeEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.TIME_KEEPER, TimeKeeperEntityRenderer::new);

    event.registerEntityRenderer(MineCellsEntities.GRENADE, GrenadeEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.BIG_GRENADE, BigGrenadeEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.DISGUSTING_WORM_EGG, DisgustingWormEggEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.MAGIC_ORB, MagicOrbEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.SCORPION_SPIT, ScorpionSpitEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.CONJUNCTIVIUS_PROJECTILE, ConjunctiviusProjectileEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.CUSTOM_ARROW, CustomArrowEntityRenderer::new);

    event.registerEntityRenderer(MineCellsEntities.ELEVATOR, ElevatorEntityRenderer::new);
    event.registerEntityRenderer(MineCellsEntities.TENTACLE_WEAPON, TentacleWeaponEntityRenderer::new);

    event.registerEntityRenderer(MineCellsEntities.CONJUNCTIVIUS_OBELISK, ctx -> new ObeliskEntityRenderer(ctx, "conjunctivius"));
    event.registerEntityRenderer(MineCellsEntities.CONCIERGE_OBELISK, ctx -> new ObeliskEntityRenderer(ctx, "concierge"));
    event.registerEntityRenderer(MineCellsEntities.ELITE_OBELISK, ctx -> new ObeliskEntityRenderer(ctx, "elite"));

    event.registerEntityRenderer(MineCellsEntities.SHOCKWAVE_PLACER, MineCellsRenderers::invisibleRenderer);
    event.registerEntityRenderer(MineCellsEntities.SPAWNER_RUNE, SpawnerRuneRenderer.Entity::new);

    event.registerBlockEntityRenderer(MineCellsBlockEntities.FLAG_BLOCK_ENTITY, FlagBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(MineCellsBlockEntities.DECORATIVE_STATUE_BLOCK_ENTITY, DecorativeStatueBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(MineCellsBlockEntities.RETURN_STONE, ReturnStoneBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(MineCellsBlockEntities.RUNIC_VINE_PLANT, RunicVinePlantBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(MineCellsBlockEntities.ARROW_SIGN, ArrowSignBlockEntityRenderer::new);

    event.registerBlockEntityRenderer(MineCellsBlockEntities.BARRIER_CONTROLLER, BarrierControllerRenderer::new);

    event.registerBlockEntityRenderer(MineCellsBlockEntities.TELEPORTER, TeleporterBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(MineCellsBlockEntities.DOORWAY, DoorwayPortalBlockEntityRenderer::new);
    event.registerBlockEntityRenderer(MineCellsBlockEntities.RIFT, RiftBlockEntityRenderer::new);

    event.registerBlockEntityRenderer(MineCellsBlockEntities.CELL_CRAFTER, ctx -> new CellCrafterBlockEntityRenderer());

    event.registerBlockEntityRenderer(MineCellsBlockEntities.SPAWNER_RUNE, SpawnerRuneRenderer.BlockEntity::new);

  }

  private static void onDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
    event.register(MineCells.createId("foggy"), new FoggyDimensionEffects());
    event.register(MineCells.createId("promenade"), new PromenadeDimensionEffects());
  }

  private static void onMenuScreens(RegisterMenuScreensEvent event) {
    event.register(MineCellsScreenHandlerTypes.CELL_FORGE_SCREEN_HANDLER, CellCrafterScreen::new);
  }

  private static void onClientSetup(FMLClientSetupEvent event) {
    event.enqueueWork(() -> {
    setBlockRenderLayers(
      RenderLayer.getCutout(),
      MineCellsBlocks.BIG_CHAIN,
      MineCellsBlocks.CHAIN_PILE,
      MineCellsBlocks.CAGE,
      MineCellsBlocks.BROKEN_CAGE,
      MineCellsBlocks.SPIKES,
      MineCellsBlocks.HANGED_SKELETON,
      MineCellsBlocks.SKELETON,
      MineCellsBlocks.CORPSE,
      MineCellsBlocks.ROTTING_CORPSE,
      MineCellsBlocks.HANGED_ROTTING_CORPSE,
      MineCellsBlocks.HANGED_CORPSE,
      MineCellsBlocks.WILTED_LEAVES.leaves,
      MineCellsBlocks.WILTED_LEAVES.hangingLeaves,
      MineCellsBlocks.WILTED_LEAVES.wallLeaves,
      MineCellsBlocks.ORANGE_WILTED_LEAVES.leaves,
      MineCellsBlocks.ORANGE_WILTED_LEAVES.hangingLeaves,
      MineCellsBlocks.ORANGE_WILTED_LEAVES.wallLeaves,
      MineCellsBlocks.RED_WILTED_LEAVES.leaves,
      MineCellsBlocks.RED_WILTED_LEAVES.hangingLeaves,
      MineCellsBlocks.RED_WILTED_LEAVES.wallLeaves,
      MineCellsBlocks.RUNIC_VINE,
      MineCellsBlocks.RUNIC_VINE_PLANT,
      MineCellsBlocks.UNBREAKABLE_CHAIN,
      MineCellsBlocks.CELL_CRAFTER,
      MineCellsBlocks.UNBREAKABLE_CELL_CRAFTER,
      MineCellsBlocks.ALCHEMY_EQUIPMENT_0,
      MineCellsBlocks.ALCHEMY_EQUIPMENT_2,
      MineCellsBlocks.PUTRID_WOOD.door,
      MineCellsBlocks.PRISON_TORCH,
      MineCellsBlocks.PROMENADE_TORCH,
      MineCellsBlocks.RAMPARTS_TORCH,
      MineCellsBlocks.WILTED_GRASS_BLOCK,
      MineCellsBlocks.BLOOMROCK_WILTED_GRASS_BLOCK,
      MineCellsBlocks.RED_PUTRID_SAPLING,
      MineCellsBlocks.ORANGE_PUTRID_SAPLING,
      MineCellsBlocks.PUTRID_SAPLING,
      MineCellsBlocks.SHOCKWAVE_FLAME,
      MineCellsBlocks.SHOCKWAVE_FLAME_PLAYER
    );
    setBlockRenderLayers(
      RenderLayer.getTranslucent(),
      MineCellsBlocks.ALCHEMY_EQUIPMENT_1,
      MineCellsBlocks.SEWAGE,
      MineCellsBlocks.ANCIENT_SEWAGE
    );

    setFluidRenderLayers(RenderLayer.getTranslucent(),
      MineCellsFluids.STILL_SEWAGE,
      MineCellsFluids.FLOWING_SEWAGE,
      MineCellsFluids.STILL_ANCIENT_SEWAGE,
      MineCellsFluids.FLOWING_ANCIENT_SEWAGE);

    ModelPredicateProviderRegistry.register(
      MineCellsItems.HATTORIS_KATANA,
      Identifier.of("blocking"),
      (stack, world, entity, i) -> entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F
    );

    ModelPredicateProviderRegistry.register(
      MineCellsItems.CELL_HOLDER,
      MineCells.createId("cells"),
      (stack, world, entity, seed) -> {
        var nbt = LegacyUtil.getOrCreateNbt(stack);
        if (nbt == null) return 0f;

        var cells = nbt.getInt("Cells");
        if (cells > 128) return 1.0f;
        if (cells > 64) return 0.75f;
        if (cells > 0) return 0.5f;
        return 0f;
      }
    );

    MineCellsItems.BOWS.forEach(MineCellsRenderers::registerBowPredicate);
    MineCellsItems.CROSSBOWS.forEach(MineCellsRenderers::registerCrossbowPredicate);
    MineCellsItems.SHIELDS.forEach(MineCellsRenderers::registerShieldPredicate);

    });
  }

  private static void onBlockColors(RegisterColorHandlersEvent.Block event) {
    event.register(
      (state, world, pos, tintIndex) -> (world == null || pos == null) ? 0x80CC80 : BiomeColors.getFoliageColor(world, pos),
      MineCellsBlocks.WILTED_LEAVES.leaves, MineCellsBlocks.WILTED_LEAVES.hangingLeaves
    );

    // Blocks colored by dimension
    event.register(
      (state, world, pos, tintIndex) -> {
        if (world instanceof World realWorld) {
          var dim = MineCellsDimension.of(realWorld);
          if (dim == null) return 0xFFC540;
          return dim.getColor();
        }
        return 0xFFC540;
      },
      MineCellsBlocks.SPAWNER_RUNE
    );

    event.register(
      (state, world, pos, tintIndex) -> {
        if (world == null || pos == null) return 0x80CC80;
        //noinspection deprecation
        long l = MathHelper.hashCode(pos.getX(), pos.getY(), pos.getZ());
        double x = MathHelper.clamp((((l & 15L) / 15.0F) - 0.5) * 0.5, -1, 1);
        x *= 0.5;
        x += 1;
        var color = BiomeColors.getFoliageColor(world, pos);
        var r = (int) ((color >> 16 & 0xFF) * x);
        var g = (int) ((color >> 8 & 0xFF) * x);
        var b = (int) ((color & 0xFF) * x);
        return (r << 16) | (g << 8) | b;
      },
      MineCellsBlocks.WILTED_LEAVES.wallLeaves
    );

    event.register(
      (state, world, pos, tintIndex) -> world == null ? 0x80CC80 : BiomeColors.getGrassColor(world, pos),
      MineCellsBlocks.WILTED_GRASS_BLOCK, MineCellsBlocks.BLOOMROCK_WILTED_GRASS_BLOCK
    );

  }

  private static void onItemColors(RegisterColorHandlersEvent.Item event) {
    event.register((stack, tintIndex) -> 0x80CC80,
      MineCellsBlocks.WILTED_LEAVES.leaves, MineCellsBlocks.WILTED_LEAVES.hangingLeaves, MineCellsBlocks.WILTED_LEAVES.wallLeaves,
      MineCellsBlocks.WILTED_GRASS_BLOCK, MineCellsBlocks.BLOOMROCK_WILTED_GRASS_BLOCK
    );

    for (var entry : MineCellsItems.DOORWAY_COLORS.entrySet()) {
      event.register((stack, tintIndex) -> tintIndex == 1 ? entry.getValue() : 0xFFFFFF, entry.getKey());
    }

  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
    for (var type : event.getEntityTypes()) {
      addOverlayLayers((EntityRenderer) event.getRenderer(type));
    }
    for (var skin : event.getSkins()) {
      addOverlayLayers((EntityRenderer) event.getSkin(skin));
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static void addOverlayLayers(EntityRenderer renderer) {
    if (!(renderer instanceof LivingEntityRenderer livingRenderer)) return;
    var iceTexture = Identifier.of("textures/block/ice.png");
    var bloodOverlay = MineCells.createId("textures/entity/effect/blood_overlay.png");
    livingRenderer.addFeature(ModelOverlayFeatureRenderer.of(
      (LivingEntity entity) -> ((LivingEntityAccessor) entity).getMineCellsFlag(MineCellsEffectFlags.FROZEN),
      (LivingEntity entity, net.minecraft.client.render.VertexConsumerProvider consumers) -> ModelOverlayVertexConsumer
        .of(consumers.getBuffer(RenderLayer.getEntityTranslucent(iceTexture)))
        .offset(2.0322f).skipPlanes(), true
    ).apply((FeatureRendererContext<LivingEntity, EntityModel<LivingEntity>>) livingRenderer));
    livingRenderer.addFeature(ModelOverlayFeatureRenderer.of(
      (LivingEntity entity) -> ((LivingEntityAccessor) entity).getMineCellsFlag(MineCellsEffectFlags.BLEEDING),
      (LivingEntity entity, net.minecraft.client.render.VertexConsumerProvider consumers) -> ModelOverlayVertexConsumer
        .of(consumers.getBuffer(RenderLayer.getEntityTranslucent(bloodOverlay)))
        .offset(0.0321f).mapUv(OverlayUvMapper.verticalScrollAnimation(-0.06f)).skipPlanes(), true
    ).apply((FeatureRendererContext<LivingEntity, EntityModel<LivingEntity>>) livingRenderer));
  }

  private static void setBlockRenderLayers(RenderLayer layer, net.minecraft.block.Block... blocks) {
    for (var block : blocks) RenderLayers.setRenderLayer(block, layer);
  }

  private static void setFluidRenderLayers(RenderLayer layer, net.minecraft.fluid.Fluid... fluids) {
    for (var fluid : fluids) RenderLayers.setRenderLayer(fluid, layer);
  }

  private static void registerBowPredicate(CustomBowItem item) {
    ModelPredicateProviderRegistry.register(item, MineCells.createId("pulling"), (stack, world, entity, seed) ->
      entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F
    );
    ModelPredicateProviderRegistry.register(item, MineCells.createId("pull"), (stack, world, entity, seed) -> {
      if (entity == null) {
        return 0.0F;
      } else {
        var stackItem = (CustomBowItem) stack.getItem();
        return entity.getActiveItem() != stack
          ? 0.0F
          : (float) entity.getItemUseTime() / stackItem.getDrawTime(entity, stack);
      }
    });
  }

  private static void registerCrossbowPredicate(CustomCrossbowItem item) {
    ModelPredicateProviderRegistry.register(item, MineCells.createId("pull"), (stack, world, entity, seed) -> {
      if (entity == null) {
        return 0.0F;
      } else {
        var stackItem = (CustomCrossbowItem) stack.getItem();
        return CrossbowItem.isCharged(stack)
          ? 0.0F
          : (float) entity.getItemUseTime() / stackItem.getDrawTime(entity, stack);
      }
    });

    ModelPredicateProviderRegistry.register(
      item,
      MineCells.createId("pulling"),
      (stack, world, entity, seed) -> entity != null
        && entity.isUsingItem()
        && entity.getActiveItem() == stack
        && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);

    ModelPredicateProviderRegistry.register(
      item,
      MineCells.createId("charged"),
      (stack, world, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
    );
  }

  private static void registerShieldPredicate(Item item) {
    ModelPredicateProviderRegistry.register(item, MineCells.createId("blocking"), (stack, world, entity, seed) ->
      entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F
    );
  }
}
