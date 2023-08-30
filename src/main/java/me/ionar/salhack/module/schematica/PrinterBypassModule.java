package me.ionar.salhack.module.schematica;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.schematica.EventSchematicaPlaceBlock;
import me.ionar.salhack.events.schematica.EventSchematicaPlaceBlockFull;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PrinterBypassModule extends Module {
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]
            {"Mode"}, "Which mode to use for printer bypass", Modes.Full);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[]
            {"Delay"}, "Delay of the place for Full mode", 0f, 0.0f, 1.0f, 0.1f);
    private BlockPos BlockToPlace = null;
    private Item NeededItem = null;
    private final Timer timer = new Timer();
    @EventHandler
    private final Listener<EventSchematicaPlaceBlock> Position = new Listener<>(event ->
    {
        if (Mode.getValue() == Modes.Packet) {
            BlockInteractionHelper.faceVectorPacketInstant(new Vec3d(event.Pos.getX(), event.Pos.getY(), event.Pos.getZ()));
        }
    });
    @EventHandler
    private final Listener<EventSchematicaPlaceBlockFull> OnSchematicaPlaceBlockFull = new Listener<>(event ->
    {
        if (Mode.getValue() != Modes.Full)
            return;

        event.cancel();

        boolean result = BlockToPlace == null;

        if (result)
            BlockToPlace = event.Pos;

        event.Result = result;

        if (event.ItemStack != null)
            NeededItem = event.ItemStack;
        else
            NeededItem = null;
    });
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE)
            return;

        if (BlockToPlace == null)
            return;

        if (!timer.passed(Delay.getValue() * 1000f))
            return;

        /*if (NeededItem != null)
        {
            if (mc.player.getHeldItemMainhand().getItem() != NeededItem)
            {
                for (int i = 0; i < 9; ++i)
                {
                    if (mc.player.inventory.getStackInSlot(i).getItem() == NeededItem)
                    {
                        mc.player.inventory.currentItem = i;
                        mc.playerController.updateController();
                        break;
                    }
                }
            }

        }*/

        timer.reset();

        float[] rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(BlockToPlace.getX(), BlockToPlace.getY(), BlockToPlace.getZ()));

        /*
         * ValidResult result = BlockInteractionHelper.valid(pos);
         *
         * if (result == ValidResult.AlreadyBlockThere && !mc.world.getBlockState(pos).getMaterial().isReplaceable()) continue;
         *
         * if (result == ValidResult.NoNeighbors) continue;
         */

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
            float pitch = rotations[1];
            float yaw = rotations[0];

            mc.player.rotationYawHead = yaw;

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

        PlaceResult place = BlockInteractionHelper.place(BlockToPlace, 5.0f, false, false);

        //if (place == PlaceResult.Placed)
        //    SendMessage("Placed! at " + BlockToPlace.toString());

        BlockToPlace = null;
    });

    public PrinterBypassModule() {
        super("PrinterBypass", new String[]
                {"PrinterNCP"}, "Faces block rotations on schematica place block events", "NONE", 0xDB24AB, ModuleType.SCHEMATICA);
    }

    @Override
    public String getMetaData() {
        return String.valueOf(Mode.getValue());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        BlockToPlace = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        BlockToPlace = null;
    }

    public enum Modes {
        Packet,
        Full,
    }

}
