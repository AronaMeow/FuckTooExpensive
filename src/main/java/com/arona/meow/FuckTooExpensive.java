package com.arona.meow;

import com.arona.meow.config.ConfigManager;
import com.arona.meow.listener.AnvilListener;
import org.bukkit.plugin.java.JavaPlugin;

public class FuckTooExpensive extends JavaPlugin {
    
    private static FuckTooExpensive instance;
    private ConfigManager configmanager;
    
    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configManager = new ConfigManager(new);
        getServer.getPluginManager().registerEvents(new AnvilListener(configManager),this)
      
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
