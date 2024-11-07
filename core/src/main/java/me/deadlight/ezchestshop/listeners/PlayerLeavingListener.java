package me.deadlight.ezchestshop.listeners;
import me.deadlight.ezchestshop.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeavingListener implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent event) throws NoSuchFieldException, IllegalAccessException {
        Utils.versionUtils.ejectConnection(event.getPlayer());
        ChatListener.chatmap.remove(event.getPlayer().getUniqueId());
        ChatListener.PRICE_MAP.remove(event.getPlayer().getUniqueId());
        Utils.enabledOutlines.remove(event.getPlayer().getUniqueId());
    }


}
