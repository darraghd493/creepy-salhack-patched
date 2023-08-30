package me.ionar.salhack.gui.minecraft;

import me.ionar.salhack.main.AlwaysEnabledModule;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.misc.AutoReconnectModule;
import me.ionar.salhack.util.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.multiplayer.GuiConnecting;

import java.util.concurrent.TimeUnit;

public class SalGuiReconnectButton extends GuiButton {

    private final AutoReconnectModule Mod;
    private final Timer timer = new Timer();
    private final float ReconnectTimer;

    public SalGuiReconnectButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);

        timer.reset();

        Mod = (AutoReconnectModule) ModuleManager.Get().GetMod(AutoReconnectModule.class);
        ReconnectTimer = Mod.Delay.getValue() * 1000f;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        super.drawButton(mc, mouseX, mouseY, partialTicks);

        if (visible) {
            if (Mod.isEnabled() && !timer.passed(ReconnectTimer))
                this.displayString = "AutoReconnect (" + TimeUnit.MILLISECONDS.toSeconds(Math.abs((timer.getTime() + (long) ReconnectTimer) - System.currentTimeMillis())) + ")";
            else if (!Mod.isEnabled()) this.displayString = "AutoReconnect";

            if (timer.passed(ReconnectTimer) && Mod.isEnabled() && AlwaysEnabledModule.LastIP != null && AlwaysEnabledModule.LastPort != -1) {
                mc.displayGuiScreen(new GuiConnecting(null, mc, AlwaysEnabledModule.LastIP, AlwaysEnabledModule.LastPort));
            }
        }
    }

    public void Clicked() {
        Mod.toggle();

        timer.reset();
    }
}
