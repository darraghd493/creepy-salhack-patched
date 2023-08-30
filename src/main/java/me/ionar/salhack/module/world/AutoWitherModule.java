package me.ionar.salhack.module.world;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.Pair;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.util.ArrayList;
import java.util.Iterator;

public class AutoWitherModule extends Module {
    private BlockPos WitherFeetBlock = null;
    private final ArrayList<Pair<BlockPos, Block>> Positions = new ArrayList<Pair<BlockPos, Block>>();
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnMotionUpdate = new Listener<>(event ->
    {
        if (Positions.isEmpty()) {
            SendMessage("Positions is empty");
            toggle();
            return;
        }

        Iterator<Pair<BlockPos, Block>> itr = Positions.iterator();

        Pair<BlockPos, Block> pos = null;
        boolean placed = false;

        while (itr.hasNext()) {
            pos = itr.next();

            int slot = -1;

            if (pos.getSecond() == Blocks.SOUL_SAND)
                slot = GetSoulsandInHotbar();
            else if (pos.getSecond() == Blocks.SKULL)
                slot = GetSkullInHotbar();

            if (slot != -1 && mc.player.inventory.currentItem != slot) {
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();
                return;
            }

            PlaceResult place = BlockInteractionHelper.place(pos.getFirst(), 5.0f, false, false);

            if (place != PlaceResult.Placed)
                continue;

            final double[] pos2 = EntityUtil.calculateLookAt(
                    pos.getFirst().getX() + 0.5,
                    pos.getFirst().getY() + 0.5,
                    pos.getFirst().getZ() + 0.5,
                    mc.player);

            mc.player.rotationYawHead = (float) pos2[0];

            PlayerUtil.PacketFacePitchAndYaw((float) pos2[1], (float) pos2[0]);

            placed = true;
            break;
        }

        if (pos != null && placed)
            Positions.remove(pos);
    });

    public AutoWitherModule() {
        super("AutoWither", new String[]{""}, "Automatically places a wither at the location of your selection if available", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void toggleNoSave() {

    }

    @Override
    public void onEnable() {
        super.onEnable();
        Positions.clear();
        WitherFeetBlock = null;

        final RayTraceResult ray = mc.objectMouseOver;

        if (ray == null)
            return;

        if (ray.typeOfHit != RayTraceResult.Type.BLOCK)
            return;

        IBlockState state = mc.world.getBlockState(ray.getBlockPos());

        if (state.getBlock() == Blocks.SOUL_SAND || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA) {
            /// Check blocks around it
            if (IsValidLocationForWitherBlocks(ray.getBlockPos()))
                WitherFeetBlock = ray.getBlockPos();
        } else {
            state = mc.world.getBlockState(ray.getBlockPos().up());

            if (state.getBlock() == Blocks.SOUL_SAND || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA)
                if (IsValidLocationForWitherBlocks(ray.getBlockPos().up())) {
                    WitherFeetBlock = ray.getBlockPos().up();

                    Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock, Blocks.SOUL_SAND));
                }
        }

        if (WitherFeetBlock == null) {
            SendMessage("Not a valid location for a wither.");
            toggle();
            return;
        }

        switch (PlayerUtil.GetFacing()) {
            case East:
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().south(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().north(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().south(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().north(), Blocks.SKULL));
                break;
            case North:
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().east(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().west(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().east(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().west(), Blocks.SKULL));
                break;
            case South:
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().west(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().east(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().west(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().east(), Blocks.SKULL));
                break;
            case West:
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().north(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().south(), Blocks.SOUL_SAND));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().north(), Blocks.SKULL));
                Positions.add(new Pair<BlockPos, Block>(WitherFeetBlock.up().up().south(), Blocks.SKULL));
                break;
            default:
                break;
        }
    }

    private boolean IsValidLocationForWitherBlocks(BlockPos pos) {
        BlockPos[] positions = {pos.up(), pos.up().east(), pos.up().west(), pos.up().up(), pos.up().up().east(), pos.up().up().west()};

        for (BlockPos pos1 : positions) {
            IBlockState state = mc.world.getBlockState(pos1);

            if (state.getBlock() != Blocks.AIR && state.getBlock() != Blocks.WATER && state.getBlock() != Blocks.LAVA)
                return false;
        }

        return true;
    }

    private int GetSoulsandInHotbar() {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlock) {
                final Block block = ((ItemBlock) stack.getItem()).getBlock();

                if (block instanceof BlockSoulSand) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int GetSkullInHotbar() {
        for (int i = 0; i < 9; ++i) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemSkull)
                return i;
        }
        return -1;
    }
}
