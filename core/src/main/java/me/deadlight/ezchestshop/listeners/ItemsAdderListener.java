package me.deadlight.ezchestshop.listeners;

import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import me.deadlight.ezchestshop.data.gui.GuiData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nonnull;

public class ItemsAdderListener implements Listener {

    // the update checker.
    private final UpdateChecker checker;

    public ItemsAdderListener(@Nonnull UpdateChecker checker) {
        this.checker = checker;
    }

    @EventHandler
    public void onItemsAdderLoad(ItemsAdderLoadDataEvent event) {
        if(event.getCause() != ItemsAdderLoadDataEvent.Cause.FIRST_LOAD)
            return;
        // load gui data.
        GuiData.loadGuiData();
        // check.
        checker.check();
    }
}
