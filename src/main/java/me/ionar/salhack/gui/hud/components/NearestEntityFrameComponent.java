package me.ionar.salhack.gui.hud.components;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;

import java.text.DecimalFormat;
import java.util.Comparator;

public class NearestEntityFrameComponent extends HudComponentItem {
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[]{"P"}, "Displays players", true);
    public final Value<Boolean> Friends = new Value<Boolean>("Friends", new String[]{"F"}, "Displays Friends", false);
    public final Value<Boolean> Mobs = new Value<Boolean>("Mobs", new String[]{"M"}, "Displays Mobs", true);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[]{"A"}, "Displays Animals", true);

    public NearestEntityFrameComponent() {
        super("NearestEntityFrame", 400, 2);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        //RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), GetX(), GetY(), 0xFFFFFF);

        RenderUtil.drawRect(GetX(), GetY(), GetX() + GetWidth(), GetY() + GetHeight(), 0x990C0C0C);
        //RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), GetX(), GetY(), 0xFFEC00);

        EntityLivingBase entity = mc.world.loadedEntityList.stream()
                .filter(this::IsValidEntity)
                .map(entity1 -> (EntityLivingBase) entity1)
                .min(Comparator.comparing(c -> mc.player.getDistance(c)))
                .orElse(null);

        if (entity == null)
            return;

        float healthPct = ((entity.getHealth() + entity.getAbsorptionAmount()) / entity.getMaxHealth()) * 100.0f;
        float healthBarPct = Math.min(healthPct, 100.0f);

        DecimalFormat format = new DecimalFormat("#.#");

        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GuiInventory.drawEntityOnScreen((int) GetX() + 10, (int) GetY() + 30, 15, mouseX, mouseY, entity);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        RenderUtil.drawStringWithShadow(entity.getName(), GetX() + 20, GetY() + 1, 0xFFFFFF);
        RenderUtil.drawGradientRect(GetX() + 20, GetY() + 11, GetX() + 20 + healthBarPct, GetY() + 22, 0x999FF365, 0x9913FF00);
        RenderUtil.drawStringWithShadow(String.format("(%s) %s / %s", format.format(healthPct) + "%", format.format(entity.getHealth() + entity.getAbsorptionAmount()), format.format(entity.getMaxHealth())), GetX() + 20, GetY() + 11, 0xFFFFFF);


        this.SetWidth(120);
        this.SetHeight(33);
    }

    private boolean IsValidEntity(Entity entity) {
        if (!(entity instanceof EntityLivingBase))
            return false;

        if (entity instanceof EntityPlayer) {
            if (entity == mc.player)
                return false;

            if (!Players.getValue())
                return false;

            if (FriendManager.Get().IsFriend(entity) && !Friends.getValue())
                return false;
        }

        if (EntityUtil.isHostileMob(entity) && !Mobs.getValue() || (entity instanceof EntityPigZombie && !Mobs.getValue()))
            return false;

        return !(entity instanceof EntityAnimal) || Animals.getValue();
    }
}
