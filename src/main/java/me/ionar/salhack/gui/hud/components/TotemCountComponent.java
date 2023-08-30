package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class TotemCountComponent extends HudComponentItem {
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    public TotemCountComponent() {
        super("TotemCount", 2, 215);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        int totemCount = 0;

        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i) {
            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);

            if (s.isEmpty())
                continue;

            if (s.getItem() == Items.TOTEM_OF_UNDYING) {
                ++totemCount;
            }
        }

        final String totemCount1 = hud.Rainbow.getValue() ? "Totems: " + totemCount : ChatFormatting.GRAY + "Totems: " + ChatFormatting.WHITE + totemCount;

        SetWidth(RenderUtil.getStringWidth(totemCount1));
        SetHeight(RenderUtil.getStringHeight(totemCount1));
        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(totemCount1, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);
    }

}
