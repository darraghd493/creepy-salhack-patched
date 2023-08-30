package me.ionar.salhack.events.render;

import me.ionar.salhack.events.MinecraftEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EventRenderArmorLayer extends MinecraftEvent {
    public EntityLivingBase Entity;

    public EventRenderArmorLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, EntityEquipmentSlot slotIn) {
        super();

        Entity = entity;
    }
}
