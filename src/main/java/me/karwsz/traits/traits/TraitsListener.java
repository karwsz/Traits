package me.karwsz.traits.traits;

import me.karwsz.traits.Traits;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;


public class TraitsListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Traits.instance.playerTraits.applyAll(event.getPlayer());
        Traits.instance.playerTraits.disableRemoved(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Traits.instance.playerTraits.applyAll(event.getPlayer());
    }
}
