package com.arona.meow;

import com.arona.meow.config.ConfigManager;
import com.arona.meow.listener.AnvilListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class FuckTooExpensive extends JavaPlugin {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        configManager = new ConfigManager(this);

        getServer().getPluginManager().registerEvents(new AnvilListener(configManager), this);

        getLogger().info("FuckTooExpensive 已启用");
    }

    @Override
    public void onDisable() {
        getLogger().info("FuckTooExpensive 已禁用");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("fucktooexpensive")) {
            return false;
        }

        if (!sender.hasPermission("fucktooexpensive.reload")) {
            sender.sendMessage("§c你没有权限使用此命令");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§eFuckTooExpensive §7- 用法: §e/fucktooexpensive reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            configManager.reload();

            sender.sendMessage("§aFuckTooExpensive 配置已重载");
            getLogger().info(sender.getName() + " 重载了配置");
            return true;
        }

        sender.sendMessage("§c未知参数: §e" + args[0]);
        return true;
    }
}