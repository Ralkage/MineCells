package com.github.mim1q.minecells.progression;

import com.github.mim1q.minecells.MineCells;
import com.github.mim1q.minecells.block.FlagBlock;
import com.github.mim1q.minecells.dimension.MineCellsDimension;
import com.github.mim1q.minecells.item.CellHolderItem;
import com.github.mim1q.minecells.registry.MineCellsBlocks;
import com.github.mim1q.minecells.registry.MineCellsEntities;
import com.github.mim1q.minecells.registry.MineCellsItems;
import com.github.mim1q.minecells.util.MathUtils;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Optional, one-reward-per-run challenges attached to each dungeon's native banner.
 * The state lives in the overworld save so it survives dimension unloads and logouts.
 */
public final class WorldTrials extends PersistentState {
  private static final PersistentState.Type<WorldTrials> TYPE = new PersistentState.Type<>(
    WorldTrials::new, (nbt, lookup) -> read(nbt), DataFixTypes.LEVEL
  );
  private static final int RAMPARTS_CLIMB = 12;
  private static final int CLOCK_TOWER_CLIMB = 16;
  private static final long CRYPT_LIMIT_TICKS = 8 * 60 * 20L;
  private static final long TOWER_LIMIT_TICKS = 5 * 60 * 20L;

  private final Map<Key, Attempt> attempts = new HashMap<>();

  private WorldTrials() {
  }

  private static WorldTrials get(ServerWorld world) {
    return world.getServer().getOverworld().getPersistentStateManager().getOrCreate(TYPE, "minecells_world_trials");
  }

  /** Returns true when this is a trial banner and the interaction was handled. */
  public static boolean useBanner(ServerPlayerEntity player, FlagBlock flag, BlockPos pos) {
    var dimension = MineCellsDimension.of(player.getWorld());
    if (!isTrialBanner(dimension, flag)) return false;

    var world = player.getServerWorld();
    var state = get(world);
    var key = key(player, dimension, pos);
    var attempt = state.attempts.computeIfAbsent(key, ignored -> new Attempt());
    var prefix = "message.minecells.trial." + dimension.key.getValue().getPath();

    if (attempt.status == 2) {
      say(player, Text.translatable("message.minecells.trial.already_completed"));
      return true;
    }
    if (attempt.status == 0) {
      if (dimension == MineCellsDimension.CLOCK_TOWER && towerBannerStage(pos) != 0) {
        say(player, Text.translatable("message.minecells.trial.clock_tower.start_at_entry"));
        return true;
      }
      attempt.start(player, pos);
      state.markDirty();
      say(player, Text.translatable("message.minecells.trial.started", Text.translatable(prefix + ".objective")));
      return true;
    }

    if (isExpired(world, dimension, attempt)) {
      attempt.status = 0;
      state.markDirty();
      say(player, Text.translatable("message.minecells.trial.expired"));
      return true;
    }

    if (dimension == MineCellsDimension.CLOCK_TOWER) {
      int bannerStage = towerBannerStage(pos);
      if (bannerStage == 1 && attempt.puzzleStep == 0) {
        attempt.puzzleStep = 1;
        state.markDirty();
        say(player, Text.translatable("message.minecells.trial.clock_tower.stage_forge"));
        return true;
      }
      if (bannerStage == 2 && attempt.puzzleStep == 0) {
        say(player, Text.translatable("message.minecells.trial.clock_tower.need_forge"));
        return true;
      }
    }

    if (isComplete(player, dimension, attempt, pos)) {
      consumeOffering(player, dimension);
      attempt.status = 2;
      state.markDirty();
      var reward = reward(player, dimension);
      grantAdvancement(player, dimension);
      say(player, Text.translatable("message.minecells.trial.completed", reward.name(), reward.count(), reward.experience()));
      return true;
    }

    say(player, Text.translatable("message.minecells.trial.progress", progress(player, dimension, attempt, pos, prefix)));
    return true;
  }

  /** Called for every nearby participant when one of the two existing arena bosses dies. */
  public static void onBossDefeated(ServerPlayerEntity player, MineCellsDimension dimension, BlockPos bossPos) {
    if (MineCellsDimension.of(player.getWorld()) != dimension) return;
    var world = player.getServerWorld();
    var state = get(world);
    var attempt = state.attempts.get(key(player, dimension, bossPos));
    if (attempt == null || attempt.status != 1 || isExpired(world, dimension, attempt)) return;
    attempt.bossDefeated = true;
    state.markDirty();
    say(player, Text.translatable("message.minecells.trial.boss_defeated"));
  }

  public static boolean isTrialBanner(MineCellsDimension dimension, FlagBlock flag) {
    if (dimension == null) return false;
    return switch (dimension) {
      case PRISONERS_QUARTERS -> flag == MineCellsBlocks.TORN_KINGS_CREST_FLAG || flag == MineCellsBlocks.KINGS_CREST_FLAG;
      case PROMENADE_OF_THE_CONDEMNED -> flag == MineCellsBlocks.PROMENADE_OF_THE_CONDEMNED_FLAG;
      case RAMPARTS -> flag == MineCellsBlocks.RAMPARTS_FLAG;
      case BLACK_BRIDGE -> flag == MineCellsBlocks.BLACK_BRIDGE_FLAG;
      case INSUFFERABLE_CRYPT -> flag == MineCellsBlocks.INSUFFERABLE_CRYPT_FLAG;
      case CLOCK_TOWER -> flag == MineCellsBlocks.CLOCK_TOWER_FLAG;
      default -> false;
    };
  }

  private static Key key(ServerPlayerEntity player, MineCellsDimension dimension, BlockPos pos) {
    var center = MathUtils.getClosestMultiplePosition(pos, 1024);
    return new Key(player.getUuid(), dimension, new BlockPos(center).asLong());
  }

  private static boolean isExpired(ServerWorld world, MineCellsDimension dimension, Attempt attempt) {
    long limit = switch (dimension) {
      case INSUFFERABLE_CRYPT -> CRYPT_LIMIT_TICKS;
      case CLOCK_TOWER -> TOWER_LIMIT_TICKS;
      default -> Long.MAX_VALUE;
    };
    return world.getTime() - attempt.startedAt > limit;
  }

  private static boolean isComplete(ServerPlayerEntity player, MineCellsDimension dimension, Attempt attempt, BlockPos pos) {
    return switch (dimension) {
      case PRISONERS_QUARTERS -> countItem(player, MineCellsItems.MONSTER_CELL) >= 8;
      case PROMENADE_OF_THE_CONDEMNED ->
        killDelta(player, MineCellsEntities.RUNNER, attempt.firstStat) >= 2
          && killDelta(player, MineCellsEntities.GRENADIER, attempt.secondStat) >= 1;
      case RAMPARTS -> pos.getY() - BlockPos.fromLong(attempt.startPos).getY() >= RAMPARTS_CLIMB;
      case BLACK_BRIDGE -> attempt.bossDefeated && countItem(player, MineCellsItems.BOSS_STEM_CELL) >= 1;
      case INSUFFERABLE_CRYPT -> attempt.bossDefeated;
      case CLOCK_TOWER -> attempt.puzzleStep == 1 && towerBannerStage(pos) == 2
        && pos.getY() - BlockPos.fromLong(attempt.startPos).getY() >= CLOCK_TOWER_CLIMB;
      default -> false;
    };
  }

  private static Text progress(ServerPlayerEntity player, MineCellsDimension dimension, Attempt attempt, BlockPos pos, String prefix) {
    return switch (dimension) {
      case PRISONERS_QUARTERS -> Text.translatable(prefix + ".progress", Math.min(8, countItem(player, MineCellsItems.MONSTER_CELL)));
      case PROMENADE_OF_THE_CONDEMNED -> Text.translatable(prefix + ".progress",
        Math.min(2, killDelta(player, MineCellsEntities.RUNNER, attempt.firstStat)),
        Math.min(1, killDelta(player, MineCellsEntities.GRENADIER, attempt.secondStat)));
      case RAMPARTS -> Text.translatable(prefix + ".progress",
        Math.min(RAMPARTS_CLIMB, Math.max(0, pos.getY() - BlockPos.fromLong(attempt.startPos).getY())));
      case BLACK_BRIDGE -> Text.translatable(prefix + ".progress",
        attempt.bossDefeated ? 1 : 0, Math.min(1, countItem(player, MineCellsItems.BOSS_STEM_CELL)));
      case INSUFFERABLE_CRYPT -> Text.translatable(prefix + ".progress",
        attempt.bossDefeated ? 1 : 0, secondsLeft(player.getServerWorld(), CRYPT_LIMIT_TICKS, attempt));
      case CLOCK_TOWER -> Text.translatable(prefix + ".progress",
        attempt.puzzleStep,
        Math.min(CLOCK_TOWER_CLIMB, Math.max(0, pos.getY() - BlockPos.fromLong(attempt.startPos).getY())),
        secondsLeft(player.getServerWorld(), TOWER_LIMIT_TICKS, attempt));
      default -> Text.empty();
    };
  }

  private static long secondsLeft(ServerWorld world, long limit, Attempt attempt) {
    return Math.max(0, (limit - (world.getTime() - attempt.startedAt) + 19) / 20);
  }

  /** The fixed Clock Tower layout assigns one native banner to each puzzle room. */
  private static int towerBannerStage(BlockPos pos) {
    var center = MathUtils.getClosestMultiplePosition(pos, 1024);
    int tileX = Math.floorDiv(pos.getX() - center.getX(), 16);
    int tileZ = Math.floorDiv(pos.getZ() - center.getZ(), 16);
    if (tileX == 0 && tileZ == 0) return 0;  // Entry
    if (tileX == 1 && tileZ == 1) return 1;  // Forge
    if (tileX == 6 && tileZ == 0) return 2;  // Pendulum room
    return -1;
  }

  private static int killDelta(ServerPlayerEntity player, EntityType<?> type, int baseline) {
    return Math.max(0, player.getStatHandler().getStat(Stats.KILLED.getOrCreateStat(type)) - baseline);
  }

  private static int countItem(ServerPlayerEntity player, Item item) {
    int count = 0;
    for (int i = 0; i < player.getInventory().size(); i++) {
      var stack = player.getInventory().getStack(i);
      if (stack.isOf(item)) count += stack.getCount();
      if (item == MineCellsItems.MONSTER_CELL && stack.isOf(MineCellsItems.CELL_HOLDER)) {
        count += CellHolderItem.getCellCount(stack);
      }
    }
    return count;
  }

  private static void consumeOffering(ServerPlayerEntity player, MineCellsDimension dimension) {
    if (dimension == MineCellsDimension.PRISONERS_QUARTERS) {
      consumeItem(player, MineCellsItems.MONSTER_CELL, 8);
    } else if (dimension == MineCellsDimension.BLACK_BRIDGE) {
      consumeItem(player, MineCellsItems.BOSS_STEM_CELL, 1);
    }
  }

  private static void consumeItem(ServerPlayerEntity player, Item item, int amount) {
    int remaining = amount;
    for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
      var stack = player.getInventory().getStack(i);
      if (stack.isOf(item)) {
        int taken = Math.min(remaining, stack.getCount());
        stack.decrement(taken);
        remaining -= taken;
      } else if (item == MineCellsItems.MONSTER_CELL && stack.isOf(MineCellsItems.CELL_HOLDER)) {
        int taken = Math.min(remaining, CellHolderItem.getCellCount(stack));
        CellHolderItem.setCellCount(stack, CellHolderItem.getCellCount(stack) - taken);
        remaining -= taken;
      }
    }
    player.getInventory().markDirty();
  }

  private static Reward reward(ServerPlayerEntity player, MineCellsDimension dimension) {
    int cells = switch (dimension) {
      case PRISONERS_QUARTERS -> 0;
      case PROMENADE_OF_THE_CONDEMNED -> 8;
      case RAMPARTS -> 10;
      case BLACK_BRIDGE -> 16;
      case INSUFFERABLE_CRYPT -> 16;
      case CLOCK_TOWER -> 20;
      default -> 0;
    };
    var stack = dimension == MineCellsDimension.PRISONERS_QUARTERS
      ? new ItemStack(MineCellsItems.HEALTH_FLASK)
      : new ItemStack(MineCellsItems.MONSTER_CELL, cells);
    var itemName = stack.getName();
    var count = stack.getCount();
    player.getInventory().insertStack(stack);
    if (!stack.isEmpty()) player.dropItem(stack, false);
    int experience = 5 + cells * 2;
    player.addExperience(experience);
    return new Reward(itemName, count, experience);
  }

  private static void grantAdvancement(ServerPlayerEntity player, MineCellsDimension dimension) {
    var advancement = player.server.getAdvancementLoader().get(MineCells.createId("trial/" + dimension.key.getValue().getPath()));
    if (advancement != null) player.getAdvancementTracker().grantCriterion(advancement, "trial_complete");
  }

  private static void say(ServerPlayerEntity player, Text text) {
    player.sendMessage(text, false);
  }

  private static WorldTrials read(NbtCompound nbt) {
    var state = new WorldTrials();
    for (var element : nbt.getList("attempts", NbtElement.COMPOUND_TYPE)) {
      var data = (NbtCompound) element;
      try {
        var dimension = MineCellsDimension.of(net.minecraft.util.Identifier.tryParse(data.getString("dimension")));
        if (dimension == null || dimension == MineCellsDimension.OVERWORLD) continue;
        var key = new Key(UUID.fromString(data.getString("owner")), dimension, data.getLong("run_center"));
        state.attempts.put(key, Attempt.read(data));
      } catch (IllegalArgumentException ignored) {
        // A malformed entry does not invalidate the rest of the world save.
      }
    }
    return state;
  }

  @Override
  public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
    var list = new NbtList();
    attempts.forEach((key, attempt) -> {
      var data = attempt.write();
      data.putString("owner", key.owner.toString());
      data.putString("dimension", key.dimension.key.getValue().toString());
      data.putLong("run_center", key.runCenter);
      list.add(data);
    });
    nbt.put("attempts", list);
    return nbt;
  }

  private record Key(UUID owner, MineCellsDimension dimension, long runCenter) {
  }

  private record Reward(Text name, int count, int experience) {
  }

  private static final class Attempt {
    private int status;
    private long startedAt;
    private long startPos;
    private int firstStat;
    private int secondStat;
    private int puzzleStep;
    private boolean bossDefeated;

    private void start(ServerPlayerEntity player, BlockPos pos) {
      status = 1;
      startedAt = player.getServerWorld().getTime();
      startPos = pos.asLong();
      firstStat = player.getStatHandler().getStat(Stats.KILLED.getOrCreateStat(MineCellsEntities.RUNNER));
      secondStat = player.getStatHandler().getStat(Stats.KILLED.getOrCreateStat(MineCellsEntities.GRENADIER));
      puzzleStep = 0;
      bossDefeated = false;
    }

    private static Attempt read(NbtCompound nbt) {
      var attempt = new Attempt();
      attempt.status = nbt.getInt("status");
      attempt.startedAt = nbt.getLong("started_at");
      attempt.startPos = nbt.getLong("start_pos");
      attempt.firstStat = nbt.getInt("first_stat");
      attempt.secondStat = nbt.getInt("second_stat");
      attempt.puzzleStep = nbt.getInt("puzzle_step");
      attempt.bossDefeated = nbt.getBoolean("boss_defeated");
      return attempt;
    }

    private NbtCompound write() {
      var nbt = new NbtCompound();
      nbt.putInt("status", status);
      nbt.putLong("started_at", startedAt);
      nbt.putLong("start_pos", startPos);
      nbt.putInt("first_stat", firstStat);
      nbt.putInt("second_stat", secondStat);
      nbt.putInt("puzzle_step", puzzleStep);
      nbt.putBoolean("boss_defeated", bossDefeated);
      return nbt;
    }
  }
}
