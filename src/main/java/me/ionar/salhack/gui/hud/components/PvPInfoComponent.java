package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.combat.AutoCrystalModule;
import me.ionar.salhack.module.combat.AutoCrystalRewrite;
import me.ionar.salhack.module.combat.AutoTrap;
import me.ionar.salhack.module.combat.KillAuraModule;
import me.ionar.salhack.module.movement.SpeedModule;
import me.ionar.salhack.util.render.RenderUtil;

public class PvPInfoComponent extends HudComponentItem {
    private final KillAuraModule _killAura;
    private final AutoCrystalModule _autoCrystal;
    private final AutoTrap _autoTrap;
    private final SpeedModule _speed;
    private final AutoCrystalRewrite _autoCrystalRewrite;
    public PvPInfoComponent() {
        super("PvPInfo", 2, 290);

        _killAura = (KillAuraModule) ModuleManager.Get().GetMod(KillAuraModule.class);
        _autoCrystal = (AutoCrystalModule) ModuleManager.Get().GetMod(AutoCrystalModule.class);
        _autoTrap = (AutoTrap) ModuleManager.Get().GetMod(AutoTrap.class);
        _speed = (SpeedModule) ModuleManager.Get().GetMod(SpeedModule.class);
        _autoCrystalRewrite = (AutoCrystalRewrite) ModuleManager.Get().GetMod(AutoCrystalRewrite.class);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        final String aura = ChatFormatting.GRAY + "KA " + ChatFormatting.WHITE + (_killAura.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String crystal = ChatFormatting.GRAY + "CA " + ChatFormatting.WHITE + ((_autoCrystal.isEnabled() || _autoCrystalRewrite.isEnabled()) ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String autoTrap = ChatFormatting.GRAY + "AT " + ChatFormatting.WHITE + (_autoTrap.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");
        final String speed = ChatFormatting.GRAY + "S " + ChatFormatting.WHITE + (_speed.isEnabled() ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF");

        RenderUtil.drawStringWithShadow(aura, GetX(), GetY(), -1);
        RenderUtil.drawStringWithShadow(crystal, GetX(), GetY() + 12, -1);
        RenderUtil.drawStringWithShadow(autoTrap, GetX(), GetY() + 24, -1);
        RenderUtil.drawStringWithShadow(speed, GetX(), GetY() + 36, -1);

        SetWidth(RenderUtil.getStringWidth(aura));
        SetHeight(RenderUtil.getStringHeight(crystal) + RenderUtil.getStringHeight(aura) + RenderUtil.getStringHeight(autoTrap) + RenderUtil.getStringHeight(speed));
    }
}
