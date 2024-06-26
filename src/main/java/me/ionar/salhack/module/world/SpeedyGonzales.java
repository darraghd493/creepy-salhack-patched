package me.ionar.salhack.module.world;

import me.ionar.salhack.events.player.EventPlayerClickBlock;
import me.ionar.salhack.events.player.EventPlayerDamageBlock;
import me.ionar.salhack.events.player.EventPlayerResetBlockRemoving;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public final class SpeedyGonzales extends Module {
    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]
            {"Mode", "M"}, "The speed-mine mode to use.", Mode.Instant);
    public final Value<Float> Speed = new Value<Float>("Speed", new String[]
            {"S"}, "Speed for Bypass Mode", 1.0f, 0.0f, 1.0f, 0.1f);
    public final Value<Boolean> reset = new Value<Boolean>("Reset", new String[]
            {"Res"}, "Stops current block destroy damage from resetting if enabled.", true);
    public final Value<Boolean> doubleBreak = new Value<Boolean>("DoubleBreak", new String[]
            {"DoubleBreak", "Double", "DB"}, "Mining a block will also mine the block above it, if enabled.", false);
    public final Value<Boolean> FastFall = new Value<Boolean>("FastFall", new String[]
            {"FF"}, "Makes it so you fall faster.", false);
    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        mc.playerController.blockHitDelay = 0;

        if (this.reset.getValue() && Minecraft.getMinecraft().gameSettings.keyBindUseItem.isKeyDown()) {
            mc.playerController.isHittingBlock = false;
        }

        if (FastFall.getValue()) {
            if (mc.player.onGround)
                --mc.player.motionY;
        }
    });
    @EventHandler
    private final Listener<EventPlayerResetBlockRemoving> ResetBlock = new Listener<>(event ->
    {
        if (this.reset.getValue()) {
            event.cancel();
        }
    });
    @EventHandler
    private final Listener<EventPlayerClickBlock> ClickBlock = new Listener<>(event ->
    {
        if (this.reset.getValue()) {
            if (mc.playerController.curBlockDamageMP > 0.1f) {
                mc.playerController.isHittingBlock = true;
            }
        }
    });
    @EventHandler
    private final Listener<EventPlayerDamageBlock> OnDamageBlock = new Listener<>(event ->
    {
        if (canBreak(event.getPos())) {
            if (this.reset.getValue()) {
                mc.playerController.isHittingBlock = false;
            }

            switch (this.mode.getValue()) {
                case Packet:
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(
                            CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getDirection()));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            event.getPos(), event.getDirection()));
                    event.cancel();
                    break;
                case Damage:
                    if (mc.playerController.curBlockDamageMP >= 0.7f) {
                        mc.playerController.curBlockDamageMP = 1.0f;
                    }
                    break;
                case Instant:
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(
                            CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getDirection()));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            event.getPos(), event.getDirection()));
                    mc.playerController.onPlayerDestroyBlock(event.getPos());
                    mc.world.setBlockToAir(event.getPos());
                    break;
                case Bypass:

                    mc.player.swingArm(EnumHand.MAIN_HAND);

                    final IBlockState blockState = Minecraft.getMinecraft().world.getBlockState(event.getPos());

                    float speed = blockState.getPlayerRelativeBlockHardness(mc.player, mc.world, event.getPos()) * Speed.getValue();


                    //  mc.playerController.onPlayerDestroyBlock(;)

                    break;
            }
        }

        if (this.doubleBreak.getValue()) {
            final BlockPos above = event.getPos().add(0, 1, 0);

            if (canBreak(above) && mc.player.getDistance(above.getX(), above.getY(), above.getZ()) <= 5f) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(
                        CPacketPlayerDigging.Action.START_DESTROY_BLOCK, above, event.getDirection()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        above, event.getDirection()));
                mc.playerController.onPlayerDestroyBlock(above);
                mc.world.setBlockToAir(above);
            }
        }
    });

    public SpeedyGonzales() {
        super("SpeedyGonzales", new String[]
                {"Speedy Gonzales"}, "Allows you to break blocks faster", "NONE", 0x24DB60, ModuleType.WORLD);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    private boolean canBreak(BlockPos pos) {
        final IBlockState blockState = mc.world.getBlockState(pos);
        final Block block = blockState.getBlock();

        return block.getBlockHardness(blockState, Minecraft.getMinecraft().world, pos) != -1;
    }

    private enum Mode {
        Packet, Damage, Instant, Bypass
    }

}
