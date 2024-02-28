package me.karwsz.traits.commands;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestion;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import me.karwsz.traits.Configuration;
import me.karwsz.traits.Traits;
import me.karwsz.traits.compatibility.Messages;
import me.karwsz.traits.traits.AttributeTrait;
import me.karwsz.traits.traits.Trait;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TraitsCommand {

    public TraitsCommand() {

        // ============================= '/traits create' =============================
        CommandAPICommand createSubCommand = new CommandAPICommand("create")
                .executes((commandSender, commandArguments) -> {
                    String helpStr = "--- /traits create help ---\n" +
                            "/traits create attribute <id> <displayName> <attribute> <amount> <operation>\n" +
                            "/traits create special NOT IMPLEMENTED";
                    Messages.sendMessage(commandSender, Component.text(helpStr, NamedTextColor.GREEN));
                })
                .withSubcommand(createCreateAttributeSubcommand()).withPermission("traits.admin");


        // ============================= '/traits redefine' =============================
        CommandAPICommand redefineSubCommand = new CommandAPICommand("redefine")
                .withSubcommand(createRedefineAttributeSubcommand()).withPermission("traits.admin");




        String commandHelp =
        "--- /traits help ---\n" +
                "/traits - show this message\n" +
                "/traits create - help for creating traits\n" +
                "/traits redefine attribute/special - create or redefine given trait type\n" +
                "/traits remove <trait> - removes a trait\n" +
                "\n" +
                "/traits set <player> <trait> (true/false) - add or remove player trait\n" +
                "/traits purge <player> - remove all traits from player\n" +
                "/traits list <player> - get player traits";

        new CommandAPICommand("traits")
                .withHelp("Main Traits plugin command", commandHelp)
                .withFullDescription(commandHelp)
                .executes(executionInfo -> {
                    Messages.sendMessage(executionInfo.sender(), Component.text(commandHelp, NamedTextColor.GREEN));
                })
                .withSubcommand(createSubCommand)
                .withSubcommand(redefineSubCommand)
                .withSubcommand(createSetSubcommand())
                .withSubcommand(createRemoveSubcommand())
                .withSubcommand(createListSubcommand())
                .withSubcommand(createPurgeSubcommand())
                .withPermission("traits.admin")
                .register();
    }

    private CommandAPICommand createPurgeSubcommand() {
        return new CommandAPICommand("purge")
                .withArguments(new OfflinePlayerArgument("player"))
                .executes((commandSender, commandArguments) -> {
                    OfflinePlayer offlinePlayer = (OfflinePlayer) commandArguments.get("player");
                    assert offlinePlayer != null;
                    if (!offlinePlayer.hasPlayedBefore()) {
                        Messages.sendMessage(commandSender, Component.text("This player has never played before!", NamedTextColor.RED));
                        return;
                    }
                    Traits.instance.playerTraits.purgeTraits(offlinePlayer.getUniqueId());
                    Messages.sendMessage(commandSender, Component.text("Purged traits of " + offlinePlayer.getName(), NamedTextColor.GREEN));
                });
    }


    // '/traits list <player>'
    @SuppressWarnings("all")
    private CommandAPICommand createListSubcommand() {
        return new CommandAPICommand("list")
                .withArguments(new OfflinePlayerArgument("player"))
                .executes(executionInfo -> {
                    OfflinePlayer player = (OfflinePlayer) executionInfo.args().get("player");
                    if (!player.hasPlayedBefore()) {
                        Messages.sendMessage(executionInfo.sender(), Component.text("This player has never played before!", NamedTextColor.RED));
                        return;
                    }
                    UUID uuid = player.getUniqueId();
                    List<Trait> traitsList = Traits.instance.playerTraits.getPlayerTraits(uuid);
                    if (traitsList.isEmpty()) {
                        Messages.sendMessage(executionInfo.sender(), Component.text("This player has no traits", NamedTextColor.YELLOW));
                        return;
                    }
                    StringBuilder builder = new StringBuilder(ChatColor.GREEN + "Player traits:\n");
                    for (Trait trait : traitsList) {
                        builder.append("\n").append(ChatColor.YELLOW).append("- ").append(LegacyComponentSerializer.legacyAmpersand().serialize(trait.getDisplayName()))
                                .append(ChatColor.YELLOW)
                                .append(" (").append(trait.getId()).append(")");
                    }
                    Messages.sendMessage(executionInfo.sender(), LegacyComponentSerializer.legacyAmpersand().deserialize(builder.toString()));
                });
    }


    // '/traits remove <trait>'
    private static CommandAPICommand createRemoveSubcommand() {
        return new CommandAPICommand("remove")
                .withArguments(new StringArgument("trait").replaceSuggestions(ArgumentSuggestions.stringCollection(info -> Traits.instance.traitsManager.getTraitIds())))
                .executes(executionInfo -> {
                    String id = (String) executionInfo.args().get("trait");
                    if (Traits.instance.traitsManager.findTraitById(id) == null) {
                        Messages.sendMessage(executionInfo.sender(), Component.text("Trait not found: " + id, NamedTextColor.RED));
                        return;
                    }
                    Traits.instance.traitsManager.findTraitById(id).remove();
                    Messages.sendMessage(executionInfo.sender(), Component.text("Removed trait: " + id, NamedTextColor.GREEN));
                }).withPermission("traits.admin");
    }


    // '/traits set <target> (val)'
    private CommandAPICommand createSetSubcommand() {
        return new CommandAPICommand("set")
                .withArguments(new OfflinePlayerArgument("target"), traitArgument()).withOptionalArguments(new BooleanArgument("val"))
                .executes(info -> {
                    CommandSender sender = info.sender();
                    OfflinePlayer target = (OfflinePlayer) info.args().get("target");
                    Trait trait = (Trait) info.args().get("trait");
                    boolean value = (boolean) info.args().getOptional("val").orElse(true);
                    List<Trait> playerTraits = Traits.instance.playerTraits.getPlayerTraits(target.getUniqueId());
                    if (value) {
                        if (playerTraits.contains(trait)) {
                            Messages.sendMessage(sender, Component.text(String.format("Player %s already has trait '%s'", target.getName(), trait.getId()), NamedTextColor.YELLOW));
                            return;
                        }
                        Traits.instance.playerTraits.giveTrait(target.getUniqueId(), trait);
                        String[] splitMessage = Configuration.MSG_traitReceived().split("%s");
                        TextComponent.Builder comp = Component.text(splitMessage[0], NamedTextColor.YELLOW).toBuilder();
                        comp.append(trait.getDisplayName());
                        if (splitMessage.length > 1) {
                            comp.append(Component.text(splitMessage[1], NamedTextColor.YELLOW));
                        }
                        if (target.isOnline()) {
                            Messages.sendMessage(target.getPlayer(), comp.build());
                        }
                        if (!target.equals(sender)) {
                            Messages.sendMessage(sender, Component.text(String.format("Player %s has received '", target.getName()), NamedTextColor.GREEN).append(trait.getDisplayName()).append(Component.text("' trait", NamedTextColor.GREEN)));
                        }
                    } else {
                        if (!playerTraits.contains(trait)) {
                            Messages.sendMessage(sender, Component.text(String.format("Player %s doesn't have '%s' trait", target.getName(), trait.getId()), NamedTextColor.YELLOW));
                            return;
                        }
                        Traits.instance.playerTraits.removeTrait(target.getUniqueId(), trait);
                        String[] splitMessage = Configuration.MSG_traitRevoked().split("%s");
                        TextComponent.Builder comp = Component.text(splitMessage[0], NamedTextColor.YELLOW).toBuilder();
                        comp.append(trait.getDisplayName());
                        if (splitMessage.length > 1) {
                            comp.append(Component.text(splitMessage[1], NamedTextColor.YELLOW));
                        }
                        if (target.isOnline()) {
                            Messages.sendMessage(target.getPlayer(), comp.build());
                        }
                        if (!target.equals(sender)) {
                            Messages.sendMessage(sender, LegacyComponentSerializer.legacyAmpersand().deserialize(String.format(ChatColor.GREEN + "Player %s lost '%s' trait", target.getName(), LegacyComponentSerializer.legacyAmpersand().serialize(trait.getDisplayName()))));
                        }
                    }
                }).withPermission("traits.admin");
    }


    private CommandAPICommand createCreateAttributeSubcommand() {
       return new CommandAPICommand("attribute")
                .withOptionalArguments(new StringArgument("id"))
                .withOptionalArguments(new TextArgument("displayName"))
                .withOptionalArguments(new NamespacedKeyArgument("attribute").replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
                    CommandDispatcher<CommandSourceStack> dispatcher = MinecraftServer.getServer().getCommands().getDispatcher();
                    CommandSourceStack stack = MinecraftServer.getServer().createCommandSourceStack();
                    try {
                        return dispatcher.getCompletionSuggestions(dispatcher.parse("minecraft:attribute @p " + info.currentArg(), stack)).get().getList().stream().map(Suggestion::getText).collect(Collectors.toList());
                    } catch (
                            InterruptedException |
                            ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                })))
                .withOptionalArguments(new DoubleArgument("amount"))
                .withOptionalArguments(operationArgument())
                .executes(info -> {
                    CommandSender sender = info.sender();
                    String id = (String) info.args().get("id");
                    assert id != null;
                    AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), id, (double) info.args().get("amount"),
                            (AttributeModifier.Operation) info.args().get("operation"));
                    AttributeTrait trait = new AttributeTrait(id, (String) info.args().get("displayName"),
                            (NamespacedKey) info.args().get("attribute"), modifier);
                    if (!Traits.instance.traitsManager.registerTrait(trait)) {
                        Messages.sendMessage(sender, Component.text("Error: id " + id + " is already used", NamedTextColor.RED));
                        return;
                    }
                    Messages.sendMessage(sender, Component.text("Trait " + id, NamedTextColor.GREEN).append(Component.text(" created successfully", NamedTextColor.GREEN)));
                }).withPermission("traits.admin")
        ;
    }

    private CommandAPICommand createRedefineAttributeSubcommand() {
        return new CommandAPICommand("attribute")
                .withArguments(new StringArgument("id"))
                .withArguments(new TextArgument("displayName"))
                .withArguments(new NamespacedKeyArgument("attribute").replaceSuggestions(ArgumentSuggestions.stringCollection(info -> {
                    CommandDispatcher<CommandSourceStack> dispatcher = MinecraftServer.getServer().getCommands().getDispatcher();
                    CommandSourceStack stack = MinecraftServer.getServer().createCommandSourceStack();
                    try {
                        return dispatcher.getCompletionSuggestions(dispatcher.parse("minecraft:attribute @p " + info.currentArg(), stack)).get().getList().stream().map(Suggestion::getText).collect(Collectors.toList());
                    } catch (
                            InterruptedException |
                            ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                })))
                .withArguments(new DoubleArgument("amount"))
                .withArguments(operationArgument())
                .executes(info -> {
                    CommandSender sender = info.sender();
                    String id = (String) info.args().get("id");
                    assert id != null;
                    AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), id, (double) info.args().get("amount"),
                            (AttributeModifier.Operation) info.args().get("operation"));
                    AttributeTrait trait = new AttributeTrait(id, (String) info.args().get("displayName"),
                            (NamespacedKey) info.args().get("attribute"), modifier);
                    Traits.instance.traitsManager.redefineTrait(trait);
                    Messages.sendMessage(sender, Component.text("Trait " + id, NamedTextColor.GREEN).append(Component.text(" redefined successfully", NamedTextColor.GREEN)));
                }).withPermission("traits.admin")
                ;
    }




    public Argument<AttributeModifier.Operation> operationArgument() {
        return new CustomArgument<AttributeModifier.Operation, String>(new StringArgument("operation"), customArgumentInfo -> {
            String input = customArgumentInfo.input();
            switch (input.toLowerCase()) {
                case "add" -> {
                    return AttributeModifier.Operation.ADD_NUMBER;
                }
                case "multiply" -> {
                    return AttributeModifier.Operation.ADD_SCALAR;
                }
                case "multiply_base" -> {
                    return AttributeModifier.Operation.MULTIPLY_SCALAR_1;
                }
                default -> {
                    throw CustomArgument.CustomArgumentException.fromString("Unknown operation: " + input);
                }
            }
        }).replaceSuggestions(ArgumentSuggestions.strings("add", "multiply", "multiply_base"));
    }


    private Argument<Trait> traitArgument() {
        return new CustomArgument<Trait, String>(new StringArgument("trait"), info -> {
            Trait trait = Traits.instance.traitsManager.findTraitById(info.input());
            if (trait == null) {
                throw CustomArgument.CustomArgumentException.fromString("Unknown trait: " + info.input());
            }
            return trait;
        }).replaceSuggestions(ArgumentSuggestions.stringCollection(info -> Traits.instance.traitsManager.getTraitIds()));
    }
}
