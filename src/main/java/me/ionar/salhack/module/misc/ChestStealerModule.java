package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;

public class ChestStealerModule extends Module {
    public Value<Modes> Mode = new Value<Modes>("Mode", new String[]
            {"M"}, "The mode for chest stealer", Modes.Steal);
    public Value<Float> Delay = new Value<Float>("Delay", new String[]
            {"D"}, "Delay for each tick", 1f, 0f, 10f, 1f);
    public Value<Boolean> DepositShulkers = new Value<Boolean>("DepositShulkers", new String[]
            {"S"}, "Only deposit shulkers", false);
    public Value<Boolean> EntityChests = new Value<Boolean>("EntityChests", new String[]
            {"EC"}, "Take from entity chests", false);
    public Value<Boolean> Shulkers = new Value<Boolean>("Shulkers", new String[]
            {"EC"}, "Take from shulkers", false);
    private final Timer timer = new Timer();
    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (!timer.passed(Delay.getValue() * 100f))
            return;

        timer.reset();

        if (mc.currentScreen instanceof GuiChest) {
            GuiChest chest = (GuiChest) mc.currentScreen;

            for (int i = 0; i < chest.lowerChestInventory.getSizeInventory(); ++i) {
                ItemStack stack = chest.lowerChestInventory.getStackInSlot(i);

                if ((stack.isEmpty() || stack.getItem() == Items.AIR) && Mode.getValue() == Modes.Store) {
                    HandleStoring(chest.inventorySlots.windowId, chest.lowerChestInventory.getSizeInventory() - 9);
                    return;
                }

                if (Shulkers.getValue() && !(stack.getItem() instanceof ItemShulkerBox))
                    continue;

                if (stack.isEmpty())
                    continue;

                switch (Mode.getValue()) {
                    case Steal:
                        mc.playerController.windowClick(chest.inventorySlots.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
                        return;
                    case Drop:
                        mc.playerController.windowClick(chest.inventorySlots.windowId, i, -999, ClickType.THROW, mc.player);
                        return;
                    default:
                        break;
                }
            }
        } else if (mc.currentScreen instanceof GuiScreenHorseInventory && EntityChests.getValue()) {
            GuiScreenHorseInventory chest = (GuiScreenHorseInventory) mc.currentScreen;

            for (int i = 0; i < chest.horseInventory.getSizeInventory(); ++i) {
                ItemStack stack = chest.horseInventory.getStackInSlot(i);

                if ((stack.isEmpty() || stack.getItem() == Items.AIR) && Mode.getValue() == Modes.Store) {
                    HandleStoring(chest.inventorySlots.windowId, chest.horseInventory.getSizeInventory() - 9);
                    return;
                }

                if (Shulkers.getValue() && !(stack.getItem() instanceof ItemShulkerBox))
                    continue;

                if (stack.isEmpty())
                    continue;

                switch (Mode.getValue()) {
                    case Steal:
                        mc.playerController.windowClick(chest.inventorySlots.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
                        return;
                    case Drop:
                        mc.playerController.windowClick(chest.inventorySlots.windowId, i, -999, ClickType.THROW, mc.player);
                        return;
                    default:
                        break;
                }
            }
        } else if (mc.currentScreen instanceof GuiShulkerBox && Shulkers.getValue()) {
            GuiShulkerBox chest = (GuiShulkerBox) mc.currentScreen;

            for (int i = 0; i < chest.inventory.getSizeInventory(); ++i) {
                ItemStack stack = chest.inventory.getStackInSlot(i);

                if ((stack.isEmpty() || stack.getItem() == Items.AIR) && Mode.getValue() == Modes.Store) {
                    HandleStoring(chest.inventorySlots.windowId, chest.inventory.getSizeInventory() - 9);
                    return;
                }

                if (Shulkers.getValue() && !(stack.getItem() instanceof ItemShulkerBox))
                    continue;

                if (stack.isEmpty())
                    continue;

                switch (Mode.getValue()) {
                    case Steal:
                        mc.playerController.windowClick(chest.inventorySlots.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
                        return;
                    case Drop:
                        mc.playerController.windowClick(chest.inventorySlots.windowId, i, -999, ClickType.THROW, mc.player);
                        return;
                    default:
                        break;
                }
            }
        }
    });

    public ChestStealerModule() {
        super("ChestStealer", new String[]
                {"Chest"}, "Steals the contents from chests", "NONE", 0xDB5E24, ModuleType.MISC);
    }

    @Override
    public String getMetaData() {
        return Mode.getValue().toString();
    }

    private void HandleStoring(int windowId, int slot) {
        if (Mode.getValue() == Modes.Store) {
            for (int y = 9; y < mc.player.inventoryContainer.inventorySlots.size() - 1; ++y) {
                ItemStack invStack = mc.player.inventoryContainer.getSlot(y).getStack();

                if (invStack.isEmpty() || invStack.getItem() == Items.AIR)
                    continue;

                if (Shulkers.getValue() && !(invStack.getItem() instanceof ItemShulkerBox))
                    continue;

                mc.playerController.windowClick(windowId, y + slot, 0, ClickType.QUICK_MOVE, mc.player);
                return;
            }
        }
    }

    public boolean isContainerOpen() {
        return mc.player.openContainer != null && isValidGui();
    }

    public boolean isValidGui() {
        return !(mc.currentScreen instanceof GuiEnchantment
                && mc.currentScreen instanceof GuiMerchant
                && mc.currentScreen instanceof GuiRepair
                && mc.currentScreen instanceof GuiBeacon
                && mc.currentScreen instanceof GuiCrafting
                && mc.currentScreen instanceof GuiContainerCreative
                && mc.currentScreen instanceof GuiInventory);
    }

    public enum Modes {
        Steal,
        Store,
        Drop,
    }

}
