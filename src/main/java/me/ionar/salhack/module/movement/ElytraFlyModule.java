package me.ionar.salhack.module.movement;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerTravel;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer;

public final class ElytraFlyModule extends Module {
    public final Value<Mode> mode = new Value<Mode>("Mode", new String[]
            {"Mode", "M"}, "Mode to use for 2b2t flight.", Mode.Normal);
    public final Value<Float> speed = new Value<Float>("Speed", new String[]
            {"Spd"}, "Speed multiplier for flight, higher values equals more speed. - 2b speed recommended is 1.8~", 1.82f, 0.0f, 10.0f, 0.1f);
    public final Value<Float> DownSpeed = new Value<Float>("DownSpeed", new String[]
            {"DS"}, "DownSpeed multiplier for flight, higher values equals more speed.", 1.82f, 0.0f, 10.0f, 0.1f);
    public final Value<Float> GlideSpeed = new Value<Float>("GlideSpeed", new String[]
            {"GlideSpeed"}, "Glide value for acceleration, this is divided by 10000.", 1f, 0f, 10f, 1f);
    public final Value<Float> UpSpeed = new Value<Float>("UpSpeed", new String[]
            {"UpSpeed"}, "Up speed for elytra.", 2.0f, 0f, 10f, 1f);
    public final Value<Boolean> Accelerate = new Value<Boolean>("Accelerate", new String[]
            {"Accelerate", "Accelerate"}, "Auto accelerates when going up", true);
    public final Value<Integer> vAccelerationTimer = new Value<Integer>("Timer", new String[]
            {"AT"}, "Acceleration timer, default 1000", 1000, 0, 10000, 1000);
    public final Value<Float> RotationPitch = new Value<Float>("RotationPitch", new String[]
            {"RP"}, "RotationPitch default 0.0, this is for going up, -90 is lowest you can face, 90 is highest", 0.0f, -90f, 90f, 10.0f);
    public final Value<Boolean> CancelInWater = new Value<Boolean>("CancelInWater", new String[]
            {"CiW"}, "Cancel in water, anticheat will flag you if you try to go up in water, accelerating will still work.", true);
    public final Value<Integer> CancelAtHeight = new Value<Integer>("CancelAtHeight", new String[]
            {"CAH"}, "Doesn't allow flight Y is below, or if too close to bedrock. since 2b anticheat is wierd", 5, 0, 10, 1);
    public final Value<Boolean> InstantFly = new Value<Boolean>("InstantFly", new String[]
            {"IF"}, "Sends the fall flying packet when your off ground", true);
    public final Value<Boolean> EquipElytra = new Value<Boolean>("EquipElytra", new String[]{"EE"}, "Equips your elytra when enabled if you're not already wearing one", false);
    public final Value<Boolean> PitchSpoof = new Value<Boolean>("PitchSpoof", new String[]{"PS"}, "Spoofs your pitch for hauses new patch", false);

    private final Timer PacketTimer = new Timer();
    private final Timer AccelerationTimer = new Timer();
    private final Timer AccelerationResetTimer = new Timer();
    private final Timer InstantFlyTimer = new Timer();
    private boolean SendMessage = false;
    private FlightModule Flight = null;
    private int ElytraSlot = -1;
    @EventHandler
    private final Listener<EventPlayerTravel> OnTravel = new Listener<>(event ->
    {
        if (mc.player == null || Flight.isEnabled()) ///< Ignore if Flight is on: ex flat flying
            return;

        /// Player must be wearing an elytra.
        if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA)
            return;

        if (!mc.player.isElytraFlying()) {
            if (!mc.player.onGround && InstantFly.getValue()) {
                if (!InstantFlyTimer.passed(1000))
                    return;

                InstantFlyTimer.reset();

                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_FALL_FLYING));
            }

            return;
        }

        switch (mode.getValue()) {
            case Normal:
            case Tarzan:
            case Packet:
                HandleNormalModeElytra(event);
                break;
            case Superior:
                HandleImmediateModeElytra(event);
                break;
            case Control:
                HandleControlMode(event);
                break;
            default:
                break;
        }
    });
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof CPacketPlayer && PitchSpoof.getValue()) {
            if (!mc.player.isElytraFlying())
                return;

            if (event.getPacket() instanceof CPacketPlayer.PositionRotation && PitchSpoof.getValue()) {
                CPacketPlayer.PositionRotation rotation = (CPacketPlayer.PositionRotation) event.getPacket();

                mc.getConnection().sendPacket(new CPacketPlayer.Position(rotation.x, rotation.y, rotation.z, rotation.onGround));
                event.cancel();
            } else if (event.getPacket() instanceof CPacketPlayer.Rotation && PitchSpoof.getValue()) {
                event.cancel();
            }
        }
    });

    public ElytraFlyModule() {
        super("ElytraFly", new String[]
                {"ElytraFly2b2t"}, "Allows you to fly with elytra on 2b2t", "P", 0x24DB26, ModuleType.MOVEMENT);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        Flight = (FlightModule) ModuleManager.Get().GetMod(FlightModule.class);

        ElytraSlot = -1;

        if (EquipElytra.getValue()) {
            if (mc.player != null && mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.ELYTRA) {
                for (int i = 0; i < 44; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);

                    if (stack.isEmpty() || stack.getItem() != Items.ELYTRA)
                        continue;

                    ItemElytra elytra = (ItemElytra) stack.getItem();

                    ElytraSlot = i;
                    break;
                }

                if (ElytraSlot != -1) {
                    boolean hasArmorAtChest = mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() != Items.AIR;

                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, ElytraSlot, 0, ClickType.PICKUP, mc.player);
                    mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);

                    if (hasArmorAtChest)
                        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, ElytraSlot, 0, ClickType.PICKUP, mc.player);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (mc.player == null)
            return;

        if (ElytraSlot != -1) {
            boolean hasItem = !mc.player.inventory.getStackInSlot(ElytraSlot).isEmpty() || mc.player.inventory.getStackInSlot(ElytraSlot).getItem() != Items.AIR;

            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, ElytraSlot, 0, ClickType.PICKUP, mc.player);

            if (hasItem)
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
        }
    }

    @Override
    public String getMetaData() {
        return this.mode.getValue().name();
    }

    public void HandleNormalModeElytra(EventPlayerTravel travel) {
        double yHeight = mc.player.posY;

        if (yHeight <= CancelAtHeight.getValue()) {
            if (!SendMessage) {
                SalHack.SendMessage(ChatFormatting.RED + "WARNING, you must scaffold up or use fireworks, as YHeight <= CancelAtHeight!");
                SendMessage = true;
            }

            return;
        }

        boolean isMoveKeyDown = mc.player.movementInput.moveForward > 0 || mc.player.movementInput.moveStrafe > 0;

        boolean cancelInWater = !mc.player.isInWater() && !mc.player.isInLava() && CancelInWater.getValue();

        if (mc.player.movementInput.jump) {
            travel.cancel();
            Accelerate();
            return;
        }

        if (!isMoveKeyDown) {
            AccelerationTimer.resetTimeSkipTo(-vAccelerationTimer.getValue());
        } else if ((mc.player.rotationPitch <= RotationPitch.getValue() || mode.getValue() == Mode.Tarzan) && cancelInWater) {
            if (Accelerate.getValue()) {
                if (AccelerationTimer.passed(vAccelerationTimer.getValue())) {
                    Accelerate();
                    return;
                }
            }
            return;
        }

        travel.cancel();
        Accelerate();
    }

    public void HandleImmediateModeElytra(EventPlayerTravel travel) {
        if (mc.player.movementInput.jump) {
            double motionSq = Math.sqrt(mc.player.motionX * mc.player.motionX + mc.player.motionZ * mc.player.motionZ);

            if (motionSq > 1.0) {
                return;
            } else {
                double[] dir = MathUtil.directionSpeedNoForward(speed.getValue());

                mc.player.motionX = dir[0];
                mc.player.motionY = -(GlideSpeed.getValue() / 10000f);
                mc.player.motionZ = dir[1];
            }

            travel.cancel();
            return;
        }

        mc.player.setVelocity(0, 0, 0);

        travel.cancel();

        double[] dir = MathUtil.directionSpeed(speed.getValue());

        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
            mc.player.motionX = dir[0];
            mc.player.motionY = -(GlideSpeed.getValue() / 10000f);
            mc.player.motionZ = dir[1];
        }

        if (mc.player.movementInput.sneak)
            mc.player.motionY = -DownSpeed.getValue();

        mc.player.prevLimbSwingAmount = 0;
        mc.player.limbSwingAmount = 0;
        mc.player.limbSwing = 0;
    }

    public void Accelerate() {
        if (AccelerationResetTimer.passed(vAccelerationTimer.getValue())) {
            AccelerationResetTimer.reset();
            AccelerationTimer.reset();
            SendMessage = false;
        }

        float speed = this.speed.getValue();

        final double[] dir = MathUtil.directionSpeed(speed);

        mc.player.motionY = -(GlideSpeed.getValue() / 10000f);

        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
            mc.player.motionX = dir[0];
            mc.player.motionZ = dir[1];
        } else {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }

        if (mc.player.movementInput.sneak)
            mc.player.motionY = -DownSpeed.getValue();

        mc.player.prevLimbSwingAmount = 0;
        mc.player.limbSwingAmount = 0;
        mc.player.limbSwing = 0;
    }


    private void HandleControlMode(EventPlayerTravel event) {
        final double[] dir = MathUtil.directionSpeed(speed.getValue());

        if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
            mc.player.motionX = dir[0];
            mc.player.motionZ = dir[1];

            mc.player.motionX -= (mc.player.motionX * (Math.abs(mc.player.rotationPitch) + 90) / 90) - mc.player.motionX;
            mc.player.motionZ -= (mc.player.motionZ * (Math.abs(mc.player.rotationPitch) + 90) / 90) - mc.player.motionZ;
        } else {
            mc.player.motionX = 0;
            mc.player.motionZ = 0;
        }

        mc.player.motionY = (-MathUtil.degToRad(mc.player.rotationPitch)) * mc.player.movementInput.moveForward;


        mc.player.prevLimbSwingAmount = 0;
        mc.player.limbSwingAmount = 0;
        mc.player.limbSwing = 0;
        event.cancel();
    }

    private enum Mode {
        Normal, Tarzan, Superior, Packet, Control
    }
}
