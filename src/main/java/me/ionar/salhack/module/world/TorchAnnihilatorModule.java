package me.ionar.salhack.module.world;

import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;

public class TorchAnnihilatorModule extends Module {
    public static Value<Integer> Radius = new Value<Integer>("Radius", new String[]{"R"}, "Radius to search for and break torches", 4, 0, 10, 1);
    public static Value<Boolean> RedstoneTorches = new Value<Boolean>("RedstoneTorches", new String[]{"R"}, "Break Flowers", true);
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        BlockPos closestPos = BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), Radius.getValue(), Radius.getValue(), false, true, 0).stream()
                .filter(pos -> IsValidBlockPos(pos))
                .min(Comparator.comparing(pos -> EntityUtil.GetDistanceOfEntityToBlock(mc.player, pos)))
                .orElse(null);

        if (closestPos != null) {
            event.cancel();

            final double[] pos = EntityUtil.calculateLookAt(
                    closestPos.getX() + 0.5,
                    closestPos.getY() - 0.5,
                    closestPos.getZ() + 0.5,
                    mc.player);

            mc.player.rotationYawHead = (float) pos[0];

            PlayerUtil.PacketFacePitchAndYaw((float) pos[1], (float) pos[0]);

            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.playerController.clickBlock(closestPos, EnumFacing.UP);
        }
    });

    public TorchAnnihilatorModule() {
        super("TorchAnnihilator", new String[]{""}, "Automatically breaks torches in a distance, AVO style", "NONE", -1, ModuleType.WORLD);
    }

    private boolean IsValidBlockPos(final BlockPos pos) {
        IBlockState state = mc.world.getBlockState(pos);

        if (state.getBlock() instanceof BlockTorch) {
            return RedstoneTorches.getValue() || !(state.getBlock() instanceof BlockRedstoneTorch);
        }

        return false;
    }
}
