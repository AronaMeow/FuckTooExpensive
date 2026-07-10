package com.arona.meow.config;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    
    private final JavaPlugin plugin;
    private int maxRepairCost;
    private boolean allowInfinityMending;
    private int infinityMendingCost;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }
    
    public void reload() {
        plugin.reloadConfig();
        maxRepairCost = Math.max(0, Math.min(39, plugin.getConfig().getInt("max-repair-cost", 39)));
        allowInfinityMending = plugin.getConfig().getBoolean("allow-infinity-mending", true);
        infinityMendingCost = Math.max(1, Math.min(39, plugin.getConfig().getInt("infinity-mending-cost", 30)));
    }
    
    public int getMaxRepairCost() {
        return maxRepairCost;
    }
    
    public boolean isAllowInfinityMending() {
        return allowInfinityMending;
    }
    public int getInfinityMendingCost() {
        return infinityMendingCost;
}
}
