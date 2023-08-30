package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.main.Wrapper;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;

public class WatermarkComponent extends HudComponentItem {
    private static final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private static final String WatermarkString = hud.Rainbow.getValue() ? SalHackMod.NAME + " " + SalHackMod.VERSION : SalHackMod.NAME + ChatFormatting.WHITE + " " + SalHackMod.VERSION;
    public final Value<Boolean> Reliant = new Value<Boolean>("Reliant", new String[]
            {""}, "Shows reliant text instead of salhack", false);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;

    public WatermarkComponent() {
        super("Watermark", 2, 2);
        SetHidden(false);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (Reliant.getValue()) {
            final String text = "Reliant (rel-1.12.2-Forge)";

            Wrapper.GetMC().fontRenderer.drawStringWithShadow(text, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : 0xFFFFFF);

            SetWidth(Wrapper.GetMC().fontRenderer.getStringWidth(text));
            SetHeight(Wrapper.GetMC().fontRenderer.FONT_HEIGHT);
        } else {
            Rainbow.OnRender();
            RenderUtil.drawStringWithShadow(WatermarkString, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : 0x2ACCED);

            SetWidth(RenderUtil.getStringWidth(WatermarkString));
            SetHeight(RenderUtil.getStringHeight(WatermarkString));
        }
    }
}
