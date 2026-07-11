//我什么都不知道
//我只是在吃AI的软饭
package com.arona.meow.listener;

import com.arona.meow.config.ConfigManager;
import org.bukkit.Material;
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
        AnvilView view = (AnvilView) event.getView();
        ItemStack left = inv.getItem(0);
        ItemStack right = inv.getItem(1);

        if (left == null || right == null) return;

        boolean leftIsBook = left.getType() == Material.ENCHANTED_BOOK;
        boolean rightIsBook = right.getType() == Material.ENCHANTED_BOOK;

        // ========== 无限+经验修补共存处理 ==========
        if (config.isAllowInfinityMending()) {
            boolean hasMending = hasEnchant(left, Enchantment.MENDING) || hasEnchant(right, Enchantment.MENDING);
            boolean hasInfinity = hasEnchant(left, Enchantment.INFINITY) || hasEnchant(right, Enchantment.INFINITY);

            if (hasMending && hasInfinity) {
                boolean leftIsBow = left.getType() == Material.BOW;
                boolean rightIsBow = right.getType() == Material.BOW;

                // 必须有弓，且不能是书+书
                if ((leftIsBow || rightIsBow) && !(leftIsBook && rightIsBook)) {
                    ItemStack bow = leftIsBow ? left : right;
                    ItemStack other = leftIsBow ? right : left;

                    // 检查 other 上的所有附魔是否都能附魔到弓上
                    if (allEnchantsCanApply(other, bow)) {
                        ItemStack result = bow.clone();
                        ItemMeta meta = result.getItemMeta();

                        mergeAllEnchants(meta, bow, result);
                        mergeAllEnchants(meta, other, result);

                        ensureEnchant(meta, Enchantment.INFINITY,
                                Math.max(getLevel(bow, Enchantment.INFINITY), getLevel(other, Enchantment.INFINITY)), result);
                        ensureEnchant(meta, Enchantment.MENDING,
                                Math.max(getLevel(bow, Enchantment.MENDING), getLevel(other, Enchantment.MENDING)), result);

                        result.setItemMeta(meta);
                        event.setResult(result);

                        int displayCost = config.getInfinityMendingCost();
                        view.setRepairCost(displayCost);
                        view.setMaximumRepairCost(Integer.MAX_VALUE);

                        return;
                    }
                }
            }
        }

        // ========== 普通情况：只处理过于昂贵 ==========
        view.setMaximumRepairCost(Integer.MAX_VALUE);

        int displayCost = Math.min(view.getRepairCost(), config.getMaxRepairCost());
        if (displayCost <= 0) displayCost = 1;
        view.setRepairCost(displayCost);
    }

    
    // ========== 工具方法 ==========
    
    private boolean hasEnchant(ItemStack item, Enchantment enchant) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return storageMeta.hasStoredEnchant(enchant);
        }
        return meta.hasEnchant(enchant);
    }
    
    private int getLevel(ItemStack item, Enchantment enchant) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return storageMeta.getStoredEnchantLevel(enchant);
        }
        return meta.getEnchantLevel(enchant);
    }

    private void mergeAllEnchants(ItemMeta resultMeta, ItemStack source, ItemStack resultItem) {
        if (source == null || !source.hasItemMeta()) return;

        Map<Enchantment, Integer> enchants;
        ItemMeta sourceMeta = source.getItemMeta();
        if (sourceMeta instanceof EnchantmentStorageMeta storageMeta) {
            enchants = storageMeta.getStoredEnchants();
        } else {
            enchants = sourceMeta.getEnchants();
        }

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

            int currentLevel = 0;
            if (resultMeta instanceof EnchantmentStorageMeta storageMeta) {
                currentLevel = storageMeta.getStoredEnchantLevel(enchant);
            } else {
                currentLevel = resultMeta.getEnchantLevel(enchant);
            }

            int finalLevel;
            if (currentLevel == level) {
                finalLevel = Math.min(level + 1, enchant.getMaxLevel());
            } else {
                finalLevel = Math.max(level, currentLevel);
            }

            if (resultMeta instanceof EnchantmentStorageMeta storageMeta) {
                storageMeta.addStoredEnchant(enchant, finalLevel, true);
            } else {
                resultMeta.addEnchant(enchant, finalLevel, true);
            }
        }
    }

    private void ensureEnchant(ItemMeta meta, Enchantment enchant, int level, ItemStack resultItem) {
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
    
    private boolean isConflict(ItemMeta resultMeta, Enchantment newEnchant) {
        // 无限和经验修补不冲突
        if (newEnchant == Enchantment.INFINITY && hasEnchantInMeta(resultMeta, Enchantment.MENDING)) return false;
        if (newEnchant == Enchantment.MENDING && hasEnchantInMeta(resultMeta, Enchantment.INFINITY)) return false;
        
        // 检查其他冲突
        if (resultMeta instanceof EnchantmentStorageMeta storageMeta) {
            for (Enchantment e : storageMeta.getStoredEnchants().keySet()) {
                if (newEnchant.conflictsWith(e)) return true;
            }
        } else {
            for (Enchantment e : resultMeta.getEnchants().keySet()) {
                if (newEnchant.conflictsWith(e)) return true;
            }
        }
        return false;
    }
    
    private boolean hasEnchantInMeta(ItemMeta meta, Enchantment enchant) {
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return storageMeta.hasStoredEnchant(enchant);
        }
        return meta.hasEnchant(enchant);
    }
    
    private void ensureEnchant(ItemMeta meta, Enchantment enchant, int level) {
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
    
    private int getMultiplier(Enchantment enchant, boolean isBook) {
        String key = enchant.getKey().getKey();
        int base = switch (key) {
            case "protection", "sharpness", "efficiency", "power", "loyalty", "piercing" -> 1;
            case "fire_protection", "feather_falling", "projectile_protection", "smite",
                 "bane_of_arthropods", "knockback", "unbreaking", "impaling", "quick_charge" -> 2;
            case "blast_protection", "thorns", "respiration", "depth_strider", "aqua_affinity",
                 "fire_aspect", "looting", "fortune", "luck_of_the_sea", "lure", "frost_walker",
                 "mending", "riptide", "multishot", "sweeping_edge" -> 4;
            case "silk_touch", "infinity", "channeling", "swift_sneak", "soul_speed",
                 "binding_curse", "vanishing_curse" -> 8;
            default -> 2;
        };
        return isBook ? Math.max(1, base / 2) : base;
    }

    private boolean allEnchantsCanApply(ItemStack source, ItemStack target) {
        if (source == null || !source.hasItemMeta()) return true;

        Map<Enchantment, Integer> enchants;
        ItemMeta meta = source.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            enchants = storageMeta.getStoredEnchants();
        } else {
            enchants = meta.getEnchants();
        }

        for (Enchantment enchant : enchants.keySet()) {
            if (!enchant.canEnchantItem(target)) {
                return false;
            }
        }
        return true;
    }
    
    private int calculateRealCost(ItemStack left, ItemStack right) {
        int cost = 0;
        boolean rightIsBook = right.getType() == Material.ENCHANTED_BOOK;
        
        Map<Enchantment, Integer> rightEnchants;
        ItemMeta rightMeta = right.getItemMeta();
        if (rightMeta instanceof EnchantmentStorageMeta storageMeta) {
            rightEnchants = storageMeta.getStoredEnchants();
        } else {
            rightEnchants = rightMeta.getEnchants();
        }
        
        Map<Enchantment, Integer> leftEnchants;
        ItemMeta leftMeta = left.getItemMeta();
        if (leftMeta instanceof EnchantmentStorageMeta storageMeta) {
            leftEnchants = storageMeta.getStoredEnchants();
        } else {
            leftEnchants = leftMeta.getEnchants();
        }
        
        for (var entry : rightEnchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();
            
            // 检查是否可附魔到目标物品
            if (!enchant.canEnchantItem(left) && left.getType() != Material.ENCHANTED_BOOK) {
                continue;
            }
            
            // 冲突检查
            boolean conflict = leftEnchants.keySet().stream().anyMatch(e -> {
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
                int targetLevel = leftEnchants.getOrDefault(enchant, 0);
                if (targetLevel == level) {
                    cost += multiplier * Math.min(level + 1, enchant.getMaxLevel());
                } else {
                    cost += multiplier * Math.max(level, targetLevel);
                }
            }
        }
        
        // 惩罚成本
        cost += getPenalty(left) + getPenalty(right);
        
        // 重命名成本
        // 简化处理：如果有重命名 +1
        
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
}
