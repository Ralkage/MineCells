package com.github.mim1q.minecells.registry;

import com.github.mim1q.minecells.MineCells;
import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.item.Items;
import net.minecraft.util.Util;

/** Creative-tab buttons can only be constructed on a physical client. */
public final class MineCellsItemGroupClient {
  private MineCellsItemGroupClient() { }

  public static void addLinkButtons(OwoItemGroup group) {
    var tabs = MineCells.createId("textures/gui/tabs.png");
    group.addButton(linkButton(group, Icon.of(Items.BOOK), "wiki", "https://mim1q.dev/minecells", tabs));
    group.addButton(linkButton(group, Icon.of(MineCells.createId("textures/gui/button/discord.png"), 0, 0, 16, 16), "discord", "https://discord.gg/6TjQbSjbuB", tabs));
    group.addButton(linkButton(group, Icon.of(MineCells.createId("textures/gui/button/kofi.png"), 0, 0, 16, 16), "kofi", "https://ko-fi.com/mim1q", tabs));
  }

  private static ItemGroupButton linkButton(OwoItemGroup group, Icon icon, String name, String url, net.minecraft.util.Identifier tabs) {
    return new ItemGroupButton(group, icon, name, tabs, () -> {
      var client = MinecraftClient.getInstance();
      var screen = client.currentScreen;
      client.setScreen(new ConfirmLinkScreen(confirmed -> {
        if (confirmed) Util.getOperatingSystem().open(url);
        client.setScreen(screen);
      }, url, true));
    });
  }
}
