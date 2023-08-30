package me.ionar.salhack.module.world;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.render.EventRenderLayers;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.PlaceResult;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.Pair;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class AutoHighwayBuilder extends Module {
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]{""}, "Mode", Modes.ThreeWide);
    public final Value<BuildingModes> BuildingMode = new Value<BuildingModes>("BuildingMode", new String[]{""}, "Dynamic will update source block while walking, static keeps same position and resets on toggle", BuildingModes.Dynamic);
    public final Value<Integer> BlocksPerTick = new Value<Integer>("BlocksPerTick", new String[]{"BPT"}, "Blocks per tick", 4, 1, 10, 1);
    public final Value<Float> Delay = new Value<Float>("Delay", new String[]{"Delay"}, "Delay of the place", 0f, 0.0f, 1.0f, 0.1f);
    public final Value<Boolean> Visualize = new Value<Boolean>("Visualize", new String[]{"Render"}, "Visualizes where blocks are to be placed", true);
    ArrayList<BlockPos> BlockArray = new ArrayList<BlockPos>();
    private final Vec3d Center = Vec3d.ZERO;
    private final ICamera camera = new Frustum();
    private final Timer timer = new Timer();
    private final Timer NetherPortalTimer = new Timer();
    private BlockPos SourceBlock = null;
    private float PitchHead = 0.0f;
    private boolean SentPacket = false;
    @EventHandler
    private final Listener<EventRenderLayers> OnRender = new Listener<>(event ->
    {
        if (event.getEntityLivingBase() == mc.player)
            event.SetHeadPitch(PitchHead == -420.0f ? mc.player.rotationPitch : PitchHead);
    });
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE)
            return;

        if (!timer.passed(Delay.getValue() * 1000f))
            return;

        timer.reset();

        final Vec3d pos = MathUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());

        BlockPos orignPos = new BlockPos(pos.x, pos.y + 0.5f, pos.z);

        int lastSlot;
        Pair<Integer, Block> pair = findStackHotbar();

        int slot = -1;
        double offset = pos.y - orignPos.getY();

        if (pair != null) {
            slot = pair.getFirst();

            if (pair.getSecond() instanceof BlockSlab) {
                if (offset == 0.5f) {
                    orignPos = new BlockPos(pos.x, pos.y + 0.5f, pos.z);
                }
            }
        }

        if (BuildingMode.getValue() == BuildingModes.Dynamic)
            BlockArray.clear();

        if (BlockArray.isEmpty())
            FillBlockArrayAsNeeded(pos, orignPos, pair);

        boolean needPlace = false;

        float[] rotations = null;

        if (slot != -1) {
            if ((mc.player.onGround)) {
                lastSlot = mc.player.inventory.currentItem;
                mc.player.inventory.currentItem = slot;
                mc.playerController.updateController();

                int blocksPerTick = BlocksPerTick.getValue();

                for (BlockPos pos1 : BlockArray) {
                    /*ValidResult result = BlockInteractionHelper.valid(pos);

                    if (result == ValidResult.AlreadyBlockThere && !mc.world.getBlockState(pos).getMaterial().isReplaceable())
                        continue;

                    if (result == ValidResult.NoNeighbors)
                        continue;*/

                    PlaceResult place = BlockInteractionHelper.place(pos1, 5.0f, false, offset == -0.5f);

                    if (place != PlaceResult.Placed)
                        continue;

                    needPlace = true;
                    rotations = BlockInteractionHelper.getLegitRotations(new Vec3d(pos1.getX(), pos1.getY(), pos1.getZ()));
                    if (--blocksPerTick <= 0)
                        break;
                }

                if (!slotEqualsBlock(lastSlot, pair.getSecond())) {
                    mc.player.inventory.currentItem = lastSlot;
                }
                mc.playerController.updateController();
            }
        }

        if (!needPlace || rotations == null) {
            PitchHead = -420.0f;
            SentPacket = false;
            return;
        }

        event.cancel();

        /// @todo: clean this up

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
            PitchHead = pitch;

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

            SentPacket = true;
            mc.player.prevOnGround = mc.player.onGround;
            mc.player.autoJumpEnabled = mc.player.mc.gameSettings.autoJump;
        }
    });
    @EventHandler
    private final Listener<RenderEvent> OnRenderEvent = new Listener<>(event ->
    {
        if (!Visualize.getValue())
            return;

        for (BlockPos pos : BlockArray) {
            IBlockState state = mc.world.getBlockState(pos);

            if (state != null && state.getBlock() != Blocks.AIR && state.getBlock() != Blocks.WATER)
                continue;

            final AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - mc.getRenderManager().viewerPosX,
                    pos.getY() - mc.getRenderManager().viewerPosY, pos.getZ() - mc.getRenderManager().viewerPosZ,
                    pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    pos.getY() + (1) - mc.getRenderManager().viewerPosY,
                    pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY,
                    mc.getRenderViewEntity().posZ);

            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                glEnable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                glLineWidth(1.5f);

                final double dist = mc.player.getDistance(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f)
                        * 0.75f;

                float alpha = MathUtil.clamp((float) (dist * 255.0f / 5.0f / 255.0f), 0.0f, 0.3f);

                //  public static void drawBoundingBox(AxisAlignedBB bb, float width, int color)


                int color = 0x9000FFFF;

                RenderUtil.drawBoundingBox(bb, 1.0f, color);
                RenderUtil.drawFilledBox(bb, color);
                glDisable(GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    });

    public AutoHighwayBuilder() {
        super("AutoHighwayBuilder", new String[]
                {"AutoSwastika"}, "Automatically builds many types of highways", "NONE", 0x96DB24, ModuleType.WORLD);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (mc.player == null) {
            toggle();
            return;
        }

        timer.reset();
        SourceBlock = null;
        BlockArray.clear();
    }

    @Override
    public String getMetaData() {
        return Mode.getValue().toString() + " - " + BuildingMode.getValue().toString();
    }

    private boolean slotEqualsBlock(int slot, Block type) {
        if (mc.player.inventory.getStackInSlot(slot).getItem() instanceof ItemBlock) {
            final ItemBlock block = (ItemBlock) mc.player.inventory.getStackInSlot(slot).getItem();
            return block.getBlock() == type;
        }

        return false;
    }

    private void FillBlockArrayAsNeeded(final Vec3d pos, final BlockPos orignPos, final Pair<Integer, Block> pair) {
        BlockPos interpPos = null;

        switch (Mode.getValue()) {
            case FiveWide: {
                switch (PlayerUtil.GetFacing()) {
                    case East: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().east());
                        this.BlockArray.add(orignPos.down().east().north());
                        this.BlockArray.add(orignPos.down().east().south());
                        this.BlockArray.add(orignPos.down().east().north().north());
                        this.BlockArray.add(orignPos.down().east().south().south());
                        this.BlockArray.add(orignPos.down().east().north().north().north());
                        this.BlockArray.add(orignPos.down().east().south().south().south());
                        this.BlockArray.add(orignPos.down().east().north().north().north().up());
                        this.BlockArray.add(orignPos.down().east().south().south().south().up());
                        break;
                    }
                    case North: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().north());
                        this.BlockArray.add(orignPos.down().north().east());
                        this.BlockArray.add(orignPos.down().north().west());
                        this.BlockArray.add(orignPos.down().north().east().east());
                        this.BlockArray.add(orignPos.down().north().west().west());
                        this.BlockArray.add(orignPos.down().north().east().east().east());
                        this.BlockArray.add(orignPos.down().north().west().west().west());
                        this.BlockArray.add(orignPos.down().north().east().east().east().up());
                        this.BlockArray.add(orignPos.down().north().west().west().west().up());
                        break;
                    }
                    case South: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().south());
                        this.BlockArray.add(orignPos.down().south().east());
                        this.BlockArray.add(orignPos.down().south().west());
                        this.BlockArray.add(orignPos.down().south().east().east());
                        this.BlockArray.add(orignPos.down().south().west().west());
                        this.BlockArray.add(orignPos.down().south().east().east().east());
                        this.BlockArray.add(orignPos.down().south().west().west().west());
                        this.BlockArray.add(orignPos.down().south().east().east().east().up());
                        this.BlockArray.add(orignPos.down().south().west().west().west().up());
                        break;
                    }
                    case West: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().west());
                        this.BlockArray.add(orignPos.down().west().north());
                        this.BlockArray.add(orignPos.down().west().south());
                        this.BlockArray.add(orignPos.down().west().north().north());
                        this.BlockArray.add(orignPos.down().west().south().south());
                        this.BlockArray.add(orignPos.down().west().north().north().north());
                        this.BlockArray.add(orignPos.down().west().south().south().south());
                        this.BlockArray.add(orignPos.down().west().north().north().north().up());
                        this.BlockArray.add(orignPos.down().west().south().south().south().up());
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case FourWide: {
                switch (PlayerUtil.GetFacing()) {
                    case East: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().north());
                        this.BlockArray.add(orignPos.down().north().north());
                        this.BlockArray.add(orignPos.down().north().south());
                        this.BlockArray.add(orignPos.down().north().north().north());
                        this.BlockArray.add(orignPos.down().north().south().south());
                        this.BlockArray.add(orignPos.down().north().north().north().up());
                        this.BlockArray.add(orignPos.down().north().south().south().south());
                        this.BlockArray.add(orignPos.down().north().south().south().south().up());
                        break;
                    }
                    case North: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().north());
                        this.BlockArray.add(orignPos.down().north().east());
                        this.BlockArray.add(orignPos.down().north().west());
                        this.BlockArray.add(orignPos.down().north().east().east());
                        this.BlockArray.add(orignPos.down().north().west().west());
                        this.BlockArray.add(orignPos.down().north().east().east().up());
                        this.BlockArray.add(orignPos.down().north().west().west().west());
                        this.BlockArray.add(orignPos.down().north().west().west().west().up());
                        break;
                    }
                    case South: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().south());
                        this.BlockArray.add(orignPos.down().south().east());
                        this.BlockArray.add(orignPos.down().south().west());
                        this.BlockArray.add(orignPos.down().south().east().east());
                        this.BlockArray.add(orignPos.down().south().west().west());
                        this.BlockArray.add(orignPos.down().south().east().east().up());
                        this.BlockArray.add(orignPos.down().south().west().west().west());
                        this.BlockArray.add(orignPos.down().south().west().west().west().up());
                        break;
                    }
                    case West: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().west());
                        this.BlockArray.add(orignPos.down().west().north());
                        this.BlockArray.add(orignPos.down().west().south());
                        this.BlockArray.add(orignPos.down().west().north().north());
                        this.BlockArray.add(orignPos.down().west().south().south());
                        this.BlockArray.add(orignPos.down().west().north().east().up());
                        this.BlockArray.add(orignPos.down().west().south().south().south());
                        this.BlockArray.add(orignPos.down().west().south().south().south().up());
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case SevenWide: {
                switch (PlayerUtil.GetFacing()) {
                    case East: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().east());
                        this.BlockArray.add(orignPos.down().east().north());
                        this.BlockArray.add(orignPos.down().east().south());
                        this.BlockArray.add(orignPos.down().east().north().north());
                        this.BlockArray.add(orignPos.down().east().south().south());
                        this.BlockArray.add(orignPos.down().east().north().north().north());
                        this.BlockArray.add(orignPos.down().east().south().south().south());
                        this.BlockArray.add(orignPos.down().east().north().north().north().north());
                        this.BlockArray.add(orignPos.down().east().south().south().south().south());
                        this.BlockArray.add(orignPos.down().east().north().north().north().north().up());
                        this.BlockArray.add(orignPos.down().east().south().south().south().south().up());
                        break;
                    }
                    case North: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().north());
                        this.BlockArray.add(orignPos.down().north().east());
                        this.BlockArray.add(orignPos.down().north().west());
                        this.BlockArray.add(orignPos.down().north().east().east());
                        this.BlockArray.add(orignPos.down().north().west().west());
                        this.BlockArray.add(orignPos.down().north().east().east().east());
                        this.BlockArray.add(orignPos.down().north().west().west().west());
                        this.BlockArray.add(orignPos.down().north().east().east().east().east());
                        this.BlockArray.add(orignPos.down().north().west().west().west().west());
                        this.BlockArray.add(orignPos.down().north().east().east().east().east().up());
                        this.BlockArray.add(orignPos.down().north().west().west().west().west().up());
                        break;
                    }
                    case South: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().south());
                        this.BlockArray.add(orignPos.down().south().east());
                        this.BlockArray.add(orignPos.down().south().west());
                        this.BlockArray.add(orignPos.down().south().east().east());
                        this.BlockArray.add(orignPos.down().south().west().west());
                        this.BlockArray.add(orignPos.down().south().east().east().east());
                        this.BlockArray.add(orignPos.down().south().west().west().west());
                        this.BlockArray.add(orignPos.down().south().east().east().east().east());
                        this.BlockArray.add(orignPos.down().south().west().west().west().west());
                        this.BlockArray.add(orignPos.down().south().east().east().east().east().up());
                        this.BlockArray.add(orignPos.down().south().west().west().west().west().up());
                        break;
                    }
                    case West: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().west());
                        this.BlockArray.add(orignPos.down().west().north());
                        this.BlockArray.add(orignPos.down().west().south());
                        this.BlockArray.add(orignPos.down().west().north().north());
                        this.BlockArray.add(orignPos.down().west().south().south());
                        this.BlockArray.add(orignPos.down().west().north().north().north());
                        this.BlockArray.add(orignPos.down().west().south().south().south());
                        this.BlockArray.add(orignPos.down().west().north().north().north().north());
                        this.BlockArray.add(orignPos.down().west().south().south().south().south());
                        this.BlockArray.add(orignPos.down().west().north().north().north().north().up());
                        this.BlockArray.add(orignPos.down().west().south().south().south().south().up());
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case ThreeWide: {
                switch (PlayerUtil.GetFacing()) {
                    case East: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().east());
                        this.BlockArray.add(orignPos.down().east().north());
                        this.BlockArray.add(orignPos.down().east().south());
                        this.BlockArray.add(orignPos.down().east().north().north());
                        this.BlockArray.add(orignPos.down().east().south().south());
                        this.BlockArray.add(orignPos.down().east().north().north().up());
                        this.BlockArray.add(orignPos.down().east().south().south().up());
                        break;
                    }
                    case North: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().north());
                        this.BlockArray.add(orignPos.down().north().east());
                        this.BlockArray.add(orignPos.down().north().west());
                        this.BlockArray.add(orignPos.down().north().east().east());
                        this.BlockArray.add(orignPos.down().north().west().west());
                        this.BlockArray.add(orignPos.down().north().east().east().up());
                        this.BlockArray.add(orignPos.down().north().west().west().up());
                        break;
                    }
                    case South: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().south());
                        this.BlockArray.add(orignPos.down().south().east());
                        this.BlockArray.add(orignPos.down().south().west());
                        this.BlockArray.add(orignPos.down().south().east().east());
                        this.BlockArray.add(orignPos.down().south().west().west());
                        this.BlockArray.add(orignPos.down().south().east().east().up());
                        this.BlockArray.add(orignPos.down().south().west().west().up());
                        break;
                    }
                    case West: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().west());
                        this.BlockArray.add(orignPos.down().west().north());
                        this.BlockArray.add(orignPos.down().west().south());
                        this.BlockArray.add(orignPos.down().west().north().north());
                        this.BlockArray.add(orignPos.down().west().south().south());
                        this.BlockArray.add(orignPos.down().west().north().north().up());
                        this.BlockArray.add(orignPos.down().west().south().south().up());
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case RightRail: {
                switch (PlayerUtil.GetFacing()) {
                    case East: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().east());
                        this.BlockArray.add(orignPos.down().east().south());
                        this.BlockArray.add(orignPos.down().east().south().up());
                        break;
                    }
                    case North: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().north());
                        this.BlockArray.add(orignPos.down().north().west());
                        this.BlockArray.add(orignPos.down().north().west().up());
                        break;
                    }
                    case South: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().south());
                        this.BlockArray.add(orignPos.down().south().west());
                        this.BlockArray.add(orignPos.down().south().west().up());
                        break;
                    }
                    case West: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().west());
                        this.BlockArray.add(orignPos.down().west().south());
                        this.BlockArray.add(orignPos.down().west().south().up());
                        break;
                    }
                }
                break;
            }
            case LeftRail: {
                switch (PlayerUtil.GetFacing()) {
                    case East: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().east());
                        this.BlockArray.add(orignPos.down().east().north());
                        this.BlockArray.add(orignPos.down().east().north().up());
                        break;
                    }
                    case North: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().north());
                        this.BlockArray.add(orignPos.down().north().east());
                        this.BlockArray.add(orignPos.down().north().east().up());
                        break;
                    }
                    case South: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().south());
                        this.BlockArray.add(orignPos.down().south().east());
                        this.BlockArray.add(orignPos.down().south().east().up());
                        break;
                    }
                    case West: {
                        this.BlockArray.add(orignPos.down());
                        this.BlockArray.add(orignPos.down().west());
                        this.BlockArray.add(orignPos.down().west().north());
                        this.BlockArray.add(orignPos.down().west().north().up());
                        break;
                    }
                    default: {
                        break;
                    }
                }
                break;
            }
            case HighwayTunnel:
                BlockArray.add(orignPos.down());
                BlockArray.add(orignPos.down().north());
                BlockArray.add(orignPos.down().north().east());
                BlockArray.add(orignPos.down().north().west());
                BlockArray.add(orignPos.down().north().east().east());
                BlockArray.add(orignPos.down().north().west().west());
                BlockArray.add(orignPos.down().north().east().east().east());
                BlockArray.add(orignPos.down().north().west().west().west());
                BlockArray.add(orignPos.down().north().east().east().east().up());
                BlockArray.add(orignPos.down().north().west().west().west().up());
                BlockArray.add(orignPos.down().north().east().east().east().up().up());
                BlockArray.add(orignPos.down().north().west().west().west().up().up());
                BlockArray.add(orignPos.down().north().east().east().east().up().up().up());
                BlockArray.add(orignPos.down().north().west().west().west().up().up().up());
                BlockArray.add(orignPos.down().north().east().east().east().up().up().up().up());
                BlockArray.add(orignPos.down().north().west().west().west().up().up().up().up());
                BlockArray.add(orignPos.down().north().east().east().east().up().up().up().up().west());
                BlockArray.add(orignPos.down().north().west().west().west().up().up().up().up().east());
                BlockArray.add(orignPos.down().north().east().east().east().up().up().up().up().west().west());
                BlockArray.add(orignPos.down().north().west().west().west().up().up().up().up().east().east());
                BlockArray.add(orignPos.down().north().east().east().east().up().up().up().up().west().west().west());
                BlockArray.add(orignPos.down().north().west().west().west().up().up().up().up().east().east().east());
                break;
            case HighwayWall:
                switch (PlayerUtil.GetFacing()) {
                    case East:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).east().east();

                        for (int x = -2; x <= 3; ++x) {
                            for (int y = 0; y < 3; ++y) {
                                BlockArray.add(interpPos.add(0, y, x));
                            }
                        }
                        break;
                    case North:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).north().north();

                        for (int x = -2; x <= 3; ++x) {
                            for (int y = 0; y < 3; ++y) {
                                BlockArray.add(interpPos.add(x, y, 0));
                            }
                        }
                        break;
                    case South:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).south().south();

                        for (int x = -2; x <= 3; ++x) {
                            for (int y = 0; y < 3; ++y) {
                                BlockArray.add(interpPos.add(x, y, 0));
                            }
                        }
                        break;
                    case West:
                        interpPos = new BlockPos(pos.x, pos.y, pos.z).west().west();

                        for (int x = -2; x <= 3; ++x) {
                            for (int y = 0; y < 3; ++y) {
                                BlockArray.add(interpPos.add(0, y, x));
                            }
                        }
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private Pair<Integer, Block> findStackHotbar() {
        if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)
            return new Pair<Integer, Block>(mc.player.inventory.currentItem, ((ItemBlock) mc.player.getHeldItemMainhand().getItem()).getBlock());

        for (int i = 0; i < 9; i++) {
            final ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock) {
                final ItemBlock block = (ItemBlock) stack.getItem();

                return new Pair<Integer, Block>(i, block.getBlock());
            }
        }
        return null;
    }

    public Vec3d GetCenter(double posX, double posY, double posZ) {
        double x = Math.floor(posX) + 0.5D;
        double y = Math.floor(posY);
        double z = Math.floor(posZ) + 0.5D;

        return new Vec3d(x, y, z);
    }

    /// Verifies the array is all obsidian
    private boolean VerifyPortalFrame(ArrayList<BlockPos> blocks) {
        for (BlockPos pos : blocks) {
            IBlockState state = mc.world.getBlockState(pos);

            if (state == null || !(state.getBlock() instanceof BlockObsidian))
                return false;
        }

        return true;
    }

    public enum Modes {
        ThreeWide,
        FourWide,
        FiveWide,
        SevenWide,
        RightRail,
        LeftRail,
        HighwayTunnel,
        HighwayWall
    }

    public enum BuildingModes {
        Dynamic,
        Static,
    }
}
