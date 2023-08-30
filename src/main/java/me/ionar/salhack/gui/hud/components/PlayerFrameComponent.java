package me.ionar.salhack.gui.hud.components;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;

import java.text.DecimalFormat;

public class PlayerFrameComponent extends HudComponentItem {
    public PlayerFrameComponent() {
        super("PlayerFrame", 200, 2);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        RenderUtil.drawRect(GetX(), GetY(), GetX() + GetWidth(), GetY() + GetHeight(), 0x990C0C0C);
        //RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), GetX(), GetY(), 0xFFEC00);

        float healthPct = ((mc.player.getHealth() + mc.player.getAbsorptionAmount()) / mc.player.getMaxHealth()) * 100.0f;
        float healthBarPct = Math.min(healthPct, 100.0f);

        float hungerPct = (((float) mc.player.getFoodStats().getFoodLevel() + mc.player.getFoodStats().getSaturationLevel()) / 20) * 100.0f;
        float hungerBarPct = Math.min(hungerPct, 100.0f);

        DecimalFormat format = new DecimalFormat("#.#");

        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GuiInventory.drawEntityOnScreen((int) GetX() + 10, (int) GetY() + 30, 15, mouseX, mouseY, mc.player);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        RenderUtil.drawStringWithShadow(mc.getSession().getUsername(), GetX() + 20, GetY() + 1, 0xFFFFFF);
        RenderUtil.drawGradientRect(GetX() + 20, GetY() + 11, GetX() + 20 + healthBarPct, GetY() + 22, 0x999FF365, 0x9913FF00);
        RenderUtil.drawGradientRect(GetX() + 20, GetY() + 22, GetX() + 20 + hungerBarPct, GetY() + 33, 0x99F9AC05, 0x99F9AC05);
        RenderUtil.drawStringWithShadow(String.format("(%s) %s / %s", format.format(healthPct) + "%", format.format(mc.player.getHealth() + mc.player.getAbsorptionAmount()), format.format(mc.player.getMaxHealth())), GetX() + 20, GetY() + 11, 0xFFFFFF);
        RenderUtil.drawStringWithShadow(String.format("(%s) %s / %s", format.format(hungerPct) + "%", format.format(mc.player.getFoodStats().getFoodLevel() + mc.player.getFoodStats().getSaturationLevel()), "20"), GetX() + 20, GetY() + 22, 0xFFFFFF);
        
        /*final Iterator<ItemStack> items = mc.player.getArmorInventoryList().iterator();
        final ArrayList<ItemStack> stacks = new ArrayList<>();
        while (items.hasNext())
        {
            final ItemStack stack = items.next();
            if (stack != ItemStack.EMPTY && stack.getItem() != Items.AIR)
            {
                stacks.add(stack);
            }
        }
        Collections.reverse(stacks);
        
        for (int i = 0; i < stacks.size(); ++i)
        {
            ItemStack stack = stacks.get(i);
            
            int x = (int) (GetX() + 1);
            int y = (int) (GetY() + 40) + (i * 15);
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, x, y);
        }*/

        this.SetWidth(120);
        this.SetHeight(33);
    }

}
