package me.ionar.salhack.module.movement;

import me.ionar.salhack.events.liquid.EventLiquidCollisionBB;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public final class JesusModule extends Module {

    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]
            {"Mode", "M"}, "The current Jesus/WaterWalk mode to use.", Mode.NCP);
    public final Value<Float> offset = new Value<Float>("Offset", new String[]
            {"Off", "O"}, "Amount to offset the player into the water's bounding box.", 0.18f, 0.0f, 0.9f, 0.01f);
    public final Value<Float> JumpHeight = new Value<Float>("JumpHeight", new String[]
            {"J"}, "JumpHeight for trampoline", 1.18f, 0.0f, 50.0f, 1.0f);
    private final Timer timer = new Timer();
    @EventHandler
    private final Listener<EventLiquidCollisionBB> OnLiquidCollisionBB = new Listener<>(event ->
    {
        if (mc.world != null && mc.player != null) {
            if (this.checkCollide() && !(mc.player.motionY >= 0.1f)
                    && event.getBlockPos().getY() < mc.player.posY - this.offset.getValue()) {
                if (mc.player.getRidingEntity() != null) {
                    event.setBoundingBox(new AxisAlignedBB(0, 0, 0, 1, 1 - this.offset.getValue(), 1));
                } else {
                    if (this.mode.getValue() == Mode.BOUNCE) {
                        event.setBoundingBox(new AxisAlignedBB(0, 0, 0, 1, 0.9f, 1));
                    } else {
                        event.setBoundingBox(Block.FULL_BLOCK_AABB);
                    }
                }
                event.cancel();
            }
        }
    });
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof CPacketPlayer) {
            if (this.mode.getValue() != Mode.VANILLA && mc.player.getRidingEntity() == null
                    && !mc.gameSettings.keyBindJump.isKeyDown()) {
                final CPacketPlayer packet = (CPacketPlayer) event.getPacket();

                if (!isInLiquid() && isOnLiquid(this.offset.getValue()) && checkCollide()
                        && mc.player.ticksExisted % 3 == 0) {
                    packet.y -= this.offset.getValue();
                }
            }
        }
    });

    public JesusModule() {
        super("Jesus", new String[]
                {"LiquidWalk", "WaterWalk"}, "Allows you to walk on water", "NONE", 0x24DB6E, ModuleType.MOVEMENT);
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    private boolean checkCollide() {
        if (mc.player.isSneaking()) {
            return false;
        }

        if (mc.player.getRidingEntity() != null) {
            if (mc.player.getRidingEntity().fallDistance >= 3.0f) {
                return false;
            }
        }

        return !(mc.player.fallDistance >= 3.0f);
    }

    public boolean isInLiquid() {
        if (mc.player.fallDistance >= 3.0f) {
            return false;
        }

        if (mc.player != null) {
            boolean inLiquid = false;
            final AxisAlignedBB bb = mc.player.getRidingEntity() != null
                    ? mc.player.getRidingEntity().getEntityBoundingBox()
                    : mc.player.getEntityBoundingBox();
            int y = (int) bb.minY;
            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX) + 1; x++) {
                for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ) + 1; z++) {
                    final Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (!(block instanceof BlockAir)) {
                        if (!(block instanceof BlockLiquid)) {
                            return false;
                        }
                        inLiquid = true;
                    }
                }
            }
            return inLiquid;
        }
        return false;
    }

    public boolean isOnLiquid(double offset) {
        if (mc.player.fallDistance >= 3.0f) {
            return false;
        }

        if (mc.player != null) {
            final AxisAlignedBB bb = mc.player.getRidingEntity() != null
                    ? mc.player.getRidingEntity().getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(0.0d,
                    -offset, 0.0d)
                    : mc.player.getEntityBoundingBox().contract(0.0d, 0.0d, 0.0d).offset(0.0d, -offset, 0.0d);
            boolean onLiquid = false;
            int y = (int) bb.minY;
            for (int x = MathHelper.floor(bb.minX); x < MathHelper.floor(bb.maxX + 1.0D); x++) {
                for (int z = MathHelper.floor(bb.minZ); z < MathHelper.floor(bb.maxZ + 1.0D); z++) {
                    final Block block = mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block != Blocks.AIR) {
                        if (!(block instanceof BlockLiquid)) {
                            return false;
                        }
                        onLiquid = true;
                    }
                }
            }
            return onLiquid;
        }

        return false;
    }

    private enum Mode {
        VANILLA, NCP, BOUNCE,
    }

}
