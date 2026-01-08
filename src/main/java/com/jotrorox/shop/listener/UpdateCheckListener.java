package com.jotrorox.shop.listener;

import com.jotrorox.shop.util.UpdateChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public record UpdateCheckListener(Plugin plugin, UpdateChecker.VersionResult versionResult) implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("sleepyshop.admin")) return;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage("");

            player.sendMessage(
                    Component.text("SleepyShop Update Available")
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD)
            );

            player.sendMessage(
                    Component.text("Current: ", NamedTextColor.GRAY)
                            .append(Component.text(plugin.getDescription().getVersion(), NamedTextColor.RED))
                            .append(Component.text(" → Latest: ", NamedTextColor.GRAY))
                            .append(Component.text(versionResult.latestVersion(), NamedTextColor.GREEN))
            );

            player.sendMessage(
                    Component.text("Download the update on Hangar")
                            .color(NamedTextColor.AQUA)
                            .decorate(TextDecoration.UNDERLINED)
                            .decorate(TextDecoration.BOLD)
                            .clickEvent(ClickEvent.openUrl(versionResult.downloadUrl()))
            );

            player.sendMessage("");
        }, 2 * 20L);

    }
}