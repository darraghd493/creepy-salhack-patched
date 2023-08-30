package me.ionar.salhack.module.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public final class AutoMendArmorModule extends Module {
    public final Value<Float> Delay = new Value<Float>("Delay", new String[]{"D"}, "Delay for moving armor pieces", 1.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Float> Pct = new Value<Float>("Pct", new String[]{"P"}, "Amount of armor pct to heal at, so you don't waste extra experience potions", 90.0f, 0.0f, 100.0f, 10.0f);
    public final Value<Boolean> GhostHand = new Value<Boolean>("GhostHand", new String[]{"GH"}, "Uses ghost hand for exp", false);
    private final LinkedList<MendState> SlotsToMoveTo = new LinkedList<MendState>();
    private final Timer timer = new Timer();
    private final Timer internalTimer = new Timer();
    private boolean ReadyToMend = false;
    private boolean AllDone = false;
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE)
            return;

        event.cancel();

        boolean isSprinting = mc.player.isSprinting();

        if (isSprinting != mc.player.serverSprintState) {
            if (isSprinting) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            } else {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            mc.player.serverSprintState = isSprinting;
        }

        boolean isSneaking = mc.player.isSneaking();

        if (isSneaking != mc.player.serverSneakState) {
            if (isSneaking) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            } else {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            mc.player.serverSneakState = isSneaking;
        }

        if (PlayerUtil.isCurrentViewEntity()) {
            float pitch = 90f;
            float yaw = mc.player.rotationYaw;

            AxisAlignedBB axisalignedbb = mc.player.getEntityBoundingBox();
            double posXDifference = mc.player.posX - mc.player.lastReportedPosX;
            double posYDifference = axisalignedbb.minY - mc.player.lastReportedPosY;
            double posZDifference = mc.player.posZ - mc.player.lastReportedPosZ;
            double yawDifference = yaw - mc.player.lastReportedYaw;
            double rotationDifference = pitch - mc.player.lastReportedPitch;
            ++mc.player.positionUpdateTicks;
            boolean movedXYZ = posXDifference * posXDifference + posYDifference * posYDifference + posZDifference * posZDifference > 9.0E-4D || mc.player.positionUpdateTicks >= 20;
            boolean movedRotation = yawDifference != 0.0D || rotationDifference != 0.0D;

            if (mc.player.isRiding()) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999.0D, mc.player.motionZ, yaw, pitch, mc.player.onGround));
                movedXYZ = false;
            } else if (movedXYZ && movedRotation) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, axisalignedbb.minY, mc.player.posZ, yaw, pitch, mc.player.onGround));
            } else if (movedXYZ) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, axisalignedbb.minY, mc.player.posZ, mc.player.onGround));
            } else if (movedRotation) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, mc.player.onGround));
            } else if (mc.player.prevOnGround != mc.player.onGround) {
                mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
            }

            if (movedXYZ) {
                mc.player.lastReportedPosX = mc.player.posX;
                mc.player.lastReportedPosY = axisalignedbb.minY;
                mc.player.lastReportedPosZ = mc.player.posZ;
                mc.player.positionUpdateTicks = 0;
            }

            if (movedRotation) {
                mc.player.lastReportedYaw = yaw;
                mc.player.lastReportedPitch = pitch;
            }

            mc.player.prevOnGround = mc.player.onGround;
            mc.player.autoJumpEnabled = mc.player.mc.gameSettings.autoJump;
        }

        if (timer.passed(Delay.getValue() * 100)) {
            timer.reset();

            if (SlotsToMoveTo.isEmpty())
                return;

            boolean needBreak = false;

            for (MendState state : SlotsToMoveTo) {
                if (state.MovedToInv)
                    continue;

                state.MovedToInv = true;

                //   SendMessage("" + state.SlotMovedTo);

                if (state.Reequip) {
                    if (state.SlotMovedTo <= 4) {
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, state.SlotMovedTo, 0, ClickType.PICKUP, mc.player);
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, state.ArmorSlot, 0, ClickType.PICKUP, mc.player);
                    } else
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, state.SlotMovedTo, 0, ClickType.QUICK_MOVE, mc.player);
                    //   mc.playerController.windowClick(mc.player.inventoryContainer.windowId, state.ArmorSlot, 0, ClickType.PICKUP, mc.player);
                } else {
                    //   mc.playerController.windowClick(mc.player.inventoryContainer.windowId, state.ArmorSlot, 0, ClickType.QUICK_MOVE, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, state.SlotMovedTo, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, state.ArmorSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, state.SlotMovedTo, 0, ClickType.PICKUP, mc.player);
                }

                needBreak = true;
                break;
            }

            if (!needBreak) {
                ReadyToMend = true;

                if (AllDone) {
                    SendMessage(ChatFormatting.AQUA + "Disabling.");
                    toggle();
                    return;
                }
            }
        }

        if (!internalTimer.passed(1000))
            return;

        if (ReadyToMend && !AllDone) {
            ItemStack currItem = mc.player.getHeldItemMainhand();

            int currSlot = -1;
            if (currItem.isEmpty() || currItem.getItem() != Items.EXPERIENCE_BOTTLE) {
                int slot = PlayerUtil.GetItemInHotbar(Items.EXPERIENCE_BOTTLE);

                if (slot != -1) {
                    currSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = slot;
                    mc.playerController.updateController();
                } else {
                    SendMessage(ChatFormatting.RED + "No XP Found!");

                    SlotsToMoveTo.forEach(state ->
                    {
                        state.MovedToInv = false;
                        state.Reequip = true;
                    });

                    SlotsToMoveTo.get(0).MovedToInv = true;
                    AllDone = true;
                    return;
                }
            }

            currItem = mc.player.getHeldItemMainhand();

            if (currItem.isEmpty() || currItem.getItem() != Items.EXPERIENCE_BOTTLE)
                return;

            final Iterator<ItemStack> armor = mc.player.getArmorInventoryList().iterator();

            while (armor.hasNext()) {
                ItemStack stack = armor.next();

                if (stack == ItemStack.EMPTY || stack.getItem() == Items.AIR)
                    continue;

                float armorPct = GetArmorPct(stack);

                if (armorPct >= Pct.getValue()) {
                    if (!SlotsToMoveTo.isEmpty()) {
                        MendState state = SlotsToMoveTo.get(0);

                        if (state.DoneMending) {
                            SlotsToMoveTo.forEach(state1 ->
                            {
                                state1.MovedToInv = false;
                                state1.Reequip = true;
                            });
                            SendMessage(ChatFormatting.GREEN + "All done!");
                            state.MovedToInv = true;
                            AllDone = true;
                            return;
                        }

                        state.DoneMending = true;
                        state.MovedToInv = false;
                        state.Reequip = false;

                        SendMessage(String.format("%sDone Mending %s%s %sat the requirement of %s NewPct: %s", ChatFormatting.LIGHT_PURPLE, ChatFormatting.AQUA, stack.getDisplayName(), ChatFormatting.LIGHT_PURPLE, Pct.getValue().toString() + "%", armorPct + "%"));
                        ReadyToMend = false;

                        SlotsToMoveTo.remove(0);
                        SlotsToMoveTo.add(state);

                        MendState newState = SlotsToMoveTo.get(0);

                        if (newState.DoneMending || !newState.NeedMend) {
                            SlotsToMoveTo.forEach(state1 ->
                            {
                                state1.MovedToInv = false;
                                state1.Reequip = true;
                            });
                            state.MovedToInv = true;
                            SendMessage(ChatFormatting.GREEN + "All done!");
                            AllDone = true;
                            return;
                        } else {
                            SendMessage(ChatFormatting.LIGHT_PURPLE + "Mending next piece.. it's name is " + ChatFormatting.AQUA + newState.ItemName);

                            newState.MovedToInv = false;
                            newState.Reequip = true;
                        }
                    }

                    return;
                } else {
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                    if (currSlot != -1 && GhostHand.getValue()) {
                        mc.player.inventory.currentItem = currSlot;
                        mc.playerController.updateController();
                    }

                    break;
                }
            }
        }
    });

    public AutoMendArmorModule() {
        super("AutoMendArmor", new String[]
                {"AMA"}, "Moves your armor to a free slot and mends them piece by piece. Recommended to use autoarmor incase you need to toggle this off while using it", "NONE", 0x24DBD4, ModuleType.MISC);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        ArrayList<ItemStack> ArmorsToMend = new ArrayList<ItemStack>();
        SlotsToMoveTo.clear();
        ReadyToMend = false;
        AllDone = false;

        int slot = PlayerUtil.GetItemInHotbar(Items.EXPERIENCE_BOTTLE);

        if (slot == -1) {
            SendMessage("You don't have any XP! Disabling!");
            toggle();
            return;
        }

        final Iterator<ItemStack> armor = mc.player.getArmorInventoryList().iterator();

        int i = 0;
        boolean needMend = false;

        while (armor.hasNext()) {
            final ItemStack item = armor.next();
            if (item != ItemStack.EMPTY && item.getItem() != Items.AIR) {
                ArmorsToMend.add(item);

                float pct = GetArmorPct(item);

                if (pct < Pct.getValue()) {
                    needMend = true;
                    SendMessage(ChatFormatting.LIGHT_PURPLE + "[" + ++i + "] Mending " + ChatFormatting.AQUA + item.getDisplayName() + ChatFormatting.LIGHT_PURPLE + " it has " + pct + "%.");
                }
            }
        }

        if (ArmorsToMend.isEmpty() || !needMend) {
            SendMessage(ChatFormatting.RED + "Nothing to mend!");
            toggle();
            return;
        }

        ArmorsToMend.sort(Comparator.comparing(ItemStack::getItemDamage).reversed());

        ArmorsToMend.forEach(item ->
        {
            SendMessage(item.getDisplayName() + " " + item.getItemDamage());
        });

        i = 0;

        final Iterator<ItemStack> itr = ArmorsToMend.iterator();

        boolean first = true;

        for (i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i) {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack stack = mc.player.inventoryContainer.getInventory().get(i);

            /// Slot must be empty or air
            if (!stack.isEmpty() && stack.getItem() != Items.AIR)
                continue;

            if (!itr.hasNext())
                break;

            ItemStack armorS = itr.next();

            SlotsToMoveTo.add(new MendState(first, i, GetSlotByItemStack(armorS), GetArmorPct(armorS) < Pct.getValue(), armorS.getDisplayName()));

            if (first)
                first = false;

            // SendMessage("Found free slot " + i + " for " + armorS.getDisplayName() + " stack here is " + stack.getDisplayName());
        }
    }

    public int GetSlotByItemStack(ItemStack stack) {
        if (stack.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor) stack.getItem();

            switch (armor.getEquipmentSlot()) {
                case CHEST:
                    return 6;
                case FEET:
                    return 8;
                case HEAD:
                    return 5;
                case LEGS:
                    return 7;
                default:
                    break;
            }
        }

        return mc.player.inventory.armorInventory.indexOf(stack) + 5;
    }

    private float GetArmorPct(ItemStack stack) {
        return ((float) (stack.getMaxDamage() - stack.getItemDamage()) / (float) stack.getMaxDamage()) * 100.0f;
    }

    private class MendState {
        public boolean MovedToInv = false;
        public int SlotMovedTo = -1;
        public boolean Reequip = false;
        public int ArmorSlot = -1;
        public boolean DoneMending = false;
        public boolean NeedMend = true;
        public String ItemName;
        public MendState(boolean movedToInv, int slotMovedTo, int armorSlot, boolean needMend, String itemName) {
            MovedToInv = movedToInv;
            SlotMovedTo = slotMovedTo;
            ArmorSlot = armorSlot;
            NeedMend = needMend;
            ItemName = itemName;
        }
    }
}
