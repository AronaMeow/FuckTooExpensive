package com.arona.meow.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.Collections;
import java.util.Map;

/**
 * 附魔相关通用工具类
 * 封装对 ItemStack / ItemMeta / EnchantmentStorageMeta 的常用操作
 */
public final class EnchantmentUtil {

    private EnchantmentUtil() {
        // 工具类禁止实例化
    }

    // ========== 查询 ==========

    /**
     * 检查物品是否包含指定附魔（支持普通物品和附魔书）
     */
    public static boolean hasEnchant(ItemStack item, Enchantment enchant) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return storageMeta.hasStoredEnchant(enchant);
        }
        return meta.hasEnchant(enchant);
    }

    /**
     * 获取物品上指定附魔的等级（支持普通物品和附魔书）
     */
    public static int getLevel(ItemStack item, Enchantment enchant) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return storageMeta.getStoredEnchantLevel(enchant);
        }
        return meta.getEnchantLevel(enchant);
    }

    /**
     * 检查 ItemMeta 是否包含指定附魔（支持普通物品和附魔书）
     */
    public static boolean hasEnchantInMeta(ItemMeta meta, Enchantment enchant) {
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return storageMeta.hasStoredEnchant(enchant);
        }
        return meta.hasEnchant(enchant);
    }

    /**
     * 获取物品上的所有附魔（支持普通物品和附魔书）
     * 返回不可修改的 Map
     */
    public static Map<Enchantment, Integer> getEnchants(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return Collections.emptyMap();
        }
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return Collections.unmodifiableMap(storageMeta.getStoredEnchants());
        }
        return Collections.unmodifiableMap(meta.getEnchants());
    }

    /**
     * 从 ItemMeta 获取所有附魔（支持普通物品和附魔书）
     */
    public static Map<Enchantment, Integer> getEnchantsFromMeta(ItemMeta meta) {
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return Collections.unmodifiableMap(storageMeta.getStoredEnchants());
        }
        return Collections.unmodifiableMap(meta.getEnchants());
    }

    // ========== 修改 ==========

    /**
     * 确保 ItemMeta 包含指定附魔（如果不存在则添加）
     * 会先检查 canEnchantItem
     */
    public static void ensureEnchant(ItemMeta meta, Enchantment enchant, int level, ItemStack resultItem) {
        if (!enchant.canEnchantItem(resultItem)) return;

        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            if (!storageMeta.hasStoredEnchant(enchant)) {
                storageMeta.addStoredEnchant(enchant, Math.max(1, level), true);
            }
        } else {
            if (!meta.hasEnchant(enchant)) {
                meta.addEnchant(enchant, Math.max(1, level), true);
            }
        }
    }

    /**
     * 向 ItemMeta 添加/升级附魔（不检查 canEnchantItem，调用方自行保证）
     */
    public static void addEnchant(ItemMeta meta, Enchantment enchant, int level) {
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.addStoredEnchant(enchant, level, true);
        } else {
            meta.addEnchant(enchant, level, true);
        }
    }

    // ========== 冲突检测 ==========

    /**
     * 检查新附魔是否与 ItemMeta 上已有附魔冲突
     * 特殊处理：无限和经验修补不冲突
     */
    public static boolean isConflict(ItemMeta resultMeta, Enchantment newEnchant) {
        // 无限和经验修补不冲突
        if (newEnchant == Enchantment.INFINITY && hasEnchantInMeta(resultMeta, Enchantment.MENDING)) {
            return false;
        }
        if (newEnchant == Enchantment.MENDING && hasEnchantInMeta(resultMeta, Enchantment.INFINITY)) {
            return false;
        }

        Map<Enchantment, Integer> existing = getEnchantsFromMeta(resultMeta);
        for (Enchantment e : existing.keySet()) {
            if (newEnchant.conflictsWith(e)) {
                return true;
            }
        }
        return false;
    }

    // ========== 合并 ==========

    /**
     * 将 source 物品上的所有附魔合并到 resultMeta（对应 resultItem）上
     * 遵循原版规则：同级合并+1，不同级取高
     */
    public static void mergeAllEnchants(ItemMeta resultMeta, ItemStack source, ItemStack resultItem) {
        if (source == null || !source.hasItemMeta()) return;

        Map<Enchantment, Integer> enchants = getEnchants(source);

        for (var entry : enchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();

            // 检查附魔是否能应用到结果物品上
            if (!enchant.canEnchantItem(resultItem)) {
                continue;
            }

            if (isConflict(resultMeta, enchant)) {
                continue;
            }

            int currentLevel = getLevelFromMeta(resultMeta, enchant);

            int finalLevel;
            if (currentLevel == level) {
                finalLevel = Math.min(level + 1, enchant.getMaxLevel());
            } else {
                finalLevel = Math.max(level, currentLevel);
            }

            addEnchant(resultMeta, enchant, finalLevel);
        }
    }

    private static int getLevelFromMeta(ItemMeta meta, Enchantment enchant) {
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return storageMeta.getStoredEnchantLevel(enchant);
        }
        return meta.getEnchantLevel(enchant);
    }

    // ========== 应用性检查 ==========

    /**
     * 检查 source 上的所有附魔是否都能应用到 target 上
     */
    public static boolean allEnchantsCanApply(ItemStack source, ItemStack target) {
        if (source == null || !source.hasItemMeta()) return true;

        for (Enchantment enchant : getEnchants(source).keySet()) {
            if (!enchant.canEnchantItem(target)) {
                return false;
            }
        }
        return true;
    }

    // ========== 铁砧惩罚 ==========

    /**
     * 获取物品的铁砧使用惩罚（Prior Work Penalty）
     */
    public static int getPenalty(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Repairable repairable) {
            return (int) Math.pow(2, repairable.getRepairCost()) - 1;
        }
        return 0;
    }
}
