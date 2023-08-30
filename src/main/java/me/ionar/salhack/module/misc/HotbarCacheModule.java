package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class HotbarCacheModule extends Module {
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]{"M"}, "The mode of refilling to use, Refill may cause desync", Modes.Cache);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[]{"D"}, "Delay to use", 1.0f, 0.0f, 10.0f, 1.0f);
    private final ArrayList<Item> Hotbar = new ArrayList<Item>();
    private final Timer timer = new Timer();
    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (mc.currentScreen != null)
            return;

        if (!timer.passed(Delay.getValue() * 1000))
            return;

        switch (Mode.getValue()) {
            case Cache:
                for (int i = 0; i < 9; ++i) {
                    if (SwitchSlotIfNeed(i)) {
                        timer.reset();
                        return;
                    }
                }
                break;
            case Refill:
                for (int i = 0; i < 9; ++i) {
                    if (RefillSlotIfNeed(i)) {
                        timer.reset();
                        return;
                    }
                }
                break;
            default:
                break;
        }
    });
    public HotbarCacheModule() {
        super("HotbarCache", new String[]{"HC"}, "Automatically refills your hotbar similar to how autototem works", "NONE", 0xB324DB, ModuleType.MISC);
    }

    @Override
    public String getMetaData() {
        return String.valueOf(Mode.getValue());
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Hotbar.clear();

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (!stack.isEmpty() && !Hotbar.contains(stack.getItem()))
                Hotbar.add(stack.getItem());
            else
                Hotbar.add(Items.AIR);
        }
    }

    /// Don't activate on startup
    @Override
    public void toggleNoSave() {

    }

    private boolean SwitchSlotIfNeed(int slot) {
        Item item = Hotbar.get(slot);

        if (item == Items.AIR)
            return false;

        if (!mc.player.inventory.getStackInSlot(slot).isEmpty() && mc.player.inventory.getStackInSlot(slot).getItem() == item)
            return false;

        int slot1 = PlayerUtil.GetItemSlot(item);

        if (slot1 != -1 && slot1 != 45) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot1, 0,
                    ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot1 + 36, 0, ClickType.PICKUP,
                    mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot1, 0,
                    ClickType.PICKUP, mc.player);
            mc.playerController.updateController();

            return true;
        }

        return false;
    }

    private boolean RefillSlotIfNeed(int slot) {
        ItemStack stack = mc.player.inventory.getStackInSlot(slot);

        if (stack.isEmpty() || stack.getItem() == Items.AIR)
            return false;

        if (!stack.isStackable())
            return false;

        if (stack.getCount() >= stack.getMaxStackSize())
            return false;

        /// We're going to search the entire inventory for the same stack, WITH THE SAME NAME, and use quick move.
        for (int i = 9; i < 36; ++i) {
            final ItemStack item = mc.player.inventory.getStackInSlot(i);

            if (item.isEmpty())
                continue;

            if (CanItemBeMergedWith(stack, item)) {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, i, 0,
                        ClickType.QUICK_MOVE, mc.player);
                mc.playerController.updateController();

                /// Check again for more next available tick
                return true;
            }
        }

        return false;
    }

    private boolean CanItemBeMergedWith(ItemStack source, ItemStack target) {
        return source.getItem() == target.getItem() && source.getDisplayName().equals(target.getDisplayName());
    }

    public enum Modes {
        Cache,
        Refill,
    }
}
