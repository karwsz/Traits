package me.karwsz.traits.traits.minecraft;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.attribute.AttributeModifier;


public class AttributeTrait {


    private final String id;
    private String displayName;
    private final AttributeModifier modifier;

    public AttributeTrait(String id, String displayName, AttributeModifier modifier) {
        this.id = id;
        this.displayName = displayName;
        this.modifier = modifier;
    }

    public String getId() {
        return id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public TextComponent getDisplayName() {
        return LegacyComponentSerializer.legacy('&').deserialize(displayName);
    }

    public AttributeModifier getModifier() {
        return modifier;
    }

}
