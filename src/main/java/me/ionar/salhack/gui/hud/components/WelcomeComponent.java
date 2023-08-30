package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;

import java.util.Calendar;

public class WelcomeComponent extends HudComponentItem {
    String welcome = "";
    Calendar c = Calendar.getInstance();
    int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    //I coded this but SalHack skid coded it better so props to https://github.com/pleasegivesource/SalHackSkid for this.
    public WelcomeComponent() {
        super("WelcomeComponent", 200, 2);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (timeOfDay >= 6 && timeOfDay < 12) {
            welcome = hud.Rainbow.getValue() ? "Good Morning, " + mc.getSession().getUsername() : ChatFormatting.AQUA + "Good Morning, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        } else if (timeOfDay >= 12 && timeOfDay < 17) {
            welcome = hud.Rainbow.getValue() ? "Good Afternoon, " + mc.getSession().getUsername() : ChatFormatting.AQUA + "Good Afternoon, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        } else if (timeOfDay >= 17 && timeOfDay < 22) {
            welcome = hud.Rainbow.getValue() ? "Good Evening, " + mc.getSession().getUsername() : ChatFormatting.AQUA + "Good Evening, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        } else if (timeOfDay >= 22 || timeOfDay < 6) {
            welcome = hud.Rainbow.getValue() ? "Good Night, " + mc.getSession().getUsername() : ChatFormatting.AQUA + "Good Night, " + ChatFormatting.WHITE + mc.getSession().getUsername();
        } else {
            welcome = hud.Rainbow.getValue() ? "Hello, " + mc.getSession().getUsername() : ChatFormatting.AQUA + "Hello, " + ChatFormatting.WHITE + mc.getSession().getUsername() + ".. psst! something went wrong!";
        }

        SetWidth(RenderUtil.getStringWidth(welcome));
        SetHeight(RenderUtil.getStringHeight(welcome) + 1);

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(welcome, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : 0x2ACCED);
    }
}
