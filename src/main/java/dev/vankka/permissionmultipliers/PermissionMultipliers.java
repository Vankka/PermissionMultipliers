package dev.vankka.permissionmultipliers;

import dev.vankka.permissionmultipliers.listener.SuperMobCoinListener;
import dev.vankka.permissionmultipliers.listener.XPListener;
import dev.vankka.permissionmultipliers.multiplier.Multiplier;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PermissionMultipliers extends JavaPlugin {

    private List<Multiplier> xpMultipliers = new ArrayList<>();
    private List<Multiplier> superMobCoinMultipliers = new ArrayList<>();
    private YamlConfiguration configuration;

    @Override
    public void onEnable() {
        if (!reloadConfiguration()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new XPListener(this), this);
        if (getServer().getPluginManager().isPluginEnabled("SuperMobCoins")) {
            getServer().getPluginManager().registerEvents(new SuperMobCoinListener(this), this);
        }

        PluginCommand pluginCommand = getCommand("xpmultipliers");
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        xpMultipliers.clear();
        configuration = null;
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

    @Override
    public YamlConfiguration getConfig() {
        return configuration;
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

            xpMultipliers.clear();
            ConfigurationSection xpConfigurationSection = configuration.getConfigurationSection("XPMultipliers");
            for (String key : xpConfigurationSection.getKeys(false)) {
                ConfigurationSection section = xpConfigurationSection.getConfigurationSection(key);

                xpMultipliers.add(new Multiplier(section.getDouble("Multiplier"), section.getString("Permission")));
            }

            superMobCoinMultipliers.clear();
            ConfigurationSection superMobCoinConfigurationSection = configuration.getConfigurationSection("SuperMobCoinMultipliers");
            for (String key : superMobCoinConfigurationSection.getKeys(false)) {
                ConfigurationSection section = superMobCoinConfigurationSection.getConfigurationSection(key);

                superMobCoinMultipliers.add(new Multiplier(section.getDouble("Multiplier"), section.getString("Permission")));
            }
            return true;
        } catch (IOException e) {
            getLogger().severe("Failed to reload configuration");
            e.printStackTrace();
            return false;
        }
    }

    public List<Multiplier> getXpMultipliers() {
        return xpMultipliers;
    }

    public List<Multiplier> getSuperMobCoinMultipliers() {
        return superMobCoinMultipliers;
    }
}
