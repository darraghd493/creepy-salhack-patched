package me.ionar.salhack.module.misc;

import me.ionar.salhack.module.Module;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;

import java.util.Comparator;

public class ManualDupeModule extends Module {

    private boolean noBypass;

    public ManualDupeModule() {
        super("ManualDupe", new String[]{""}, "For dupe button, disregard.", "NONE", -1, ModuleType.MISC);
    }

    @Override
    public void onEnable() {
        if (mc.world == null) {
            this.onDisable();
            return;
        }

        super.onEnable();

        Entity entity = mc.world.loadedEntityList.stream()
                .filter(this::isValidEntity)
                .min(Comparator.comparing(entity1 -> mc.player.getDistance(entity1)))
                .orElse(null);

        if (mc.currentScreen instanceof GuiScreenHorseInventory && entity instanceof AbstractChestHorse && mc.player.getRidingEntity() != null) {
            AbstractChestHorse abstractChestHorse = (AbstractChestHorse) entity;

            if (abstractChestHorse.hasChest()) {
                noBypass = true;
                mc.player.connection.sendPacket(new CPacketUseEntity(entity, EnumHand.MAIN_HAND, entity.getPositionVector()));
                noBypass = false;
                toggle();
            }

        }

    }

    private boolean isValidEntity(Entity entity) {
        if (entity instanceof AbstractChestHorse) {
            AbstractChestHorse abstractChestHorse = (AbstractChestHorse) entity;
            return !abstractChestHorse.isChild() && abstractChestHorse.isTame();
        }
        return false;
    }

    public boolean ignoreMountBypass() { //tell mount bypass when to disable
        return noBypass;
    }

}
