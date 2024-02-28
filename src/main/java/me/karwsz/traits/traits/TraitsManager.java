package me.karwsz.traits.traits;

import me.karwsz.traits.Traits;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TraitsManager {

    private List<Trait> registeredTraits;

    public TraitsManager() {

    }

    public List<Trait> getRegisteredTraits() {
        return registeredTraits;
    }

    public Trait findTraitById(String id) {
        return registeredTraits.stream().filter(trait -> id.equalsIgnoreCase(trait.getId())).findAny().orElse(null);
    }

    public boolean isIdTaken(String id) {
        return registeredTraits.stream().anyMatch(trait -> id.equalsIgnoreCase(trait.getId()));
    }

    public boolean registerTrait(Trait trait) {
        if (isIdTaken(trait.getId())) {
            return false;
        }
        registeredTraits.add(trait);
        return true;
    }

    public void redefineTrait(Trait trait) {
        Trait old = findTraitById(trait.getId());
        if (old != null) {
            ArrayList<UUID> players = Traits.instance.playerTraits.getPlayersWith(old);
            old.remove();
            for (UUID uuid : players) {
                Traits.instance.playerTraits.giveTrait(uuid, trait);
            }
        }
        registerTrait(trait);
    }

    public void load(YamlConfiguration configuration) {
        this.registeredTraits = (List<Trait>) configuration.getList("registeredTraits", new ArrayList<>());
    }

    public void save(YamlConfiguration configuration) {
        configuration.set("registeredTraits", registeredTraits);
    }

    public List<String> getTraitIds() {
        return registeredTraits.stream().map(Trait::getId).collect(Collectors.toList());
    }

    public void unregisterTrait(Trait trait) {
        registeredTraits.remove(trait);
    }
}
