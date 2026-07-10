package com.arona.meow.config;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    
    private final JavaPlugin plugin;
    private int maxRepairCost;
    private boolean allowInfinityMending;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }
    
    public void reload() {
        plugin.reloadConfig();
        maxRepairCost = Math.max(0, Math.min(39, plugin.getConfig().getInt("max-repair-cost", 38)));
        allowInfinityMending = plugin.getConfig().getBoolean("allow-infinity-mending", false);
    }
    
    public int getMaxRepairCost() {
        return maxRepairCost;
    }
    
    public boolean isAllowInfinityMending() {
        return allowInfinityMending;
    }
}
