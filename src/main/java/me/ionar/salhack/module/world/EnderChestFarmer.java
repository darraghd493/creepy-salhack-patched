package me.ionar.salhack.module.world;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;

public class EnderChestFarmer extends Module {
    public static Value<Integer> Radius = new Value<Integer>("Radius", new String[]{"R"}, "Radius to search for enderchests, and place them", 4, 0, 10, 1);
    public Value<Float> Delay = new Value<Float>("Delay", new String[]
            {"D"}, "Timed delay for each place of ender chest", 1f, 0f, 10f, 1f);
    private final Timer PlaceTimer = new Timer();
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE)
            return;

        BlockPos closestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                .filter(pos -> IsValidBlockPos(pos))
                .min(Comparator.comparing(pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, pos)))
                .orElse(null);

        if (closestPos != null) {
            boolean hasPickaxe = mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_PICKAXE;

            if (!hasPickaxe) {
                for (int i = 0; i < 9; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);

                    if (stack.isEmpty())
                        continue;

                    if (stack.getItem() == Items.DIAMOND_PICKAXE) {
                        hasPickaxe = true;
                        mc.player.inventory.currentItem = i;
                        mc.playerController.updateController();
                        break;
                    }
                }
            }

            if (!hasPickaxe)
                return;

            event.cancel();

            final double[] pos = EntityUtil.calculateLookAt(
                    closestPos.getX() + 0.5,
                    closestPos.getY() - 0.5,
                    closestPos.getZ() + 0.5,
                    mc.player);

            PlayerUtil.PacketFacePitchAndYaw((float) pos[1], (float) pos[0]);

            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.player.connection.sendPacket(new CPacketPlayerDigging(
                    CPacketPlayerDigging.Action.START_DESTROY_BLOCK, closestPos, EnumFacing.UP));
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                    closestPos, EnumFacing.UP));
        } else {
            if (!PlaceTimer.passed(Delay.getValue() * 1000))
                return;

            PlaceTimer.reset();

            if (!IsCurrItemEnderChest()) {
                int slot = GetEnderChestSlot();

                if (slot == -1)
                    return;

                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();
            }

            for (BlockPos pos : BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0)) {
                PlaceResult result = BlockInteractionHelper.place(pos, Radius.getValue(), true, false);

                if (result == PlaceResult.Placed) {
                    event.cancel();

                    final double[] rotations = EntityUtil.calculateLookAt(
                            pos.getX() + 0.5,
                            pos.getY() - 0.5,
                            pos.getZ() + 0.5,
                            mc.player);

                    PlayerUtil.PacketFacePitchAndYaw((float) rotations[1], (float) rotations[0]);
                    return;
                }
            }
        }
    });

    public EnderChestFarmer() {
        super("EnderChestFarmer", new String[]{"EChestFarmer"}, "Automatically places enderchests around you, and attempts to mine it", "NONE", -1, ModuleType.WORLD);
    }

    private boolean IsValidBlockPos(final BlockPos pos) {
        IBlockState state = mc.world.getBlockState(pos);

        return state.getBlock() instanceof BlockEnderChest;
    }

    private boolean IsCurrItemEnderChest() {
        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock) mc.player.getHeldItemMainhand().getItem();
            return block.getBlock() == Blocks.ENDER_CHEST;
        }

        return false;
    }

    private int GetEnderChestSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack.isEmpty() || !(stack.getItem() instanceof ItemBlock))
                continue;

            final ItemBlock block = (ItemBlock) stack.getItem();
            if (block.getBlock() == Blocks.ENDER_CHEST)
                return i;
        }

        return -1;
    }
}
