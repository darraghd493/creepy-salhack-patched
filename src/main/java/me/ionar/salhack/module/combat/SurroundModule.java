package me.ionar.salhack.module.combat;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class SurroundModule extends Module {
    public final Value<Boolean> disable = new Value<Boolean>("Toggles", new String[]
            {"Toggles", "Disables"}, "Will toggle off after a place", false);
    public final Value<Boolean> ToggleOffGround = new Value<Boolean>("ToggleOffGround", new String[]
            {"Toggles", "Disables"}, "Will toggle off after a place", false);
    public final Value<CenterModes> CenterMode = new Value<CenterModes>("Center", new String[]
            {"Center"}, "Moves you to center of block", CenterModes.NCP);

    public final Value<Boolean> rotate = new Value<Boolean>("Rotate", new String[]
            {"rotate"}, "Rotate", true);
    public final Value<Integer> BlocksPerTick = new Value<Integer>("BlocksPerTick", new String[]{"BPT"}, "Blocks per tick", 1, 1, 10, 1);
    public final Value<Boolean> ActivateOnlyOnShift = new Value<Boolean>("ActivateOnlyOnShift", new String[]
            {"AoOS"}, "Activates only when shift is pressed.", false);
    private Vec3d Center = Vec3d.ZERO;
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE)
            return;

        if (ActivateOnlyOnShift.getValue()) {
            if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
                Center = Vec3d.ZERO;
                return;
            }

            if (Center == Vec3d.ZERO) {
                Center = GetCenter(mc.player.posX, mc.player.posY, mc.player.posZ);

                if (CenterMode.getValue() != CenterModes.None) {
                    mc.player.motionX = 0;
                    mc.player.motionZ = 0;
                }

                if (CenterMode.getValue() == CenterModes.Teleport) {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(Center.x, Center.y, Center.z, true));
                    mc.player.setPosition(Center.x, Center.y, Center.z);
                }
            }
        }

        /// NCP Centering
        if (Center != Vec3d.ZERO && CenterMode.getValue() == CenterModes.NCP) {
            double xDiff = Math.abs(Center.x - mc.player.posX);
            double zDiff = Math.abs(Center.z - mc.player.posZ);

            if (xDiff <= 0.1 && zDiff <= 0.1) {
                Center = Vec3d.ZERO;
            } else {
                double motionX = Center.x - mc.player.posX;
                double motionZ = Center.z - mc.player.posZ;

                mc.player.motionX = motionX / 2;
                mc.player.motionZ = motionZ / 2;
            }
        }

        if (!mc.player.onGround && !mc.player.prevOnGround && !ActivateOnlyOnShift.getValue()) {
            if (ToggleOffGround.getValue()) {
                toggle();
                SalHack.SendMessage("[Surround]: You are off ground! toggling!");
                return;
            }
        }

        final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        final BlockPos interpPos = new BlockPos(pos.x, pos.y, pos.z);

        final BlockPos north = interpPos.north();
        final BlockPos south = interpPos.south();
        final BlockPos east = interpPos.east();
        final BlockPos west = interpPos.west();

        BlockPos[] array = {north, south, east, west};

        /// We don't need to do anything if we are not surrounded
        if (IsSurrounded(mc.player))
            return;

        int lastSlot;
        final int slot = findStackHotbar(Blocks.OBSIDIAN);
        if (hasStack(Blocks.OBSIDIAN) || slot != -1) {
            if ((mc.player.onGround)) {
                lastSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();

                int blocksPerTick = BlocksPerTick.getValue();

                for (BlockPos pos1 : array) {
                    ValidResult result = BlockInteractionHelper.valid(pos1);

                    if (result == ValidResult.AlreadyBlockThere && !mc.world.getBlockState(pos1).getMaterial().isReplaceable())
                        continue;

                    if (result == ValidResult.NoNeighbors) {
                        final BlockPos[] test = {pos1.down(), pos1.north(), pos1.south(), pos1.east(), pos1.west(), pos1.up(),};

                        for (BlockPos pos2 : test) {
                            ValidResult result2 = BlockInteractionHelper.valid(pos2);

                            if (result2 == ValidResult.NoNeighbors || result2 == ValidResult.NoEntityCollision)
                                continue;

                            BlockInteractionHelper.place(pos2, 5.0f, false, false);
                            event.cancel();
                            float[] rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(pos2.getX(), pos2.getY(), pos2.getZ()));
                            ProcessBlizzFacing(rotations[0], rotations[1]);
                            break;
                        }

                        continue;
                    }

                    BlockInteractionHelper.place(pos1, 5.0f, false, false);

                    event.cancel();

                    float[] rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(pos1.getX(), pos1.getY(), pos1.getZ()));
                    ProcessBlizzFacing(rotations[0], rotations[1]);
                    if (--blocksPerTick <= 0)
                        break;
                }

                if (!slotEqualsBlock(lastSlot, Blocks.OBSIDIAN)) {
                    mc.player.inventory.currentItem = lastSlot;
                }
                mc.playerController.updateController();

                if (this.disable.getValue()) {
                    this.toggle();
                }
            }
        }
    });

    public SurroundModule() {
        super("Surround", new String[]
                {"NoCrystal"}, "Automatically surrounds you with obsidian in the four cardinal direrctions", "NONE", 0x5324DB, ModuleType.COMBAT);
    }

    @Override
    public String getMetaData() {
        return CenterMode.getValue().toString();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (mc.player == null) {
            toggle();
            return;
        }

        if (ActivateOnlyOnShift.getValue())
            return;

        Center = GetCenter(mc.player.posX, mc.player.posY, mc.player.posZ);

        if (CenterMode.getValue() != CenterModes.None) {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }

        if (CenterMode.getValue() == CenterModes.Teleport) {
            mc.player.connection.sendPacket(new CPacketPlayer.Position(Center.x, Center.y, Center.z, true));
            mc.player.setPosition(Center.x, Center.y, Center.z);
        }
    }

    @Override
    public void toggleNoSave() {

    }

    private void ProcessBlizzFacing(float yaw, float pitch) {
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
    }

    public boolean IsSurrounded(EntityPlayer who) {
        final Vec3d playerPos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        final BlockPos interpPos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);

        final BlockPos north = interpPos.north();
        final BlockPos south = interpPos.south();
        final BlockPos east = interpPos.east();
        final BlockPos west = interpPos.west();

        BlockPos[] array = {north, south, east, west};

        for (BlockPos pos : array) {
            if (BlockInteractionHelper.valid(pos) != ValidResult.AlreadyBlockThere) {
                return false;
            }
        }

        return true;
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

    public Vec3d GetCenter(double posX, double posY, double posZ) {
        double x = Math.floor(posX) + 0.5D;
        double y = Math.floor(posY);
        double z = Math.floor(posZ) + 0.5D;

        return new Vec3d(x, y, z);
    }

    public boolean HasObsidian() {
        return findStackHotbar(Blocks.OBSIDIAN) != -1;
    }

    public enum CenterModes {
        Teleport,
        NCP,
        None,
    }
}
