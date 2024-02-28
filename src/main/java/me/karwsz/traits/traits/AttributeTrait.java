package me.karwsz.traits.traits;

import com.mojang.brigadier.CommandDispatcher;
import me.karwsz.traits.Traits;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@SerializableAs("AttributeTrait")
public class AttributeTrait implements Trait {


    private final String id;
    private String displayName;
    private final AttributeModifier modifier;
    private final NamespacedKey attribute;
    private final UUID uuid;

    public AttributeTrait(String id, String displayName, NamespacedKey attribute, AttributeModifier modifier) {
        this.id = id;
        this.displayName = displayName;
        this.modifier = modifier;
        this.attribute = attribute;
        this.uuid = UUID.randomUUID();
        backupModifier();
    }

    private void backupModifier() {
        YamlConfiguration configuration = Traits.instance.configuration.modifiersBackup;

        //Replace dots with slashes to adapt to YAML specific syntax
        String namespaceSerialized = attribute.toString().replaceAll("\\.", "/");
        List<String> uuids = configuration.getStringList(namespaceSerialized);
        uuids.add(modifier.getUniqueId().toString());
        configuration.set(namespaceSerialized, uuids);

        //Save changes immediately
        Traits.instance.configuration.saveConfig(configuration, "/backup/modifiersBackup.yml");
    }

    public AttributeTrait(String id, String displayName, NamespacedKey attribute, AttributeModifier modifier, UUID uuid) {
        this.id = id;
        this.displayName = displayName;
        this.modifier = modifier;
        this.attribute = attribute;
        this.uuid = uuid;
    }


    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getId() {
        return id;
    }

    public TextComponent getDisplayName() {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(displayName);
    }

    private String operationToString(AttributeModifier.Operation operation) {
        switch (operation) {
            case ADD_NUMBER ->
            {
                return "add";
            }
            case ADD_SCALAR ->
            {
                return "multiply";
            }
            case MULTIPLY_SCALAR_1 ->
            {
                return "multiply_base";
            }
        }
        return null;
    }

    @SuppressWarnings("all")
    @Override
    public void enable(Player player) {
        CommandDispatcher<CommandSourceStack> dispatcher = MinecraftServer.getServer().getCommands().getDispatcher();
        CommandSourceStack sourceStack = MinecraftServer.getServer().createCommandSourceStack();
        MinecraftServer.getServer().getCommands().dispatchServerCommand(sourceStack, "minecraft:attribute " + player.getName() + " " + attribute.toString() + " modifier add " + uuid.toString() + " " + modifier.getName() + " " + modifier.getAmount() + " " + operationToString(modifier.getOperation()));
    }

    @SuppressWarnings("all")
    @Override
    public void disable(Player player) {
        CommandDispatcher<CommandSourceStack> dispatcher = MinecraftServer.getServer().getCommands().getDispatcher();
        CommandSourceStack sourceStack = MinecraftServer.getServer().createCommandSourceStack();
        MinecraftServer.getServer().getCommands().dispatchServerCommand(sourceStack, "minecraft:attribute " + player.getName() + " " + attribute.toString() + " modifier remove " + uuid.toString());
    }


    @Override
    public @NotNull Map<String, Object> serialize() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("attribute", attribute.toString());
        data.put("id", id);
        data.put("displayName", displayName);
        data.put("modifier", modifier);
        return data;
    }

    public static AttributeTrait deserialize(Map<String, Object> data) {
        NamespacedKey attribute = NamespacedKey.fromString((String) data.get("attribute"));
        return new AttributeTrait((String) data.get("id"), (String) data
                .get("displayName"),
                attribute,
                (AttributeModifier) data
                .get("modifier"), UUID.fromString((String) data.getOrDefault("uuid", UUID.randomUUID().toString())));
    }


    @Override
    public void remove() {
        for (UUID uuid : Traits.instance.playerTraits.getPlayersWith(this)) {
            Traits.instance.playerTraits.removeTrait(Bukkit.getOfflinePlayer(uuid).getPlayer(), this);
        }
        Traits.instance.traitsManager.unregisterTrait(this);
    }
}
