package me.ionar.salhack.util.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;

public class PlayerUtil {
    final static DecimalFormat Formatter = new DecimalFormat("#.#");
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static int GetItemSlot(Item input) {
        if (mc.player == null)
            return 0;

        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i) {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);

            if (s.isEmpty())
                continue;

            if (s.getItem() == input) {
                return i;
            }
        }
        return -1;
    }

    public static int GetRecursiveItemSlot(Item input) {
        if (mc.player == null)
            return 0;

        for (int i = mc.player.inventoryContainer.getInventory().size() - 1; i > 0; --i) {
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);

            if (s.isEmpty())
                continue;

            if (s.getItem() == input) {
                return i;
            }
        }
        return -1;
    }

    public static int GetItemSlotNotHotbar(Item input) {
        if (mc.player == null)
            return 0;

        for (int i = 9; i < 36; i++) {
            final Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item == input) {
                return i;
            }
        }
        return -1;
    }

    public static int GetItemCount(Item input) {
        if (mc.player == null)
            return 0;

        int items = 0;

        for (int i = 0; i < 45; i++) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == input) {
                items += stack.getCount();
            }
        }

        return items;
    }

    public static boolean CanSeeBlock(BlockPos pos) {
        if (mc.player == null)
            return false;

        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.getX(), pos.getY(), pos.getZ()), false, true, false) == null;
    }

    public static boolean isCurrentViewEntity() {
        return mc.getRenderViewEntity() == mc.player;
    }

    public static boolean IsEating() {
        return mc.player != null && mc.player.getHeldItemMainhand().getItem() instanceof ItemFood && mc.player.isHandActive();
    }

    public static int GetItemInHotbar(Item item) {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack != ItemStack.EMPTY) {
                if (stack.getItem() == item) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static BlockPos GetLocalPlayerPosFloored() {
        return new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
    }

    public static BlockPos EntityPosToFloorBlockPos(Entity e) {
        return new BlockPos(Math.floor(e.posX), Math.floor(e.posY), Math.floor(e.posZ));
    }

    public static float GetHealthWithAbsorption() {
        return mc.player.getHealth() + mc.player.getAbsorptionAmount();
    }

    public static boolean IsPlayerInHole() {
        BlockPos blockPos = GetLocalPlayerPosFloored();

        IBlockState blockState = mc.world.getBlockState(blockPos);

        if (blockState.getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR)
            return false;

        if (mc.world.getBlockState(blockPos.down()).getBlock() == Blocks.AIR)
            return false;

        final BlockPos[] touchingBlocks = new BlockPos[]
                {blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west()};

        int validHorizontalBlocks = 0;
        for (BlockPos touching : touchingBlocks) {
            final IBlockState touchingState = mc.world.getBlockState(touching);
            if ((touchingState.getBlock() != Blocks.AIR) && touchingState.isFullBlock())
                validHorizontalBlocks++;
        }

        return validHorizontalBlocks >= 4;
    }

    public static void PacketFacePitchAndYaw(float pitch, float yaw) {
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

    public static boolean IsPlayerTrapped() {
        BlockPos playerPos = GetLocalPlayerPosFloored();

        final BlockPos[] trapPositions = {
                playerPos.down(),
                playerPos.up().up(),
                playerPos.north(),
                playerPos.south(),
                playerPos.east(),
                playerPos.west(),
                playerPos.north().up(),
                playerPos.south().up(),
                playerPos.east().up(),
                playerPos.west().up(),
        };

        for (BlockPos pos : trapPositions) {
            IBlockState state = mc.world.getBlockState(pos);

            if (state.getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos).getBlock() != Blocks.BEDROCK)
                return false;
        }

        return true;
    }

    public static boolean IsEntityTrapped(Entity e) {
        BlockPos playerPos = EntityPosToFloorBlockPos(e);

        final BlockPos[] trapPositions = {
                playerPos.up().up(),
                playerPos.north(),
                playerPos.south(),
                playerPos.east(),
                playerPos.west(),
                playerPos.north().up(),
                playerPos.south().up(),
                playerPos.east().up(),
                playerPos.west().up(),
        };

        for (BlockPos pos : trapPositions) {
            IBlockState state = mc.world.getBlockState(pos);

            if (state.getBlock() != Blocks.OBSIDIAN && mc.world.getBlockState(pos).getBlock() != Blocks.BEDROCK)
                return false;
        }

        return true;
    }

    public static FacingDirection GetFacing() {
        switch (MathHelper.floor((double) (mc.player.rotationYaw * 8.0F / 360.0F) + 0.5D) & 7) {
            case 0:
            case 1:
                return FacingDirection.South;
            case 2:
            case 3:
                return FacingDirection.West;
            case 4:
            case 5:
                return FacingDirection.North;
            case 6:
            case 7:
                return FacingDirection.East;
        }
        return FacingDirection.North;
    }

    public static float getSpeedInKM() {
        final double deltaX = mc.player.posX - mc.player.prevPosX;
        final double deltaZ = mc.player.posZ - mc.player.prevPosZ;

        float distance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double kMH = Math.floor((distance / 1000.0f) / (0.05f / 3600.0f));

        String formatter = Formatter.format(kMH);

        if (!formatter.contains("."))
            formatter += ".0";

        return Float.valueOf(formatter);
    }

    public enum FacingDirection {
        North,
        South,
        East,
        West,
    }
}
