package me.karwsz.traits.compatibility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;


/**
 * Class created for compatibility issue with Mohist
 */
public class Messages {

    static boolean useKyori = true;

    public static void init() {
        try {
            Bukkit.getConsoleSender().sendMessage(Component.text("Traits plugin is using Kyori Components for sending messages", NamedTextColor.GREEN));
        } catch (NoSuchMethodError e) {
            useKyori = false;
        }
    }

    @SuppressWarnings("all")
    public static void sendMessage(CommandSender sender, TextComponent component) {
        if (useKyori) sender.sendMessage(component);
        else sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LegacyComponentSerializer.legacyAmpersand().serialize(component)));
    }


}
