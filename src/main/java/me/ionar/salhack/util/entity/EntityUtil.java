package me.ionar.salhack.util.entity;

import me.ionar.salhack.util.MathUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

public class EntityUtil {

    public static boolean isPassive(Entity e) {
        if (e instanceof EntityWolf && ((EntityWolf) e).isAngry())
            return false;
        if (e instanceof EntityAnimal || e instanceof EntityAgeable || e instanceof EntityTameable
                || e instanceof EntityAmbientCreature || e instanceof EntitySquid)
            return true;
        return e instanceof EntityIronGolem && ((EntityIronGolem) e).getRevengeTarget() == null;
    }

    public static boolean isLiving(Entity e) {
        return e instanceof EntityLivingBase;
    }

    public static boolean isFakeLocalPlayer(Entity entity) {
        return entity != null && entity.getEntityId() == -100 && Minecraft.getMinecraft().player != entity;
    }

    /**
     * Find the entities interpolated amount
     */
    public static Vec3d getInterpolatedAmount(Entity entity, double x, double y, double z) {
        return new Vec3d((entity.posX - entity.lastTickPosX) * x, 0 * y,
                (entity.posZ - entity.lastTickPosZ) * z);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, Vec3d vec) {
        return getInterpolatedAmount(entity, vec.x, vec.y, vec.z);
    }

    public static Vec3d getInterpolatedAmount(Entity entity, double ticks) {
        return getInterpolatedAmount(entity, ticks, ticks, ticks);
    }

    public static boolean isMobAggressive(Entity entity) {
        if (entity instanceof EntityPigZombie) {
            // arms raised = aggressive, angry = either game or we have set the anger
            // cooldown
            if (((EntityPigZombie) entity).isArmsRaised() || ((EntityPigZombie) entity).isAngry()) {
                return true;
            }
        } else if (entity instanceof EntityWolf) {
            return ((EntityWolf) entity).isAngry()
                    && !Minecraft.getMinecraft().player.equals(((EntityWolf) entity).getOwner());
        } else if (entity instanceof EntityEnderman) {
            return ((EntityEnderman) entity).isScreaming();
        }
        return isHostileMob(entity);
    }

    /**
     * If the mob by default wont attack the player, but will if the player attacks
     * it
     */
    public static boolean isNeutralMob(Entity entity) {
        return entity instanceof EntityPigZombie || entity instanceof EntityWolf || entity instanceof EntityEnderman;
    }

    /**
     * If the mob is friendly (not aggressive)
     */
    public static boolean isFriendlyMob(Entity entity) {
        return (entity.isCreatureType(EnumCreatureType.CREATURE, false) && !EntityUtil.isNeutralMob(entity))
                || (entity.isCreatureType(EnumCreatureType.AMBIENT, false)) || entity instanceof EntityVillager
                || entity instanceof EntityIronGolem || (isNeutralMob(entity) && !EntityUtil.isMobAggressive(entity));
    }

    /**
     * If the mob is hostile
     */
    public static boolean isHostileMob(Entity entity) {
        return (entity.isCreatureType(EnumCreatureType.MONSTER, false) && !EntityUtil.isNeutralMob(entity));
    }

    /**
     * Find the entities interpolated position
     */
    public static Vec3d getInterpolatedPos(Entity entity, float ticks) {
        return new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ)
                .add(getInterpolatedAmount(entity, ticks));
    }

    public static Vec3d getInterpolatedRenderPos(Entity entity, float ticks) {
        return getInterpolatedPos(entity, ticks).subtract(Minecraft.getMinecraft().getRenderManager().renderPosX,
                Minecraft.getMinecraft().getRenderManager().renderPosY,
                Minecraft.getMinecraft().getRenderManager().renderPosZ);
    }

    public static boolean isInWater(Entity entity) {
        if (entity == null)
            return false;

        double y = entity.posY + 0.01;

        for (int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++) {
                BlockPos pos = new BlockPos(x, (int) y, z);

                if (Minecraft.getMinecraft().world.getBlockState(pos).getBlock() instanceof BlockLiquid)
                    return true;
            }

        return false;
    }

    public static boolean isDrivenByPlayer(Entity entityIn) {
        return Minecraft.getMinecraft().player != null && entityIn != null
                && entityIn.equals(Minecraft.getMinecraft().player.getRidingEntity());
    }

    public static boolean isAboveWater(Entity entity) {
        return isAboveWater(entity, false);
    }

    public static boolean isAboveWater(Entity entity, boolean packet) {
        if (entity == null)
            return false;

        double y = entity.posY - (packet ? 0.03 : (EntityUtil.isPlayer(entity) ? 0.2 : 0.5)); // increasing this seems
        // to flag more in NCP but
        // needs to be increased
        // so the player lands on
        // solid water

        for (int x = MathHelper.floor(entity.posX); x < MathHelper.ceil(entity.posX); x++)
            for (int z = MathHelper.floor(entity.posZ); z < MathHelper.ceil(entity.posZ); z++) {
                BlockPos pos = new BlockPos(x, MathHelper.floor(y), z);

                if (Minecraft.getMinecraft().world.getBlockState(pos).getBlock() instanceof BlockLiquid)
                    return true;
            }

        return false;
    }

    public static double[] calculateLookAt(double px, double py, double pz, EntityPlayer me) {
        double dirx = me.posX - px;
        double diry = me.posY - py;
        double dirz = me.posZ - pz;

        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;

        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);

        // to degree
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;

        yaw += 90f;

        return new double[]
                {yaw, pitch};
    }

    public static boolean isPlayer(Entity entity) {
        return entity instanceof EntityPlayer;
    }

    public static double getRelativeX(float yaw) {
        return MathHelper.sin(-yaw * 0.017453292F);
    }

    public static double getRelativeZ(float yaw) {
        return MathHelper.cos(yaw * 0.017453292F);
    }

    public static int GetPlayerMS(EntityPlayer player) {
        if (player.getUniqueID() == null)
            return 0;

        final NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(player.getUniqueID());

        if (playerInfo == null)
            return 0;

        return playerInfo.getResponseTime();
    }

    public static Vec3d CalculateExpectedPosition(EntityPlayer player) {
        Vec3d position = new Vec3d(player.posX, player.posY, player.posZ);

        if (player.lastTickPosX != player.posX && player.lastTickPosY != player.posY && player.lastTickPosZ != player.posZ)
            return position;

        int playerMS = GetPlayerMS(player);

        final double deltaX = player.posX - player.prevPosX;
        final double deltaZ = player.posZ - player.prevPosZ;
        final float tickRate = (Minecraft.getMinecraft().timer.tickLength / 1000.0f);

        float distance = MathHelper.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double facing = MathUtil.calculateAngle(player.posX, player.posZ, player.lastTickPosX, player.lastTickPosZ) / 45;

        return new Vec3d(
                player.posX + (Math.cos(facing) * distance),
                player.posY,
                player.posZ + (Math.sin(facing) * distance)
        );
    }

    public static double GetDistance(double x1, double y1, double z1, double x, double y, double z) {
        double d0 = x1 - x;
        double d1 = y1 - y;
        double d2 = z1 - z;
        return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static double GetDistanceOfEntityToBlock(Entity entity, BlockPos pos) {
        return GetDistance(entity.posX, entity.posY, entity.posZ, pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean IsVehicle(Entity entity) {
        return entity instanceof EntityBoat || entity instanceof EntityMinecart;
    }

    public static BlockPos GetPositionVectorBlockPos(Entity entity, @Nullable BlockPos toAdd) {
        final Vec3d v = entity.getPositionVector();

        if (toAdd == null)
            return new BlockPos(v.x, v.y, v.z);

        return new BlockPos(v.x, v.y, v.z).add(toAdd);
    }
}
