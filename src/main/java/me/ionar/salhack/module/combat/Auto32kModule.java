package me.ionar.salhack.module.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.Pair;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.ItemUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.util.Comparator;

public class Auto32kModule extends Module {
    private final Value<Modes> Mode = new Value<Modes>("Mode", new String[]{"Modes"}, "The mode to use", Modes.Hopper);
    private final Value<Boolean> Automatic = new Value<Boolean>("Automatic", new String[]{"Automatic"}, "Automatically finds a place in range to place at", true);
    private final Value<Integer> Delay = new Value<Integer>("Delay", new String[]{"Delay"}, "Delay", 250, 0, 2000, 100);
    private final Value<Boolean> ThrowReverted = new Value<Boolean>("ThrowReverted", new String[]{"ThrowRevered"}, "Automatically throws reverted 32ks", true);
    private final Value<Boolean> Toggles = new Value<Boolean>("Toggles", new String[]{"Toggles"}, "Toggles off when out of the hopper", true);
    private int ShulkerSlot = -1;
    private BlockPos HopperPosition = null;
    private boolean WasInHopper = false;
    private boolean WasInDispenser = false;
    private final Timer Take32kTimer = new Timer();
    /// dispenser 32k
    private BlockPos DispenserPosition = null;
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof SPacketConfirmTransaction) {
            SPacketConfirmTransaction packet = (SPacketConfirmTransaction) event.getPacket();

            if (WasInDispenser && DispenserPosition != null) {
                mc.player.closeScreen();

                int redstoneBlock = GetSlotById(152);
                mc.player.inventory.currentItem = redstoneBlock;
                mc.playerController.updateController();
                BlockInteractionHelper.place(DispenserPosition.east(), 5.0f, true, false);

                DispenserPosition = null;

                mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(HopperPosition, EnumFacing.NORTH, EnumHand.MAIN_HAND, 0, 0, 0));
            }
        } else if (event.getPacket() instanceof SPacketOpenWindow) {
            SPacketOpenWindow packet = (SPacketOpenWindow) event.getPacket();

            if (DispenserPosition != null) {
                WasInDispenser = true;
                mc.playerController.windowClick(packet.getWindowId(), ShulkerSlot + 36, 0, ClickType.QUICK_MOVE, mc.player);
                //  SendMessage("Send the packet!");
            }
        } else if (event.getPacket() instanceof SPacketWindowItems) {
            Take32kTimer.reset();
        }
    });
    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (WasInHopper && Toggles.getValue() && mc.currentScreen == null) {
            toggle();
            return;
        }

        if (HopperPosition != null && DispenserPosition == null) {
            if (!(mc.currentScreen instanceof GuiHopper)) {
                if (!WasInHopper)
                    mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(HopperPosition, EnumFacing.NORTH, EnumHand.MAIN_HAND, 0, 0, 0));
            } else {
                if (!Take32kTimer.passed(Delay.getValue()))
                    return;

                WasInHopper = true;

                if (!(mc.world.getBlockState(HopperPosition.up()).getBlock() instanceof BlockShulkerBox)) {
                    mc.player.inventory.currentItem = ShulkerSlot;
                    mc.playerController.updateController();
                    BlockInteractionHelper.place(HopperPosition.up(), 5.0f, true, false);
                }

                if (mc.player.getHeldItemMainhand() != ItemStack.EMPTY && mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && ItemUtil.Is32k(mc.player.getHeldItemMainhand()))
                    return;

                /// This is where we find 32ks
                for (int i = 0; i < 4; ++i) {
                    ItemStack stack = mc.player.openContainer.getSlot(i).getStack();

                    if (stack == ItemStack.EMPTY)
                        continue;

                    if (stack.getItem() instanceof ItemSword) {
                        if (!ItemUtil.Is32k(stack))
                            continue;

                        int freeHotbarSlot = GetFreeHotbarSlot();

                        if (freeHotbarSlot != -1) {
                            mc.playerController.windowClick(((GuiContainer) mc.currentScreen).inventorySlots.windowId, i, 0, ClickType.QUICK_MOVE,
                                    mc.player);
                            //   mc.playerController.windowClick(mc.player.inventoryContainer.windowId, freeHotbarSlot+32, 0, ClickType.PICKUP,
                            //            mc.player);
                            break;
                        }
                    }
                }

                for (int i = 0; i < 9; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);

                    if (stack.isEmpty() || !(stack.getItem() instanceof ItemSword))
                        continue;

                    if (ItemUtil.Is32k(stack)) {
                        mc.player.inventory.currentItem = i;
                        mc.playerController.updateController();

                        SendMessage(String.format("Found 32k in slot %s", i));

                        KillAuraModule killAura = (KillAuraModule) ModuleManager.Get().GetMod(KillAuraModule.class);

                        if (!killAura.isEnabled())
                            killAura.toggle();

                        killAura.HitDelay.setValue(false);
                    } else if (ThrowReverted.getValue())
                        mc.playerController.windowClick(((GuiContainer) mc.currentScreen).inventorySlots.windowId, i + 32, 999, ClickType.THROW,
                                mc.player);
                }
            }
        } else if (DispenserPosition != null) {
            if (!(mc.currentScreen instanceof GuiDispenser)) {
                if (!WasInDispenser)
                    mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(DispenserPosition, EnumFacing.NORTH, EnumHand.MAIN_HAND, 0, 0, 0));
            }

        }
    });

    public Auto32kModule() {
        super("Auto32k", new String[]{"Auto32k"}, "Automatically bypasses the illegals plugin to let you use a 32k", "NONE", 0xFFFFFF, ModuleType.COMBAT);
    }

    @Override
    public String getMetaData() {
        if (ShulkerSlot == -1)
            return "No shulker";

        return null;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (mc.player == null) {
            toggle();
            return;
        }

        Pair<Integer, ItemStack> pair = GetShulkerSlotInHotbar();
        ShulkerSlot = pair.getFirst();

        if (pair.getSecond() != ItemStack.EMPTY) {
            SendMessage(String.format("%s[Auto32k] Found shulker %s", ChatFormatting.LIGHT_PURPLE, pair.getSecond().getDisplayName()));
        }

        HopperPosition = null;
        DispenserPosition = null;
        WasInHopper = false;
        WasInDispenser = false;

        if (Automatic.getValue()) {
            int hopperSlot = GetHopperSlot();

            if (hopperSlot == -1 || ShulkerSlot == -1)
                return;

            HopperPosition = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), 4.0f, 4, false, true, 0).stream()
                    .filter(pos -> IsValidBlockPos(pos))
                    .min(Comparator.comparing(pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, pos)))
                    .orElse(null);

            if (HopperPosition == null)
                return;

            mc.player.inventory.currentItem = hopperSlot;
            mc.playerController.updateController();
            BlockInteractionHelper.place(HopperPosition, 5.0f, true, false);
        } else {
            final RayTraceResult ray = mc.objectMouseOver;

            if (ray == null)
                return;

            if (ray.typeOfHit != RayTraceResult.Type.BLOCK)
                return;

            final BlockPos blockPos = ray.getBlockPos();

            int lastSlot = mc.player.inventory.currentItem;
            boolean needHopper = mc.world.getBlockState(blockPos).getBlock() != Blocks.HOPPER || !BlockInteractionHelper.IsLiquidOrAir(blockPos.up());

            HopperPosition = needHopper ? blockPos.up() : blockPos;
        }
    }

    private Pair<Integer, ItemStack> GetShulkerSlotInHotbar() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack != ItemStack.EMPTY) {
                if (stack.getItem() instanceof ItemShulkerBox) {
                    return new Pair<Integer, ItemStack>(i, stack);
                }
            }
        }

        return new Pair<Integer, ItemStack>(-1, ItemStack.EMPTY);
    }

    private int GetHopperSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack != ItemStack.EMPTY) {
                if (Item.getIdFromItem(stack.getItem()) == 154) ///< Hopper
                {
                    return i;
                }
            }
        }

        return -1;
    }

    private int GetSlotById(int id) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack != ItemStack.EMPTY) {
                if (Item.getIdFromItem(stack.getItem()) == id) ///< Hopper
                {
                    return i;
                }
            }
        }

        return -1;
    }

    private int GetFreeHotbarSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY || stack.getItem() == Items.AIR)
                return i;
        }

        return -1;
    }

    private boolean IsValidBlockPos(final BlockPos pos) {
        IBlockState state = mc.world.getBlockState(pos);

        if (state.getBlock() == Blocks.AIR && mc.world.getBlockState(pos.up()).getBlock() == Blocks.AIR) {
            ValidResult result = BlockInteractionHelper.valid(pos);

            return result == ValidResult.Ok;
        }

        return false;
    }

    enum Modes {
        Hopper,
        //    Dispenser,
    }
}
