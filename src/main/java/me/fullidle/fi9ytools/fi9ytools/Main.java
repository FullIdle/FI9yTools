package me.fullidle.fi9ytools.fi9ytools;

import me.fullidle.fi9ytools.fi9ytools.data.FI9yData;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        reloadConfig();
        PluginCommand command = getCommand("fi9ytools");
        CMD cmd = new CMD();
        command.setExecutor(cmd);
        command.setExecutor(cmd);
        getLogger().info("§a插载了~懂?");
    }

    @Override
    public void reloadConfig() {
        saveDefaultConfig();
        super.reloadConfig();
        FI9yData.init(this);
    }
}
