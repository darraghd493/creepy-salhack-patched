package me.ionar.salhack.module.ui;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.gui.inventory.GuiChest;

public class ChestModule extends Module {

    public boolean validGui;
    @EventHandler
    private final Listener<EventPlayerUpdate> packetEventListener = new Listener<>(event -> {
        validGui = mc.currentScreen instanceof GuiChest;

    });

    public ChestModule() {
        super("Chest", new String[]{"Chest"}, "Display chest stealer button in chests.", "NONE", 0xDBB024, ModuleType.UI);
        setEnabled(true);
    }

}
