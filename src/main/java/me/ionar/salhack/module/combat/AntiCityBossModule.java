package me.ionar.salhack.module.combat;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class AntiCityBossModule extends Module {
    public final Value<Boolean> TrapCheck = new Value<Boolean>("TrapCheck", new String[]
            {"HC"}, "Only functions if you're trapped", false);
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE)
            return;

        if (TrapCheck.getValue() && !PlayerUtil.IsPlayerTrapped())
            return;

        final int slot = findStackHotbar(Blocks.OBSIDIAN);

        /// Make sure we have obby.
        if (slot == -1)
            return;

        BlockPos centerPos = PlayerUtil.GetLocalPlayerPosFloored();
        ArrayList<BlockPos> BlocksToFill = new ArrayList<BlockPos>();

        switch (PlayerUtil.GetFacing()) {
            case East:
                BlocksToFill.add(centerPos.east().east());
                BlocksToFill.add(centerPos.east().east().up());
                BlocksToFill.add(centerPos.east().east().east());
                BlocksToFill.add(centerPos.east().east().east().up());
                break;
            case North:
                BlocksToFill.add(centerPos.north().north());
                BlocksToFill.add(centerPos.north().north().up());
                BlocksToFill.add(centerPos.north().north().north());
                BlocksToFill.add(centerPos.north().north().north().up());
                break;
            case South:
                BlocksToFill.add(centerPos.south().south());
                BlocksToFill.add(centerPos.south().south().up());
                BlocksToFill.add(centerPos.south().south().south());
                BlocksToFill.add(centerPos.south().south().south().up());
                break;
            case West:
                BlocksToFill.add(centerPos.west().west());
                BlocksToFill.add(centerPos.west().west().up());
                BlocksToFill.add(centerPos.west().west().west());
                BlocksToFill.add(centerPos.west().west().west().up());
                break;
            default:
                break;
        }

        BlockPos posToFill = null;

        for (BlockPos pos : BlocksToFill) {
            ValidResult result = BlockInteractionHelper.valid(pos);

            if (result != ValidResult.Ok)
                continue;

            posToFill = pos;
            break;
        }

        if (posToFill != null) {
            int lastSlot;
            lastSlot = mc.player.inventory.currentItem;
            mc.player.inventory.currentItem = slot;
            mc.playerController.updateController();

            event.cancel();
            float[] rotations = BlockInteractionHelper
                    .getLegitRotations(new Vec3d(posToFill.getX(), posToFill.getY(), posToFill.getZ()));
            PlayerUtil.PacketFacePitchAndYaw(rotations[1], rotations[0]);
            BlockInteractionHelper.place(posToFill, 5.0f, false, false);
            Finish(lastSlot);
        }
    });


    public AntiCityBossModule() {
        super("AntiCityBoss", new String[]
                        {"AntiTrap"}, "Automatically places 4 obsidian in the direction your facing to prevent getting crystaled",
                "NONE", -1, ModuleType.COMBAT);
    }

    @Override
    public void toggleNoSave() {

    }

    private void Finish(int lastSlot) {
        if (!slotEqualsBlock(lastSlot, Blocks.OBSIDIAN)) {
            mc.player.inventory.currentItem = lastSlot;
        }
        mc.playerController.updateController();
    }

    public boolean hasStack(Block type) {
        if (mc.player.inventory.getCurrentItem().getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getCurrentItem().getItem();
            return block.getBlock() == type;
        }
        return false;
    }

    private boolean slotEqualsBlock(int slot, Block type) {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    private int findStackHotbar(Block type) {
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock) {
                final ItemBlock block = (ItemBlock) stack.getItem();

                if (block.getBlock() == type) {
                    return i;
                }
            }
        }
        return -1;
    }
}
