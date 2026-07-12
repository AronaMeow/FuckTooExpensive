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
    /**
     * 获取指定附魔的基础倍率
     * @param enchantKey 附魔的命名空间ID（如 "protection", "sharpness"）
     * @return 配置的值
     */
    public int getEnchantmentMultiplier(String enchantKey) {
        String path = "enchantment-multipliers." + enchantKey;
        return plugin.getConfig().getInt(path, 2);
    }
}
