package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.command.*;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class MineCellsCommands {
  public static void register(RegisterCommandsEvent event) {
    CellsCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    SpawnerRuneCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    MineCellsDumpCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    MineCellsTeleportCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    SpecialPointCommand.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
  }
}
