package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.util.math.MathHelper;

import java.text.DecimalFormat;

public class YawComponent extends HudComponentItem {
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    public YawComponent() {
        super("Yaw", 2, 200);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        DecimalFormat format = new DecimalFormat("#.##");
        float yaw = MathHelper.wrapDegrees(mc.player.rotationYaw);

        String direction = hud.Rainbow.getValue() ? "Yaw: " + format.format(yaw) : ChatFormatting.GRAY + "Yaw: " + ChatFormatting.WHITE + format.format(yaw);

        if (!direction.contains("."))
            direction += ".00";
        else {
            String[] split = direction.split("\\.");

            if (split != null && split[1] != null && split[1].length() != 2)
                direction += 0;
        }

        SetWidth(RenderUtil.getStringWidth(direction));
        SetHeight(RenderUtil.getStringHeight(direction));

        Rainbow.OnRender();
        RenderUtil.drawStringWithShadow(direction, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);
    }

}
