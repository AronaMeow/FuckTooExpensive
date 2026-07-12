package com.arona.meow.vanilla.anvil;

import com.arona.meow.config.ConfigManager;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

/**
 * 附魔成本倍率管理
 * 从配置中读取各附魔的倍率，支持默认分组和单独覆盖
 */
public class EnchantmentMultiplier {

    private final ConfigManager config;

    // 原版默认分组（用于配置文件中未指定时的 fallback）
    private static final Map<String, Integer> DEFAULT_GROUPS = Map.ofEntries(
        Map.entry("protection", 1),
        Map.entry("sharpness", 1),
        Map.entry("efficiency", 1),
        Map.entry("power", 1),
        Map.entry("loyalty", 1),
        Map.entry("piercing", 1),

        Map.entry("fire_protection", 2),
        Map.entry("feather_falling", 2),
        Map.entry("projectile_protection", 2),
        Map.entry("smite", 2),
        Map.entry("bane_of_arthropods", 2),
        Map.entry("knockback", 2),
        Map.entry("unbreaking", 2),
        Map.entry("impaling", 2),
        Map.entry("quick_charge", 2),

        Map.entry("blast_protection", 4),
        Map.entry("thorns", 4),
        Map.entry("respiration", 4),
        Map.entry("depth_strider", 4),
        Map.entry("aqua_affinity", 4),
        Map.entry("fire_aspect", 4),
        Map.entry("looting", 4),
        Map.entry("fortune", 4),
        Map.entry("luck_of_the_sea", 4),
        Map.entry("lure", 4),
        Map.entry("frost_walker", 4),
        Map.entry("mending", 4),
        Map.entry("riptide", 4),
        Map.entry("multishot", 4),
        Map.entry("sweeping_edge", 4),

        Map.entry("silk_touch", 8),
        Map.entry("infinity", 8),
        Map.entry("channeling", 8),
        Map.entry("swift_sneak", 8),
        Map.entry("soul_speed", 8),
        Map.entry("binding_curse", 8),
        Map.entry("vanishing_curse", 8)
    );

    public EnchantmentMultiplier(ConfigManager config) {
        this.config = config;
    }

    /**
     * 获取附魔的基础倍率
     * @param enchant 附魔
     * @return 基础倍率（书籍会在此基础上减半）
     */
    public int getBaseMultiplier(Enchantment enchant) {
        String key = enchant.getKey().getKey();

        // 优先从配置读取
        int configured = config.getEnchantmentMultiplier(key);
        if (configured != 0) {
            return configured;
        }

        // fallback 到默认分组
        return DEFAULT_GROUPS.getOrDefault(key, 2);
    }

    /**
     * 获取实际倍率（考虑是否为书籍）
     * 书籍倍率 = max(1, base / 2)
     */
    public int getMultiplier(Enchantment enchant, boolean isBook) {
        int base = getBaseMultiplier(enchant);
        return isBook ? Math.max(1, base / 2) : base;
    }
}
