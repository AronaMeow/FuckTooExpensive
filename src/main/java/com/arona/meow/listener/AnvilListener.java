//我什么都不知道
//我只是在吃AI的软饭
package com.arona.meow.listener;

import com.arona.meow.config.ConfigManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

import java.util.Map;
import java.util.Objects;

public class AnvilListener implements Listener {

    private final ConfigManager config;

    public AnvilListener(ConfigManager config) {
        this.config = config;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack left = inv.getItem(0);
        ItemStack right = inv.getItem(1);

        if (left == null || right == null) return;

        boolean hasMending = getEnchants(left).containsKey(Enchantment.MENDING)
                || getEnchants(right).containsKey(Enchantment.MENDING);
        boolean hasInfinity = getEnchants(left).containsKey(Enchantment.INFINITY)
                || getEnchants(right).containsKey(Enchantment.INFINITY);

        // ========== 无限+经验修补共存 ==========
        if (config.isAllowInfinityMending() && hasMending && hasInfinity) {
            ItemStack result = left.clone();

            // 合并右边所有附魔
            getEnchants(right).forEach((enchant, level) -> {
                if (isValidEnchant(left, enchant)) {
                    // 相同等级则+1，否则取最大
                    if (Objects.equals(getEnchants(left).getOrDefault(enchant, -1), level)) {
                        level = Math.min(enchant.getMaxLevel(), level + 1);
                    }

                    ItemMeta meta = result.getItemMeta();
                    if (meta instanceof EnchantmentStorageMeta storageMeta) {
                        storageMeta.addStoredEnchant(enchant, level, true);
                        result.setItemMeta(storageMeta);
                    } else {
                        result.addUnsafeEnchantment(enchant, level);
                    }
                }
            });

            // 处理重命名
            ItemMeta meta = result.getItemMeta();
            if (meta != null) {
                String renameText = event.getView().getRenameText();
                if (renameText != null && !renameText.isEmpty()) {
                    meta.setDisplayName(renameText);
                }
                result.setItemMeta(meta);
            }

            event.setResult(result);

            // 成本：简单计算，然后压到配置值
            int cost = calculateSimpleCost(left, right);
            int displayCost = Math.min(cost, config.getMaxRepairCost());
            if (displayCost <= 0) displayCost = 1;

            AnvilView view = (AnvilView) event.getView();
            view.setRepairCost(displayCost);
            view.setMaximumRepairCost(Integer.MAX_VALUE);

            return;
        }

        // ========== 普通情况：只处理过于昂贵 ==========
        AnvilView view = (AnvilView) event.getView();
        view.setMaximumRepairCost(Integer.MAX_VALUE);

        int displayCost = Math.min(view.getRepairCost(), config.getMaxRepairCost());
        if (displayCost <= 0) displayCost = 1;
        view.setRepairCost(displayCost);
    }

    private Map<Enchantment, Integer> getEnchants(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Map.of();

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return storageMeta.getStoredEnchants();
        }
        return item.getEnchantments();
    }

    private boolean isValidEnchant(ItemStack item, Enchantment enchant) {
        return enchant.canEnchantItem(new ItemStack(item.getType()))
                || item.getType().toString().contains("BOOK");
    }

    private int calculateSimpleCost(ItemStack left, ItemStack right) {
        int cost = 0;
        boolean rightIsBook = right.getType().toString().contains("BOOK");

        for (var entry : getEnchants(right).entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();

            if (!isValidEnchant(left, enchant)) continue;

            // 冲突检查：跳过无限+经验修补
            boolean conflict = getEnchants(left).keySet().stream().anyMatch(e -> {
                if ((enchant == Enchantment.MENDING && e == Enchantment.INFINITY)
                        || (enchant == Enchantment.INFINITY && e == Enchantment.MENDING)) {
                    return false;
                }
                return enchant.conflictsWith(e);
            });

            if (conflict) {
                cost += 1;
            } else {
                int multiplier = getMultiplier(enchant, rightIsBook);
                int targetLevel = getEnchants(left).getOrDefault(enchant, 0);
                if (targetLevel == level) {
                    cost += multiplier * Math.min(level + 1, enchant.getMaxLevel());
                } else {
                    cost += multiplier * Math.max(level, targetLevel);
                }
            }
        }

        // 惩罚成本
        cost += getPenalty(left) + getPenalty(right);

        return cost;
    }

    private int getPenalty(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof org.bukkit.inventory.meta.Repairable repairable) {
            return (int) Math.pow(2, repairable.getRepairCost()) - 1;
        }
        return 0;
    }

    private int getMultiplier(Enchantment enchant, boolean isBook) {
        int base = switch (enchant.getKey().getKey()) {
            case "protection", "sharpness", "efficiency", "power", "loyalty", "piercing" -> 1;
            case "fire_protection", "feather_falling", "projectile_protection", "smite",
                 "bane_of_arthropods", "knockback", "unbreaking", "impaling", "quick_charge" -> 2;
            case "blast_protection", "thorns", "respiration", "depth_strider", "aqua_affinity",
                 "fire_aspect", "looting", "fortune", "luck_of_the_sea", "lure", "frost_walker",
                 "mending", "riptide", "multishot", "sweeping_edge" -> 4;
            case "silk_touch", "infinity", "channeling", "swift_sneak", "soul_speed",
                 "binding_curse", "vanishing_curse" -> 8;
            default -> 2; // 未知附魔默认2
        };

        return isBook ? Math.max(1, base / 2) : base;
    }
}