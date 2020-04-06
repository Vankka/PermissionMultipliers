package dev.vankka.xpmultipliers;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class XPMultipliers extends JavaPlugin implements Listener {

    private List<XPMultiplier> multipliers = new ArrayList<>();
    private YamlConfiguration configuration;
    private Map<UUID, Double> activeMultipliers = new HashMap<>();

    @Override
    public void onEnable() {
        if (!reloadConfiguration()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);

        PluginCommand pluginCommand = getCommand("xpmultipliers");
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);

        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Player onlinePlayer : getServer().getOnlinePlayers()) {
                XPMultiplier multiplier = null;
                for (XPMultiplier current : multipliers) {
                    if (multiplier != null && multiplier.getPriority() >= current.getPriority()) {
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

    @Override
    public void onDisable() {
        multipliers.clear();
        configuration = null;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExpPickup(PlayerExpChangeEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        double multiplier = activeMultipliers.getOrDefault(uuid, -1.0);
        if (multiplier < 0) {
            return;
        }

        int xp = event.getAmount();
        int newXp = (int) Math.min(Math.round(xp * multiplier), Integer.MAX_VALUE);
        event.setAmount(newXp);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (reloadConfiguration()) {
                sender.sendMessage(ChatColor.GREEN + "Config reloaded");
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to reload configuration, check console for details");
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Stream.of("reload")
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private boolean reloadConfiguration() {
        try {
            if (!getDataFolder().exists()) {
                Files.createDirectory(getDataFolder().toPath());
            }

            File configurationFile = new File(getDataFolder(), "config.yaml");
            if (!configurationFile.exists()) {
                try (InputStream inputStream = getResource("config.yaml")) {
                    try (OutputStream outputStream = new FileOutputStream(configurationFile)) {
                        int bit;

                        while ((bit = inputStream.read()) != -1) {
                            outputStream.write(bit);
                        }
                    }
                }
            }

            configuration = YamlConfiguration.loadConfiguration(configurationFile);

            multipliers.clear();
            ConfigurationSection configurationSection = configuration.getConfigurationSection("Multipliers");
            for (String key : configurationSection.getKeys(false)) {
                ConfigurationSection section = configurationSection.getConfigurationSection(key);

                multipliers.add(new XPMultiplier(section.getDouble("Multiplier"), section.getString("Permission"), section.getInt("Priority")));
            }
            return true;
        } catch (IOException e) {
            getLogger().severe("Failed to reload configuration");
            e.printStackTrace();
            return false;
        }
    }
}
