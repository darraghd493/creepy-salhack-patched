package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;

public class FPSComponent extends HudComponentItem {
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    public FPSComponent() {
        super("FPS", 2, 140);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final String fPS = hud.Rainbow.getValue() ? String.format("FPS %s", Minecraft.getDebugFPS()) : String.format(ChatFormatting.GRAY + "FPS %s%s", ChatFormatting.WHITE, Minecraft.getDebugFPS());

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(fPS, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);

        SetWidth(RenderUtil.getStringWidth(fPS));
        SetHeight(RenderUtil.getStringHeight(fPS) + 1);
    }

}
