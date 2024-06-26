package me.ionar.salhack.util;

import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.util.entity.EntityUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.List;
import java.util.stream.Collectors;

public class CrystalUtils {
    public static boolean CanPlaceCrystalIfObbyWasAtPos(final BlockPos pos) {
        final Minecraft mc = Wrapper.GetMC();

        final Block floor = mc.world.getBlockState(pos.add(0, 1, 0)).getBlock();
        final Block ceil = mc.world.getBlockState(pos.add(0, 2, 0)).getBlock();

        if (floor == Blocks.AIR && ceil == Blocks.AIR) {
            return mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.add(0, 1, 0))).isEmpty();
        }

        return false;
    }

    public static boolean canPlaceCrystal(final BlockPos pos) {
        final Minecraft mc = Wrapper.GetMC();

        final Block block = mc.world.getBlockState(pos).getBlock();

        if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK) {
            final Block floor = mc.world.getBlockState(pos.add(0, 1, 0)).getBlock();
            final Block ceil = mc.world.getBlockState(pos.add(0, 2, 0)).getBlock();

            if (floor == Blocks.AIR && ceil == Blocks.AIR) {
                return mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.add(0, 1, 0))).isEmpty();
            }
        }

        return false;
    }

    /// Returns a BlockPos object of player's position floored.
    public static BlockPos GetPlayerPosFloored(final EntityPlayer player) {
        return new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
    }

    public static List<BlockPos> findCrystalBlocks(final EntityPlayer player, float range) {
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(
                BlockInteractionHelper.getSphere(GetPlayerPosFloored(player), range, (int) range, false, true, 0)
                        .stream().filter(CrystalUtils::canPlaceCrystal).collect(Collectors.toList()));
        return positions;
    }

    public static float calculateDamage(final World world, double posX, double posY, double posZ, Entity entity,
                                        int interlopedAmount) {
        /// hack
        if (entity == Wrapper.GetPlayer()) {
            if (Wrapper.GetPlayer().capabilities.isCreativeMode)
                return 0.0f;
        }

        float doubleExplosionSize = 12.0F;

        double distance = entity.getDistance(posX, posY, posZ);

        if (distance > doubleExplosionSize)
            return 0f;

        if (interlopedAmount > 0) {
            Vec3d interloped = EntityUtil.getInterpolatedAmount(entity, interlopedAmount);
            distance = EntityUtil.GetDistance(interloped.x, interloped.y, interloped.z, posX, posY, posZ);
        }

        double distancedsize = distance / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        double v = (1.0D - distancedsize) * blockDensity;
        float damage = (int) ((v * v + v) / 2.0D * 7.0D * doubleExplosionSize + 1.0D);
        double finald = 1.0D;
        /*
         * if (entity instanceof EntityLivingBase) finald =
         * getBlastReduction((EntityLivingBase) entity,getDamageMultiplied(damage));
         */
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(world, damage),
                    new Explosion(world, null, posX, posY, posZ, 6F, false, true));
        }
        return (float) finald;
    }

    public static float getBlastReduction(EntityLivingBase entity, float damage, Explosion explosion) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(),
                    (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());

            int k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            float f = MathHelper.clamp(k, 0.0F, 20.0F);
            damage *= 1.0F - f / 25.0F;

            if (entity.isPotionActive(Potion.getPotionById(11))) {
                damage -= damage / 4;
            }
            // damage = Math.max(damage - ep.getAbsorptionAmount(), 0.0F);
            return damage;
        }

        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(),
                (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    private static float getDamageMultiplied(final World world, float damage) {
        int diff = world.getDifficulty().getId();
        return damage * (diff == 0 ? 0 : (diff == 2 ? 1 : (diff == 1 ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(final World world, EntityEnderCrystal crystal, Entity entity) {
        return calculateDamage(world, crystal.posX, crystal.posY, crystal.posZ, entity, 0);
    }
}
