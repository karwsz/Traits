package me.karwsz.traits.traits;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface Trait extends ConfigurationSerializable {

    String getId();
    TextComponent getDisplayName();

    void setDisplayName(String legacy);
    void enable(Player player);
    void disable(Player player);
    void remove();
}
