package dev.vankka.permissionmultipliers.listener;

import dev.vankka.permissionmultipliers.PermissionMultipliers;
import dev.vankka.permissionmultipliers.multiplier.Multiplier;
import me.swanis.mobcoins.events.MobCoinsReceiveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SuperMobCoinListener implements Listener {

    private final PermissionMultipliers plugin;
    private Map<UUID, Double> activeMultipliers = new HashMap<>();

    public SuperMobCoinListener(PermissionMultipliers plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (isDisabled()) {
                return;
            }

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                Multiplier multiplier = null;
                for (Multiplier current : plugin.getXpMultipliers()) {
                    if (multiplier != null && multiplier.getMultiplier() >= current.getMultiplier()) {
                        continue;
                    }

                    if (onlinePlayer.hasPermission(current.getPermission())) {
                        multiplier = current;
                    }
                }
                if (multiplier != null) {
                    activeMultipliers.put(onlinePlayer.getUniqueId(), multiplier.getMultiplier());
                } else {
                    activeMultipliers.remove(onlinePlayer.getUniqueId());
                }
            }
        }, 40L, 40L);
    }

    private boolean isDisabled() {
        return !plugin.getConfig().getBoolean("EnableSuperMobCoinMultipliers");
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMobCoinsReceive(MobCoinsReceiveEvent event) {
        if (isDisabled()) {
            return;
        }

        UUID uuid = event.getProfile().getPlayer().getUniqueId();
        double multiplier = activeMultipliers.getOrDefault(uuid, -1.0);
        if (multiplier < 0) {
            return;
        }

        int xp = event.getAmount();
        int newXp = (int) Math.min(Math.round(xp * multiplier), Integer.MAX_VALUE);
        event.getProfile().setMobCoins(newXp);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        activeMultipliers.remove(event.getPlayer().getUniqueId());
    }
}