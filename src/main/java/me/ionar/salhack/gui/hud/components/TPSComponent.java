package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.managers.TickRateManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;

public class TPSComponent extends HudComponentItem {
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    public TPSComponent() {
        super("TPS", 2, 125);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final String tickrate = hud.Rainbow.getValue() ? String.format("TPS %.2f", TickRateManager.Get().getTickRate()) : String.format(ChatFormatting.GRAY + "TPS%s %.2f", ChatFormatting.WHITE, TickRateManager.Get().getTickRate());

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(tickrate, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);

        SetWidth(RenderUtil.getStringWidth(tickrate));
        SetHeight(RenderUtil.getStringHeight(tickrate) + 1);
    }

}
