package com.arona.meow.listener;

import com.arona.meow.config.ConfigManager;
import com.arona.meow.util.EnchantmentUtil;
import com.arona.meow.vanilla.anvil.AnvilCostCalculator;
import com.arona.meow.vanilla.anvil.EnchantmentMultiplier;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;

public class AnvilListener implements Listener {

    private final ConfigManager config;

    public AnvilListener(ConfigManager config) {
        this.config = config;
        EnchantmentMultiplier multiplierProvider = new EnchantmentMultiplier(config);
        AnvilCostCalculator costCalculator = new AnvilCostCalculator(multiplierProvider);
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        AnvilView view = event.getView();
        ItemStack left = inv.getItem(0);
        ItemStack right = inv.getItem(1);

        if (left == null || right == null) return;

        boolean leftIsBook = left.getType() == Material.ENCHANTED_BOOK;
        boolean rightIsBook = right.getType() == Material.ENCHANTED_BOOK;

        // ========== 无限+经验修补共存处理 ==========
        if (config.isAllowInfinityMending()) {
            handleInfinityMending(event, view, left, right, leftIsBook, rightIsBook);
        }

        // ========== 普通情况：处理过于昂贵提示 ==========
        view.setMaximumRepairCost(Integer.MAX_VALUE);

        int displayCost = Math.min(view.getRepairCost(), config.getMaxRepairCost());
        if (displayCost <= 0) displayCost = 1;
        view.setRepairCost(displayCost);
    }

    /**
     * 处理无限+经验修补共存逻辑
     */
    private void handleInfinityMending(PrepareAnvilEvent event, AnvilView view,
                                       ItemStack left, ItemStack right,
                                       boolean leftIsBook, boolean rightIsBook) {
        boolean hasMending = EnchantmentUtil.hasEnchant(left, Enchantment.MENDING)
                || EnchantmentUtil.hasEnchant(right, Enchantment.MENDING);
        boolean hasInfinity = EnchantmentUtil.hasEnchant(left, Enchantment.INFINITY)
                || EnchantmentUtil.hasEnchant(right, Enchantment.INFINITY);

        if (!hasMending || !hasInfinity) return;

        boolean leftIsBow = left.getType() == Material.BOW;
        boolean rightIsBow = right.getType() == Material.BOW;

        // 必须有弓，且不能是书+书
        if (!(leftIsBow || rightIsBow) || (leftIsBook && rightIsBook)) return;

        ItemStack bow = leftIsBow ? left : right;
        ItemStack other = leftIsBow ? right : left;

        // 检查 other 上的所有附魔是否都能附魔到弓上
        if (!EnchantmentUtil.allEnchantsCanApply(other, bow)) return;

        ItemStack result = bow.clone();
        ItemMeta meta = result.getItemMeta();

        EnchantmentUtil.mergeAllEnchants(meta, bow, result);
        EnchantmentUtil.mergeAllEnchants(meta, other, result);

        EnchantmentUtil.ensureEnchant(meta, Enchantment.INFINITY,
                Math.max(EnchantmentUtil.getLevel(bow, Enchantment.INFINITY),
                        EnchantmentUtil.getLevel(other, Enchantment.INFINITY)), result);
        EnchantmentUtil.ensureEnchant(meta, Enchantment.MENDING,
                Math.max(EnchantmentUtil.getLevel(bow, Enchantment.MENDING),
                        EnchantmentUtil.getLevel(other, Enchantment.MENDING)), result);

        result.setItemMeta(meta);
        event.setResult(result);

        int displayCost = config.getInfinityMendingCost();
        view.setRepairCost(displayCost);
        view.setMaximumRepairCost(Integer.MAX_VALUE);
    }
}
