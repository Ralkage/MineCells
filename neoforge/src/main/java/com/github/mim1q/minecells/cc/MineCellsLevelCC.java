package com.github.mim1q.minecells.cc;

import com.github.mim1q.minecells.dimension.MineCellsDimension;
import com.github.mim1q.minecells.network.PortalNetworkBridge;
import com.github.mim1q.minecells.util.MathUtils;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Persistent portal and return-point state for the NeoForge build. */
public final class MineCellsLevelCC {
  private static final PersistentState.Type<State> STATE_TYPE = new PersistentState.Type<>(
    State::new, (nbt, lookup) -> State.read(nbt), DataFixTypes.LEVEL
  );

  private MineCellsLevelCC() {
  }

  private static State state(MinecraftServer server) {
    return server.getOverworld().getPersistentStateManager().getOrCreate(STATE_TYPE, "minecells_progression");
  }

  public static void sync(ServerPlayerEntity player) {
    PortalNetworkBridge.sync(player);
  }

  public static final class PortalsCC {
    private static final List<PortalData> CLIENT_PORTALS = new ArrayList<>();

    private PortalsCC() {
    }

    public static List<PortalData> getPortals(ServerPlayerEntity player) {
      return state(player.server).portalsByOwner.get(player.getUuid());
    }

    public static boolean createPortal(ServerPlayerEntity player) {
      return createPortalDataForPlayer(player).isPresent();
    }

    private static Optional<PortalData> createPortalDataForPlayer(ServerPlayerEntity player) {
      var data = state(player.server);
      var current = data.portalsByOwner.get(player.getUuid());
      if (current != null && current.size() > 1) {
        return Optional.empty();
      }

      var pos = MathUtils.getSpiralPosition(data.portals.size()).multiply(1024);
      var portal = new PortalData(player.getUuid(), new BlockPos(pos), EnumSet.noneOf(MineCellsDimension.class));
      data.addPortal(portal);
      data.markDirty();
      sync(player);
      return Optional.of(portal);
    }

    public static PortalData getOrCreatePortal(ServerPlayerEntity player) {
      var current = getPortals(player);
      if (current == null || current.isEmpty()) {
        return createPortalDataForPlayer(player).orElseThrow();
      }
      return current.getFirst();
    }

    public static Optional<PortalData> findDataOfPosition(World world, BlockPos pos) {
      var centerPos = new BlockPos(MathUtils.getClosestMultiplePosition(pos, 1024));
      var portals = world instanceof ServerWorld serverWorld
        ? state(serverWorld.getServer()).portals
        : CLIENT_PORTALS;
      return portals.stream().filter(p -> p.runCenter.equals(centerPos)).findFirst();
    }

    public static void visitDimension(ServerWorld world, BlockPos posOverride, MineCellsDimension dimension) {
      var portal = findDataOfPosition(world, posOverride);
      if (portal.isEmpty() || !portal.get().visitedDimensions.add(dimension)) {
        return;
      }
      state(world.getServer()).markDirty();
      var owner = world.getServer().getPlayerManager().getPlayer(portal.get().owner);
      if (owner != null) {
        sync(owner);
      }
    }

    public static NbtCompound writePlayerNbt(ServerPlayerEntity player) {
      var nbt = new NbtCompound();
      var list = new NbtList();
      var portals = getPortals(player);
      if (portals != null) {
        portals.forEach(portal -> list.add(portal.createNbt()));
      }
      nbt.put("portals", list);
      return nbt;
    }

    public static NbtCompound writeAllNbt(MinecraftServer server) {
      var nbt = new NbtCompound();
      var list = new NbtList();
      state(server).portals.forEach(portal -> list.add(portal.createNbt()));
      nbt.put("portals", list);
      return nbt;
    }

    public static void applyClientSync(NbtCompound nbt) {
      CLIENT_PORTALS.clear();
      var list = nbt.getList("portals", NbtElement.COMPOUND_TYPE);
      for (var element : list) {
        CLIENT_PORTALS.add(PortalData.fromNbt((NbtCompound) element));
      }
    }
  }

  public record PortalData(UUID owner, BlockPos runCenter, EnumSet<MineCellsDimension> visitedDimensions) {
    public NbtCompound createNbt() {
      var nbt = new NbtCompound();
      nbt.putString("owner", owner.toString());
      nbt.putLong("run_center", runCenter.asLong());
      var list = new NbtList();
      for (var dimension : visitedDimensions) {
        list.add(NbtString.of(dimension.key.getValue().toString()));
      }
      nbt.put("visited_dimensions", list);
      return nbt;
    }

    public static PortalData fromNbt(NbtCompound nbt) {
      var visited = EnumSet.noneOf(MineCellsDimension.class);
      var list = nbt.getList("visited_dimensions", NbtElement.STRING_TYPE);
      for (var element : list) {
        var dimension = MineCellsDimension.of(Identifier.tryParse(element.asString()));
        if (dimension != null) {
          visited.add(dimension);
        }
      }
      return new PortalData(
        UUID.fromString(nbt.getString("owner")),
        BlockPos.fromLong(nbt.getLong("run_center")),
        visited
      );
    }
  }

  public static final class OverworldEntriesCC {
    private OverworldEntriesCC() {
    }

    public static Optional<Data> getPlayerEntrancePoint(ServerPlayerEntity player, BlockPos pos, ServerWorld world) {
      var entry = state(world.getServer()).entries.get(player.getUuid());
      if (entry == null || !entry.entrancePos.equals(pos)) {
        return Optional.empty();
      }
      return Optional.of(entry);
    }

    public static void setPlayerEntrancePoint(
      ServerPlayerEntity player,
      BlockPos portalPos,
      BlockPos entrancePos,
      float entranceRot,
      ServerWorld world
    ) {
      var data = state(world.getServer());
      data.entries.put(player.getUuid(), new Data(portalPos, entrancePos, entranceRot));
      data.markDirty();
    }

    public static Pair<BlockPos, Float> getPlayerEntrancePos(ServerPlayerEntity player) {
      var world = player.getServerWorld();
      var entry = state(world.getServer()).entries.get(player.getUuid());
      if (entry != null) {
        return new Pair<>(entry.posOverride, entry.entranceRotation);
      }
      if (player.getSpawnPointDimension() == MineCellsDimension.OVERWORLD.key) {
        var pos = player.getSpawnPointPosition();
        if (pos != null) {
          return new Pair<>(pos, player.getSpawnAngle());
        }
      }
      var overworld = world.getServer().getOverworld();
      return new Pair<>(overworld.getSpawnPos(), overworld.getSpawnAngle());
    }

    public record Data(BlockPos posOverride, BlockPos entrancePos, float entranceRotation) {
    }
  }

  private static final class State extends PersistentState {
    private final List<PortalData> portals = new ArrayList<>();
    private final Map<UUID, List<PortalData>> portalsByOwner = new HashMap<>();
    private final Map<UUID, OverworldEntriesCC.Data> entries = new HashMap<>();

    private void addPortal(PortalData portal) {
      portals.add(portal);
      portalsByOwner.computeIfAbsent(portal.owner, ignored -> new ArrayList<>()).add(portal);
    }

    private static State read(NbtCompound nbt) {
      var result = new State();
      for (var element : nbt.getList("portals", NbtElement.COMPOUND_TYPE)) {
        result.addPortal(PortalData.fromNbt((NbtCompound) element));
      }
      var entriesNbt = nbt.getCompound("entries");
      for (var key : entriesNbt.getKeys()) {
        var value = entriesNbt.getCompound(key);
        result.entries.put(UUID.fromString(key), new OverworldEntriesCC.Data(
          BlockPos.fromLong(value.getLong("portal")),
          BlockPos.fromLong(value.getLong("entrance")),
          value.getFloat("rotation")
        ));
      }
      return result;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
      var portalList = new NbtList();
      portals.forEach(portal -> portalList.add(portal.createNbt()));
      nbt.put("portals", portalList);

      var entriesNbt = new NbtCompound();
      entries.forEach((owner, entry) -> {
        var value = new NbtCompound();
        value.putLong("portal", entry.posOverride.asLong());
        value.putLong("entrance", entry.entrancePos.asLong());
        value.putFloat("rotation", entry.entranceRotation);
        entriesNbt.put(owner.toString(), value);
      });
      nbt.put("entries", entriesNbt);
      return nbt;
    }
  }
}
