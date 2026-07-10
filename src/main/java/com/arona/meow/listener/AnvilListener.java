//我什么都不知道
//我只是在吃AI的软饭
package com.arona.meow.listener;

import com.arona.meow.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.scheduler.BukkitRunnable;

public class AnvilListener implements Listener {

    private final ConfigManager config;
    private final org.bukkit.plugin.Plugin plugin;

    public AnvilListener(ConfigManager config, org.bukkit.plugin.Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        AnvilView view = (AnvilView) event.getView();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        // ========== 处理无限+经验修补冲突 ==========
        if (config.isAllowInfinityMending()) {
            boolean hasInfinity = hasEnchant(first, Enchantment.INFINITY)
                    || hasEnchant(second, Enchantment.INFINITY);
            boolean hasMending = hasEnchant(first, Enchantment.MENDING)
                    || hasEnchant(second, Enchantment.MENDING);

            if (hasInfinity && hasMending) {
                // 系统会拒绝合并，手动构造结果
                ItemStack result = first.clone();
                ItemMeta meta = result.getItemMeta();

                // 合并 second 的附魔
                if (second != null && second.hasItemMeta() && second.getItemMeta().hasEnchants()) {
                    for (var entry : second.getItemMeta().getEnchants().entrySet()) {
                        meta.addEnchant(entry.getKey(), entry.getValue(), true);
                    }
                }

                // 确保无限和经验修补都在
                meta.addEnchant(Enchantment.INFINITY,
                        Math.max(getLevel(first, Enchantment.INFINITY), getLevel(second, Enchantment.INFINITY)), true);
                meta.addEnchant(Enchantment.MENDING,
                        Math.max(getLevel(first, Enchantment.MENDING), getLevel(second, Enchantment.MENDING)), true);

                result.setItemMeta(meta);
                event.setResult(result);

                // 强制设成本，确保自定义配方启用
                view.setRepairCost(5);
                view.setMaximumRepairCost(Integer.MAX_VALUE);

                // 延迟再设一次，防止被覆盖
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (inv.getItem(0) != null && inv.getItem(1) != null) {
                            view.setRepairCost(5);
                            // 重新设 result
                            ItemStack currentResult = event.getResult();
                            if (currentResult == null || !currentResult.hasItemMeta()
                                    || !currentResult.getItemMeta().hasEnchant(Enchantment.MENDING)) {
                                event.setResult(result);
                            }
                        }
                    }
                }.runTaskLater(plugin, 1L);

                return; // 跳过后面的成本处理
            }
        }

        // ========== 处理过于昂贵 ==========
        view.setMaximumRepairCost(Integer.MAX_VALUE);

        int displayCost = Math.min(view.getRepairCost(), config.getMaxRepairCost());
        if (displayCost <= 0) {
            displayCost = 1;
        }
        view.setRepairCost(displayCost);
    }

    private boolean hasEnchant(ItemStack item, Enchantment enchant) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasEnchant(enchant);
    }

    private int getLevel(ItemStack item, Enchantment enchant) {
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasEnchant(enchant)) return 0;
        return item.getItemMeta().getEnchantLevel(enchant);
    }
}