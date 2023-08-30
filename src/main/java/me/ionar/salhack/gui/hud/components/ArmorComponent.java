package me.ionar.salhack.gui.hud.components;

import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.FontManager;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class ArmorComponent extends HudComponentItem {
    public final Value<Modes> Mode = new Value<Modes>("Mode", new String[]{"Mode"}, "Modes", Modes.Bars);
    DecimalFormat Formatter = new DecimalFormat("#");
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;

    public ArmorComponent() {
        super("Armor", 200, 200);
    }

    public static float GetPctFromStack(ItemStack stack) {
        float armorPct = ((float) (stack.getMaxDamage() - stack.getItemDamage()) / (float) stack.getMaxDamage()) * 100.0f;
        float armorBarPct = Math.min(armorPct, 100.0f);

        return armorBarPct;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();

        super.render(mouseX, mouseY, partialTicks);

        final Iterator<ItemStack> items = mc.player.getArmorInventoryList().iterator();
        final ArrayList<ItemStack> stacks = new ArrayList<>();

        while (items.hasNext()) {
            final ItemStack stack = items.next();
            if (stack != ItemStack.EMPTY && stack.getItem() != Items.AIR) {
                stacks.add(stack);
            }
        }

        Collections.reverse(stacks);

        switch (Mode.getValue()) {
            case Bars:

                RenderUtil.drawRect(GetX(), GetY(), GetX() + GetWidth(), GetY() + GetHeight(), 0x990C0C0C);

                int y = 0;

                for (int i = 0; i < stacks.size(); ++i) {
                    ItemStack stack = stacks.get(i);

                    float x = (GetX() + 1);

                    float armorPct = ((float) (stack.getMaxDamage() - stack.getItemDamage()) / (float) stack.getMaxDamage()) * 100.0f;
                    float armorBarPct = Math.min(armorPct, 100.0f);

                    int colorMin = 0x999FF365;
                    int colorMax = 0x9913FF00;

                    if (armorBarPct < 80f && armorPct > 30f) {
                        colorMin = 0x99FFB600;
                        colorMax = 0x99FFF700;
                    } else if (armorBarPct < 30f) {
                        colorMin = 0x99FF0000;
                        colorMax = 0x99DA1A1A;
                    }

                    RenderUtil.drawGradientRect(x, GetY() + y, x + (GetWidth() * (armorBarPct / 100.0f)), GetY() + y + 15, colorMin, colorMax);

                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int) x, (int) GetY() + y);
                    //    mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, x, (int)GetY() + y); - enable if you want normal bar to show, but it looks worse

                    String durability = String.format("%s %s / %s", Formatter.format(armorBarPct) + "%", stack.getMaxDamage() - stack.getItemDamage(), stack.getMaxDamage());

                    x = GetX() + 18;

                    RenderUtil.drawStringWithShadow(durability, x, GetY() + y + 2, 0xFFFFFF);

                    y += 15;
                }

                this.SetWidth(100);
                this.SetHeight(y);
                break;
            case SimplePct:

                float x = 0;
                float textX = 4;
                for (int i = 0; i < stacks.size(); ++i) {
                    ItemStack stack = stacks.get(i);

                    mc.getRenderItem().renderItemAndEffectIntoGUI(stack, (int) (GetX() + x), (int) GetY() + 10);
                    mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, (int) (GetX() + x), (int) GetY() + 10);

                    Rainbow.OnRender();
                    FontManager.Get().TWCenMt18.drawCenteredString(Formatter.format(GetPctFromStack(stack)), GetX() + textX, GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);

                    x += 20;

                    if (i < stacks.size() - 1) {
                        float pct = GetPctFromStack(stacks.get(i + 1));

                        if (pct == 100.0f)
                            textX += 22;
                        else if (pct >= 10.0)
                            textX += 21f;
                        else
                            textX += 25f;
                    }
                }

                SetWidth(75);
                SetHeight(24);
                break;
            default:
                break;
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    public enum Modes {
        Bars,
        SimplePct,
    }
}
