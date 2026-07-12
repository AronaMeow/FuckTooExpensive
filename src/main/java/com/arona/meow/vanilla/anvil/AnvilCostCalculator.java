package com.arona.meow.vanilla.anvil;

import com.arona.meow.util.EnchantmentUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import java.util.Map;

/**
 * 铁砧成本计算器
 * 计算两个物品在铁砧中合并时的实际经验成本
 */
public class AnvilCostCalculator {

    private final EnchantmentMultiplier multiplierProvider;

    public AnvilCostCalculator(EnchantmentMultiplier multiplierProvider) {
        this.multiplierProvider = multiplierProvider;
    }

    /*
     * 计算将 right 的附魔合并到 left 上的实际成本
     * @param left 目标物品（会被修改的那个）
     * @param right 来源物品（提供附魔的那个）
     * @return 经验等级成本
     **/
    /*
    public int calculateCost(ItemStack left, ItemStack right) {
        if (left == null || right == null) return 0;

        int cost = 0;
        boolean rightIsBook = right.getType() == Material.ENCHANTED_BOOK;

        Map<Enchantment, Integer> rightEnchants = EnchantmentUtil.getEnchants(right);
        Map<Enchantment, Integer> leftEnchants = EnchantmentUtil.getEnchants(left);

        for (var entry : rightEnchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();

            // 检查是否可附魔到目标物品
            if (!enchant.canEnchantItem(left) && left.getType() != Material.ENCHANTED_BOOK) {
                continue;
            }

            // 冲突检查（含无限+经验修补特殊处理）
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
                int multiplier = multiplierProvider.getMultiplier(enchant, rightIsBook);
                int targetLevel = leftEnchants.getOrDefault(enchant, 0);

                if (targetLevel == level) {
                    cost += multiplier * Math.min(level + 1, enchant.getMaxLevel());
                } else {
                    cost += multiplier * Math.max(level, targetLevel);
                }
            }
        }

        // 惩罚成本
        cost += EnchantmentUtil.getPenalty(left) + EnchantmentUtil.getPenalty(right);

        return cost;
    }*/
}
