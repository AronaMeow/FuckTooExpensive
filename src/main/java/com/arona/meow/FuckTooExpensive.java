package com.arona.meow;

import org.bukkit.plugin.java.JavaPlugin;

public class FuckTooExpensive extends JavaPlugin {
    
    private static FuckTooExpensive instance;
    
    @Override
    public void onEnable() {
        instance = this;
      
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
