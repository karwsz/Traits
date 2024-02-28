package me.karwsz.traits;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class Configuration {

    public YamlConfiguration config = new YamlConfiguration();
    public YamlConfiguration traitsConfiguration = new YamlConfiguration();
    public YamlConfiguration playerData = new YamlConfiguration();
    public YamlConfiguration modifiersBackup = new YamlConfiguration();


    public Configuration() {
        load();
    }



    public void load() {
        createConfig(config, "config.yml");
        createConfig(traitsConfiguration, "traits.yml");
        createConfig(playerData, "playerData.yml");
        createConfig(modifiersBackup, "/backup/modifiersBackup.yml");
        setConfigDefaults();
    }

    private void setConfigDefaults() {
        YamlConfiguration defaults = new YamlConfiguration();
        ConfigurationSection messages = defaults.createSection("Messages");
        messages.set("traitReceived", "Trait received: %s");
        messages.set("traitRevoked", "Trait revoked: %s");
        for (String key : defaults.getKeys(false)) {
            if (config.get(key) == null) {
                config.set(key, defaults.get(key));
            }
        }
        saveConfig(config, "config.yml");
    }

    public static String MSG_traitReceived() {
        return Traits.instance.configuration.config.getString("Messages.traitReceived", "Please define 'Messages.traitReceived' in your config");
    }

    public static String MSG_traitRevoked() {
        return Traits.instance.configuration.config.getString("Messages.traitRevoked", "Please define 'Messages.traitReceived' in your config");
    }

    public void save() {
        saveConfig(config, "config.yml");
        saveConfig(traitsConfiguration, "traits.yml");
        saveConfig(playerData, "playerData.yml");
        saveConfig(modifiersBackup, "/backup/modifiersBackup.yml");
    }

    @SuppressWarnings("all")
    private void createConfig(YamlConfiguration config, String name) {
        File file = getFile(name);
        try {
            config.load(file);
            Traits.instance.getLogger().info("Configuration " + name + " loaded");
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        } catch (
                InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("all")
    public void saveConfig(YamlConfiguration configuration, String name) {
        File file = getFile(name);
        try {
            configuration.save(file);
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("all")
    private File getFile(String name) {
        File file = null;
        try {
            file = new File(Traits.instance.getDataFolder(), name);
            file.getParentFile().mkdirs();
            if (file.createNewFile()) {
                Traits.instance.getLogger().log(Level.INFO, "New config file created: " + name);
            }
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }
}
