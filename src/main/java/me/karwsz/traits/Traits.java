package me.karwsz.traits;

import dev.jorel.commandapi.*;
import me.karwsz.traits.commands.TraitsCommand;
import me.karwsz.traits.compatibility.Messages;
import me.karwsz.traits.traits.AttributeTrait;
import me.karwsz.traits.traits.PlayerTraits;
import me.karwsz.traits.traits.Trait;
import me.karwsz.traits.traits.TraitsManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public final class Traits extends JavaPlugin {
    public static Traits instance;
    public Configuration configuration;
    public TraitsManager traitsManager;
    public PlayerTraits playerTraits;

    static {
        ConfigurationSerialization.registerClass(AttributeTrait.class);
        ConfigurationSerialization.registerClass(Trait.class);
    }

    @Override
    public void onEnable() {
        Traits.instance = this;
        getLogger().log(Level.INFO, "Traits by Karwsz has been enabled");
        Messages.init();
        loadModules();
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(instance));
    }

    boolean failLoad = false;

    private void loadModules() {
        try {
            this.configuration = new Configuration();
            this.traitsManager = new TraitsManager();
            traitsManager.load(configuration.traitsConfiguration);
            this.playerTraits = new PlayerTraits();
            playerTraits.load(configuration.playerData);
        } catch (Exception e) {
            getSLF4JLogger().error("Plugin has encountered an error while loading configurations. Disabling to avoid data loss/corruption", e);
            failLoad = true;
            Bukkit.getPluginManager().disablePlugin(this);
        }

        attemptRegisterCommands();
    }

    //I have tried everything, so this is my only solution left
    //java.lang.IllegalStateException: Tried to access CommandAPIBukkit instance, but it was null! Are you using CommandAPI features before calling CommandAPI#onLoad?
    private void attemptRegisterCommands() {

        new TraitsCommand();
    }


    private void saveModules() {
        if (failLoad) return;
        this.traitsManager.save(configuration.traitsConfiguration);
        this.playerTraits.save(configuration.playerData);

        this.configuration.save();
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Traits by Karwsz has been disabled");
        saveModules();
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return configuration.config;
    }

    public static NamespacedKey parseNamespacedKey(String string) {
        if (string == null || string.equalsIgnoreCase("")) return null;
        String[] split = string.split(":");
        if (split.length < 2) return null;
        return new NamespacedKey(split[0], split[1]);
    }



}
