package com.arona.meow;

import com.arona.meow.config.ConfigManager;
import com.arona.meow.listener.AnvilListener;
import org.bukkit.plugin.java.JavaPlugin;

public class FuckTooExpensive extends JavaPlugin {
    
    private static FuckTooExpensive instance;
    private ConfigManager configManager;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        getServer().getPluginManager().registerEvents(new AnvilListener(configManager, this), this);
      
        getLogger().info("FuckTooExpensive enabled.");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("FuckTooExpensive disabled.");
    }
    
    public static FuckTooExpensive getInstance() {
        return instance;
    }
}