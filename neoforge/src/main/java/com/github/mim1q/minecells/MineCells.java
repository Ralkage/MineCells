package com.github.mim1q.minecells;

import com.github.mim1q.minecells.config.MineCellsCommonConfig;
import com.github.mim1q.minecells.cc.MineCellsLevelCC;
import com.github.mim1q.minecells.data.spawner_runes.SpawnerRunesReloadListener;
import com.github.mim1q.minecells.item.weapon.melee.CustomMeleeWeapon;
import com.github.mim1q.minecells.misc.SpecialWeaponLootEntry;
import com.github.mim1q.minecells.network.ServerPacketHandler;
import com.github.mim1q.minecells.registry.*;
import com.github.mim1q.minecells.registry.featureset.WoodSet;
import com.github.mim1q.minecells.structure.MineCellsStructures;
import com.github.mim1q.minecells.world.feature.MineCellsFeatures;
import com.github.mim1q.minecells.world.feature.MineCellsPlacementModifiers;
import com.github.mim1q.minecells.world.feature.MineCellsPlacerTypes;
import com.github.mim1q.minecells.world.feature.MineCellsStructurePlacementTypes;
import dev.mim1q.gimm1q.valuecalculators.ValueCalculatorsReloadedCallback;
import dev.mim1q.gimm1q.Gimm1qCompat;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** NeoForge's entrypoint for the 1.21.1 build. */
@Mod(MineCells.MOD_ID)
public class MineCells {
  public static final String MOD_ID = "minecells";
  public static final Logger LOGGER = LogManager.getLogger();
  public static final MineCellsCommonConfig COMMON_CONFIG = MineCellsCommonConfig.createAndLoad();
  public static final SpawnerRunesReloadListener SPAWNER_RUNE_DATA = new SpawnerRunesReloadListener();

  // Registered during the loot-entry registry event, after NeoForge opens that registry.
  public static LootPoolEntryType SPECIAL_WEAPON_LOOT_ENTRY;

  public MineCells(IEventBus modBus) {
    modBus.addListener(this::onRegister);
    modBus.addListener(MineCellsEntities::registerAttributes);
    modBus.addListener(MineCellsEntities::registerSpawnPlacements);
    NeoForge.EVENT_BUS.addListener(this::onReloadListeners);
    NeoForge.EVENT_BUS.addListener(MineCellsCommands::register);
    NeoForge.EVENT_BUS.addListener(WoodSet::onBlockToolModification);
    NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
    MineCellsGameRules.init();
    Gimm1qCompat.init(modBus);
    ServerPacketHandler.init();
    ValueCalculatorsReloadedCallback.EVENT.register(ids -> CustomMeleeWeapon.updateAttributes());
    if (FMLEnvironment.dist == Dist.CLIENT) {
      MineCellsClient.register(modBus);
    }
    COMMON_CONFIG.save();
  }

  private void onRegister(RegisterEvent event) {
    var registry = event.getRegistryKey().getValue().toString();
    switch (registry) {
      case "minecraft:sound_event" -> MineCellsSounds.init();
      case "neoforge:fluid_type" -> MineCellsFluids.registerFluidTypes();
      case "minecraft:fluid" -> MineCellsFluids.init();
      case "minecraft:mob_effect" -> MineCellsStatusEffects.init();
      case "minecraft:entity_type" -> MineCellsEntities.init();
      case "minecraft:block" -> MineCellsBlocks.init();
      case "minecraft:item" -> {
        MineCellsBlocks.registerItems();
        MineCellsEntities.registerSpawnEggs();
        MineCellsItems.init();
      }
      case "minecraft:block_entity_type" -> MineCellsBlockEntities.init();
      case "minecraft:particle_type" -> MineCellsParticles.init();
      case "minecraft:worldgen/structure_processor" -> MineCellsStructureProcessorTypes.init();
      case "minecraft:worldgen/structure_type" -> MineCellsStructures.registerTypes();
      case "minecraft:worldgen/structure_piece" -> MineCellsStructures.registerPieces();
      case "minecraft:worldgen/feature" -> MineCellsFeatures.init();
      case "minecraft:worldgen/trunk_placer_type" -> MineCellsPlacerTypes.registerTrunks();
      case "minecraft:worldgen/foliage_placer_type" -> MineCellsPlacerTypes.registerFoliage();
      case "minecraft:worldgen/structure_placement" -> MineCellsStructurePlacementTypes.init();
      case "minecraft:worldgen/placement_modifier_type" -> MineCellsPlacementModifiers.init();
      case "minecraft:point_of_interest_type" -> MineCellsPointOfInterestTypes.init();
      case "minecraft:recipe_type" -> MineCellsRecipeTypes.registerType();
      case "minecraft:recipe_serializer" -> MineCellsRecipeTypes.registerSerializer();
      case "minecraft:menu" -> MineCellsScreenHandlerTypes.init();
      case "minecraft:creative_mode_tab" -> MineCellsItemGroups.init();
      case "minecraft:loot_pool_entry_type" -> SPECIAL_WEAPON_LOOT_ENTRY = Registry.register(
        Registries.LOOT_POOL_ENTRY_TYPE,
        createId("special_weapon"),
        new LootPoolEntryType(SpecialWeaponLootEntry.CODEC)
      );
      default -> { }
    }
  }

  private void onReloadListeners(AddReloadListenerEvent event) {
    event.addListener(SPAWNER_RUNE_DATA);
  }

  private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.getEntity() instanceof ServerPlayerEntity player) {
      MineCellsLevelCC.sync(player);
    }
  }

  public static Identifier createId(String path) {
    return Identifier.of(MOD_ID, path);
  }
}
