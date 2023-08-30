package me.ionar.salhack.module.combat;

import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.RotationSpoof;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.ItemUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

import javax.annotation.Nullable;
import java.util.Comparator;

public class KillAuraModule extends Module {
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]{"Mode"}, "The KillAura Mode to use", Modes.Closest);
    public final Value<Float> Distance = new Value<Float>("Distance", new String[]{"Range"}, "Range for attacking a target", 5.0f, 0.0f, 10.0f, 1.0f);
    public final Value<Boolean> HitDelay = new Value<Boolean>("Hit Delay", new String[]{"Hit Delay"}, "Use vanilla hit delay", true);
    public final Value<Boolean> TPSSync = new Value<Boolean>("TPSSync", new String[]{"TPSSync"}, "Use TPS Sync for hit delay", false);
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[]{"Players"}, "Should we target Players", true);
    public final Value<Boolean> Monsters = new Value<Boolean>("Monsters", new String[]{"Players"}, "Should we target Monsters", true);
    public final Value<Boolean> Neutrals = new Value<Boolean>("Neutrals", new String[]{"Players"}, "Should we target Neutrals", false);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[]{"Players"}, "Should we target Animals", false);
    public final Value<Boolean> Tamed = new Value<Boolean>("Tamed", new String[]{"Players"}, "Should we target Tamed", false);
    public final Value<Boolean> Projectiles = new Value<Boolean>("Projectile", new String[]{"Projectile"}, "Should we target Projectiles (shulker bullets, etc)", false);
    public final Value<Boolean> SwordOnly = new Value<Boolean>("SwordOnly", new String[]{"SwordOnly"}, "Only activate on sword", false);
    public final Value<Boolean> PauseIfCrystal = new Value<Boolean>("PauseIfCrystal", new String[]{"PauseIfCrystal"}, "Pauses if a crystal is in your hand", false);
    public final Value<Boolean> PauseIfEating = new Value<Boolean>("PauseIfEating", new String[]{"PauseIfEating"}, "Pauses if your eating", false);
    public final Value<Boolean> AutoSwitch = new Value<Boolean>("AutoSwitch", new String[]{"AutoSwitch"}, "Automatically switches to a sword in your hotbar", false);
    public final Value<Integer> Ticks = new Value<Integer>("Ticks", new String[]{"Ticks"}, "If you don't have HitDelay on, how fast the kill aura should be hitting", 10, 0, 40, 1);
    public final Value<Integer> Iterations = new Value<Integer>("Iterations", new String[]{""}, "Allows you to do more iteratons per tick", 1, 1, 10, 1);
    public final Value<Boolean> Only32k = new Value<Boolean>("32kOnly", new String[]{""}, "Only killauras when 32k sword is in your hand", false);
    private Entity CurrentTarget;
    private AutoCrystalModule AutoCrystal;
    private AimbotModule Aimbot;
    private final Timer AimbotResetTimer = new Timer();
    private int RemainingTicks = 0;
    @EventHandler
    private final Listener<EventClientTick> OnTick = new Listener<>(event ->
    {
        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword)) {
            if (mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && PauseIfCrystal.getValue())
                return;

            if (mc.player.getHeldItemMainhand().getItem() == Items.GOLDEN_APPLE && PauseIfEating.getValue())
                return;

            int slot = -1;

            if (AutoSwitch.getValue()) {
                for (int i = 0; i < 9; ++i) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemSword) {
                        slot = i;
                        mc.player.inventory.currentItem = slot;
                        mc.playerController.updateController();
                        break;
                    }
                }
            }

            if (SwordOnly.getValue() && slot == -1)
                return;
        }

        if (Only32k.getValue()) {
            if (!ItemUtil.Is32k(mc.player.getHeldItemMainhand()))
                return;
        }

        if (AimbotResetTimer.passed(5000)) {
            AimbotResetTimer.reset();
            Aimbot.m_RotationSpoof = null;
        }

        if (RemainingTicks > 0) {
            --RemainingTicks;
        }

        /// Chose target based on current mode
        Entity targetToHit = CurrentTarget;

        switch (Mode.getValue()) {
            case Closest:
                targetToHit = mc.world.loadedEntityList.stream()
                        .filter(entity -> IsValidTarget(entity, null))
                        .min(Comparator.comparing(entity -> mc.player.getDistance(entity)))
                        .orElse(null);
                break;
            case Priority:
                if (targetToHit == null) {
                    targetToHit = mc.world.loadedEntityList.stream()
                            .filter(entity -> IsValidTarget(entity, null))
                            .min(Comparator.comparing(entity -> mc.player.getDistance(entity)))
                            .orElse(null);
                }
                break;
            case Switch:
                targetToHit = mc.world.loadedEntityList.stream()
                        .filter(entity -> IsValidTarget(entity, null))
                        .min(Comparator.comparing(entity -> mc.player.getDistance(entity)))
                        .orElse(null);

                if (targetToHit == null)
                    targetToHit = CurrentTarget;

                break;
            default:
                break;

        }

        /// nothing to hit - return until next tick for searching
        if (targetToHit == null || targetToHit.getDistance(mc.player) > Distance.getValue()) {
            CurrentTarget = null;
            return;
        }

        float[] rotation = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), targetToHit.getPositionEyes(mc.getRenderPartialTicks()));
        Aimbot.m_RotationSpoof = new RotationSpoof(rotation[0], rotation[1]);

        final float ticks = 20.0f - TickRateManager.Get().getTickRate();

        final boolean isAttackReady = !this.HitDelay.getValue() || (mc.player.getCooledAttackStrength(TPSSync.getValue() ? -ticks : 0.0f) >= 1);

        if (!isAttackReady)
            return;

        if (!HitDelay.getValue() && RemainingTicks > 0)
            return;

        RemainingTicks = Ticks.getValue();

        //  mc.playerController.attackEntity(mc.player, targetToHit);
        for (int i = 0; i < Iterations.getValue(); ++i) {
            mc.player.connection.sendPacket(new CPacketUseEntity(targetToHit));
            mc.player.swingArm(EnumHand.MAIN_HAND);
            mc.player.resetCooldown();
        }
    });
    public KillAuraModule() {
        super("KillAura", new String[]{"Aura"}, "Automatically faces and hits entities around you", "NONE", 0xFF0000, ModuleType.COMBAT);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        RemainingTicks = 0;

        if (AutoCrystal == null) {
            AutoCrystal = (AutoCrystalModule) ModuleManager.Get().GetMod(AutoCrystalModule.class);
        }
        if (Aimbot == null) {
            Aimbot = (AimbotModule) ModuleManager.Get().GetMod(AimbotModule.class);

            if (!Aimbot.isEnabled())
                Aimbot.toggle();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (Aimbot != null)
            Aimbot.m_RotationSpoof = null;
    }

    @Override
    public String getMetaData() {
        return Mode.getValue().toString();
    }

    @Override
    public void toggleNoSave() {

    }

    private boolean IsValidTarget(Entity entity, @Nullable Entity toIgnore) {
        if (!(entity instanceof EntityLivingBase)) {
            boolean isProjectile = (entity instanceof EntityShulkerBullet || entity instanceof EntityFireball);

            if (!isProjectile)
                return false;

            if (isProjectile && !Projectiles.getValue())
                return false;
        }

        if (toIgnore != null && entity == toIgnore)
            return false;

        if (entity instanceof EntityPlayer) {
            /// Ignore if it's us
            if (entity == mc.player)
                return false;

            if (!Players.getValue())
                return false;

            /// They are a friend, ignore it.
            if (FriendManager.Get().IsFriend(entity))
                return false;
        }

        if (EntityUtil.isHostileMob(entity) && !Monsters.getValue())
            return false;

        if (EntityUtil.isPassive(entity)) {
            if (entity instanceof AbstractChestHorse) {
                AbstractChestHorse horse = (AbstractChestHorse) entity;

                if (horse.isTame() && !Tamed.getValue())
                    return false;
            }

            if (!Animals.getValue())
                return false;
        }

        if (EntityUtil.isHostileMob(entity) && !Monsters.getValue())
            return false;

        if (EntityUtil.isNeutralMob(entity) && !Neutrals.getValue())
            return false;

        boolean healthCheck = true;

        if (entity instanceof EntityLivingBase) {
            EntityLivingBase base = (EntityLivingBase) entity;

            healthCheck = !base.isDead && base.getHealth() > 0.0f;
        }

        return healthCheck && entity.getDistance(entity) <= Distance.getValue();
    }

    public enum Modes {
        Closest,
        Priority,
        Switch,
    }
}