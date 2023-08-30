package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import org.lwjgl.input.Keyboard;

import java.util.Comparator;

public class AutoDuperModule extends Module {

    private final Value<Boolean> shulkerOnly = new Value<Boolean>("ShulkerOnly", new String[]{""}, "Only dupe shulkers.", true);
    private final Value<Boolean> hitGround = new Value<Boolean>("Ground", new String[]{""}, "Touch the ground in-between dupes.", true);
    private final Timer timer = new Timer(); //How long to wait.
    @EventHandler
    private final Listener<EntityJoinWorldEvent> OnWorldEvent = new Listener<>(event ->
    {
        if (event.getEntity() == mc.player) {
            toggle(); //toggle if we get kicked for flying
        }
    });
    public Value<Float> Delay = new Value<Float>("Delay", new String[]
            {"Delay, Wait"}, "Delay for each tick", 1f, 0f, 10f, 1f);
    private boolean doDrop = false; //When to drop items from entity.
    private boolean doChest = false; //When to add items to entity.
    private boolean doSneak = false; //Do a sneak.
    private boolean start = false; //When to start.
    private boolean finished = false; //When dupe is finished.
    private boolean grounded = false; //When to check if on ground.
    private int itemsToDupe;
    private int itemsMoved;
    private int itemsDropped;
    private GuiScreenHorseInventory chest;
    private boolean noBypass = false;
    @EventHandler
    private final Listener<EventPlayerUpdate> updateListener = new Listener<>(event -> {
        if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) //toggle on escape
        {
            toggle();
            return;
        }

        if (finished) {
            finished = false;
            itemsMoved = 0;
            itemsDropped = 0;
            start = true; //redo dupe
            return;
        }

        if (!timer.passed(Delay.getValue() * 100f))
            return;

        timer.reset();

        if (doSneak) {
            if (!mc.player.isSneaking()) { //if sneak failed
                mc.gameSettings.keyBindSneak.pressed = true;
                return;
            }
            mc.gameSettings.keyBindSneak.pressed = false;  //stop sneaking on new tick
            doSneak = false;
            if (!hitGround.getValue()) {
                finished = true;
            } else {
                grounded = true;
            }
            return;
        }

        if (grounded && mc.player.onGround) { //helps with getting kicked for flying
            grounded = false;
            finished = true;
            return;
        }

        if (start && isEnabled()) {
            itemsToDupe = 0;
            itemsMoved = 0;

            Entity entity = mc.world.loadedEntityList.stream()
                    .filter(this::isValidEntity)
                    .min(Comparator.comparing(entity1 -> mc.player.getDistance(entity1)))
                    .orElse(null);

            if (entity instanceof AbstractChestHorse) {
                AbstractChestHorse entity1 = (AbstractChestHorse) entity;

                if (!entity1.hasChest()) {
                    int slot = getChestInHotbar();

                    if (slot != -1 && mc.player.inventory.currentItem != slot) {
                        mc.player.inventory.currentItem = slot;
                        mc.playerController.updateController();
                        mc.playerController.interactWithEntity(mc.player, entity1, EnumHand.MAIN_HAND);
                    } else if (mc.player.inventory.currentItem != slot) {
                        SendMessage("No chests in hotbar, toggling...");
                        toggle();
                        return;
                    } else { //if chest is already in hand
                        mc.playerController.interactWithEntity(mc.player, entity1, EnumHand.MAIN_HAND);
                    }

                }

                start = false;
                mc.playerController.interactWithEntity(mc.player, entity1, EnumHand.MAIN_HAND); //ride entity
                mc.player.sendHorseInventory(); //open inventory
                doChest = true; //start next sequence

            }

        }

        if (doChest && !(mc.currentScreen instanceof GuiScreenHorseInventory)) { //check if we got kicked off entity
            doChest = false;
            start = true;
            return;
        }

        if (mc.currentScreen instanceof GuiScreenHorseInventory) {
            chest = (GuiScreenHorseInventory) mc.currentScreen; //this next part is taken from chest stealer

            itemsToDupe = getItemsToDupe();

            for (int i = 2; i < chest.horseInventory.getSizeInventory() + 1; ++i) {
                ItemStack stack = chest.horseInventory.getStackInSlot(i);

                if ((itemsToDupe == 0 || itemsMoved == chest.horseInventory.getSizeInventory() - 2) && doChest) { //itemsToDupe is for < donkey inventory slots, itemsMoved is for > donkey inventory slots
                    break; //break to execute code below
                } else if ((itemsDropped >= itemsMoved) && doDrop) { //execute code below
                    break;
                }

                if ((stack.isEmpty() || stack.getItem() == Items.AIR) && doChest) {
                    HandleStoring(chest.inventorySlots.windowId, chest.horseInventory.getSizeInventory() - 9);
                    itemsToDupe--;
                    itemsMoved = getItemsInRidingEntity();
                    return;
                } else {
                    if (doChest) { //if items were already in entity inventory
                        continue;
                    }
                }

                if (shulkerOnly.getValue() && !(stack.getItem() instanceof ItemShulkerBox))
                    continue;

                if (stack.isEmpty())
                    continue;

                if (doDrop) {
                    if (canStore()) { //move to inventory first, then drop
                        mc.playerController.windowClick(mc.player.openContainer.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
                    } else {
                        mc.playerController.windowClick(chest.inventorySlots.windowId, i, -999, ClickType.THROW, mc.player);
                    }
                    itemsDropped++;
                    return;
                }

            }

            if (doChest) {
                doChest = false;
                doDupe(); //break check
                return;
            }

            if (doDrop) {
                doDrop = false;
                mc.player.closeScreen();
                mc.gameSettings.keyBindSneak.pressed = true; //sending sneak packet messes with your connection
                doSneak = true;
            }

        }

    });

    public AutoDuperModule() {
        super("AutoDuper", new String[]{""}, "Perform SalC1 dupe automatically (Press esc to disable).", "NONE", 0xDB6824, ModuleType.MISC);
    }

    @Override
    public void onEnable() {

        super.onEnable();

        timer.reset();

        start = true;
    }

    @Override
    public void onDisable() {

        super.onDisable();

        noBypass = false;
        doDrop = false;
        doChest = false;
        doSneak = false;
        start = false;
        finished = false;
        grounded = false;

        itemsToDupe = 0;
        itemsMoved = 0;
        itemsDropped = 0;

        timer.reset();
    }

    private boolean isValidEntity(Entity entity) {
        if (entity instanceof AbstractChestHorse) {
            AbstractChestHorse chestHorse = (AbstractChestHorse) entity;

            return !chestHorse.isChild() && chestHorse.isTame();
        }

        return false;
    }

    private int getChestInHotbar() {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                final Block block = ((ItemBlock) stack.getItem()).getBlock();

                if (block instanceof BlockChest) {
                    return i;
                }
            }
        }
        return -1;
    }

    //Code from Chest Stealer.
    private void HandleStoring(int windowId, int slot) {
        for (int y = 9; y < mc.player.inventoryContainer.inventorySlots.size() - 1; ++y) {
            ItemStack invStack = mc.player.inventoryContainer.getSlot(y).getStack();

            if (invStack.isEmpty() || invStack.getItem() == Items.AIR)
                continue;

            if (!(invStack.getItem() instanceof ItemShulkerBox) && shulkerOnly.getValue())
                continue;

            mc.playerController.windowClick(windowId, y + slot, 0, ClickType.QUICK_MOVE, mc.player);
            return;
        }

    }

    private void doDupe() {
        noBypass = true; //turn off mount bypass

        Entity entity = mc.world.loadedEntityList.stream() //declaring this variable for the entire class causes NullPointerException
                .filter(this::isValidEntity)
                .min(Comparator.comparing(entity1 -> mc.player.getDistance(entity1)))
                .orElse(null);

        if (entity instanceof AbstractChestHorse) {
            mc.player.connection.sendPacket(new CPacketUseEntity(entity, EnumHand.MAIN_HAND, entity.getPositionVector())); //Packet to break chest.
            noBypass = false; //turn on mount bypass
            doDrop = true;
        }

    }

    private int getItemsToDupe() {
        int i = 0;

        for (int y = 9; y < mc.player.inventoryContainer.inventorySlots.size() - 1; ++y) {
            ItemStack invStack = mc.player.inventoryContainer.getSlot(y).getStack();

            if (invStack.isEmpty() || invStack.getItem() == Items.AIR)
                continue;

            if (!(invStack.getItem() instanceof ItemShulkerBox) && shulkerOnly.getValue())
                continue;

            i++;
        }

        if (i > chest.horseInventory.getSizeInventory() - 1)
            i = chest.horseInventory.getSizeInventory() - 1;

        return i;
    }

    private int getItemsInRidingEntity() {
        int i = 0;

        for (int i1 = 2; i1 < chest.horseInventory.getSizeInventory() + 1; ++i1) {
            ItemStack itemStack = chest.horseInventory.getStackInSlot(i1);

            if (itemStack.isEmpty() || itemStack.getItem() == Items.AIR)
                continue;

            i++;
        }
        return i;
    }

    private boolean canStore() { //check for drop or steal
        for (int y = 9; y < mc.player.inventoryContainer.inventorySlots.size() - 1; ++y) {
            ItemStack invStack = mc.player.inventoryContainer.getSlot(y).getStack();

            if (invStack.isEmpty() || invStack.getItem() == Items.AIR)
                return true;
        }
        return false;
    }

    public boolean ignoreMountBypass() { //tell mount bypass when to disable
        return noBypass;
    }

}
