package me.karwsz.traits.traits;

import me.karwsz.traits.Traits;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerTraits {


    Map<UUID, List<Trait>> playerTraits;
    //Traits that were removed from player when they were offline
    Map<UUID, List<Trait>> disableOnJoin;

    public PlayerTraits() {
        Bukkit.getPluginManager().registerEvents(new TraitsListener(), Traits.instance);
    }

    public @NotNull List<Trait> getPlayerTraits(Player player) {
        playerTraits.putIfAbsent(player.getUniqueId(), new ArrayList<>());
        return playerTraits.get(player.getUniqueId());
    }
    public @NotNull List<Trait> getPlayerTraits(UUID player) {
        playerTraits.putIfAbsent(player, new ArrayList<>());
        return playerTraits.get(player);
    }

    public void giveTrait(Player player, Trait trait) {
        List<Trait> playerTraits = getPlayerTraits(player);
        if (!playerTraits.contains(trait)) {
            playerTraits.add(trait);
            trait.enable(player);
        }
    }

    public void giveTrait(UUID player, Trait trait) {
        List<Trait> playerTraits = getPlayerTraits(player);
        if (!playerTraits.contains(trait)) {
            playerTraits.add(trait);
            OfflinePlayer offlineP;
            if ((offlineP = Bukkit.getOfflinePlayer(player)).isOnline()) {
                trait.enable(offlineP.getPlayer());
            }
        }
    }

    public void removeTrait(Player player, Trait trait) {
        List<Trait> playerTraits = getPlayerTraits(player);
        playerTraits.remove(trait);
        trait.disable(player);
    }

    public void removeTrait(UUID player, Trait trait) {
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            removeTrait(p, trait);
            return;
        }
        List<Trait> playerTraits = getPlayerTraits(player);
        playerTraits.remove(trait);
        disableOnJoin.putIfAbsent(player, new ArrayList<>());
        getDisableOnJoinTraits(player).add(trait);
    }

    public void purgeTraits(Player player) {
        for (Trait playerTrait : getPlayerTraits(player)) {
            playerTrait.disable(player);
        }
        playerTraits.put(player.getUniqueId(), new ArrayList<>());
    }



    public List<Trait> getDisableOnJoinTraits(UUID uuid) {
        return disableOnJoin.getOrDefault(uuid, null);
    }

    public void purgeTraits(UUID player) {
        Player p = Bukkit.getPlayer(player);
        if (p != null) {
            purgeTraits(p);
            return;
        }
        List<Trait> localTraits = getPlayerTraits(player);
        if (playerTraits.isEmpty()) return;
        disableOnJoin.putIfAbsent(player, new ArrayList<>());
        List<Trait> disableOnJoin = getDisableOnJoinTraits(player);
        disableOnJoin.addAll(localTraits);
        playerTraits.put(player, new ArrayList<>());
    }


    public void applyAll(Player player) {
        for (Trait playerTrait : getPlayerTraits(player)) {
            playerTrait.enable(player);
        }
    }

    public void disableAll(Player player) {
        for (Trait playerTrait : getPlayerTraits(player)) {
            playerTrait.disable(player);
        }
    }




    private HashMap<String, List<String>>  serializePlayerTraits(Map<UUID, List<Trait>> playerTraits) {
        HashMap<String, List<String>> serialized = new HashMap<>();
        for (Map.Entry<UUID, List<Trait>> entry : playerTraits.entrySet()) {
            serialized.put(entry.getKey().toString(), entry.getValue().stream().map(Trait::getId).collect(Collectors.toList()));
        }
        return serialized;
    }

    private Map<UUID, List<Trait>> deserializePlayerTraits(Map<String, List<String>> serialized) {
        HashMap<UUID, List<Trait>> playerTraits = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : serialized.entrySet()) {
            playerTraits.put(UUID.fromString(entry.getKey()), entry.getValue().stream().map(id -> Traits.instance.traitsManager.findTraitById(id)).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return playerTraits;
    }


    @SuppressWarnings("unchecked")
    public void load(YamlConfiguration playerData) {
        Map<String, List<String>> serializedPlayerTraits = new HashMap<>();
        for (String key : playerData.createSection("playerTraits").getKeys(false)) {
            serializedPlayerTraits.put(key, (List<String>) playerData.getList("playerTraits." + key));
        }
        this.playerTraits = deserializePlayerTraits(serializedPlayerTraits);
        serializedPlayerTraits.clear();
        for (String key : playerData.createSection("disableOnJoin").getKeys(false)) {
            serializedPlayerTraits.put(key, (List<String>) playerData.getList("disableOnJoin." + key));
        }
        this.disableOnJoin = deserializePlayerTraits(serializedPlayerTraits);
    }

    public void save(YamlConfiguration playerData) {
        for (Map.Entry<String, List<String>> data : serializePlayerTraits(playerTraits).entrySet()) {
            playerData.set("playerTraits." + data.getKey(), data.getValue());
        }
        for (Map.Entry<String, List<String>> data : serializePlayerTraits(disableOnJoin).entrySet()) {
            playerData.set("disableOnJoin." + data.getKey(), data.getValue());
        }
    }

    public ArrayList<UUID> getPlayersWith(Trait trait) {
        ArrayList<UUID> players = new ArrayList<>();
        for (Map.Entry<UUID, List<Trait>> entry : playerTraits.entrySet()) {
            if (entry.getValue().contains(trait)) {
                players.add(entry.getKey());
            }
        }
        return players;
    }

    public void disableRemoved(Player player) {
        List<Trait> disableOnJoin = getDisableOnJoinTraits(player.getUniqueId());
        if (disableOnJoin == null) return;
        for (Trait trait : disableOnJoin) {
            trait.disable(player);
        }
        this.disableOnJoin.remove(player.getUniqueId());
    }


}
