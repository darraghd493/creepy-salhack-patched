package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.misc.StopWatchModule;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;

import java.util.concurrent.TimeUnit;

public class StopwatchComponent extends HudComponentItem {
    private StopWatchModule Stopwatch = null;
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    public StopwatchComponent() {
        super("Stopwatch", 2, 275);

        Stopwatch = (StopWatchModule) ModuleManager.Get().GetMod(StopWatchModule.class);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final String seconds = hud.Rainbow.getValue() ? "Seconds " + TimeUnit.MILLISECONDS.toSeconds(Stopwatch.ElapsedMS - Stopwatch.StartMS) : ChatFormatting.GRAY + "Seconds " + ChatFormatting.WHITE + TimeUnit.MILLISECONDS.toSeconds(Stopwatch.ElapsedMS - Stopwatch.StartMS);

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(seconds, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);

        SetWidth(RenderUtil.getStringWidth(seconds));
        SetHeight(RenderUtil.getStringHeight(seconds));
    }
}
