package com.arona.meow.listener;

import com.arona.meow.config.ConfigManager;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilListener implements Listener {
    
    private final ConfigManager config;
    
    public AnvilListener(ConfigManager config) {
        this.config = config;
    }
    
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack result = event.getResult();
        
        if (result == null) return;
        
        //如果高于配置文件里面的最大消耗，就把代价设置成配置文件里面的
        int maxCost = config.getMaxRepairCost();
        if (inv.getRepairCost() > maxCost) {
            inv.setRepairCost(maxCost);
        }
        
        // 无限和经验修补共存
        if (config.isAllowInfinityMending()) {
            ItemMeta meta = result.getItemMeta();
            if (meta != null && meta.hasEnchant(Enchantment.INFINITY) && meta.hasEnchant(Enchantment.MENDING)) {
                event.setResult(result);
            }
        }
    }
}
