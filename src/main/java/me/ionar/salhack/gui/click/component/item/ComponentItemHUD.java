package me.ionar.salhack.gui.click.component.item;

import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.util.render.RenderUtil;

public class ComponentItemHUD extends ComponentItem {
    final HudComponentItem Mod;

    public ComponentItemHUD(HudComponentItem mod1, String displayText, String description1, int flags, int state, ComponentItemListener listener, float width1, float height1) {
        super(displayText, description1, flags, state, listener, width1, height1);
        Mod = mod1;
    }

    @Override
    public String GetDisplayText() {
        String displayText = Mod.GetDisplayName();

        float width = RenderUtil.getStringWidth(displayText);

        while (width > GetWidth()) {
            width = RenderUtil.getStringWidth(displayText);
            displayText = displayText.substring(0, displayText.length() - 1);
        }

        return displayText;
    }

    @Override
    public String GetDescription() {
        return "";
    }

    @Override
    public void Update() {
    }

    @Override
    public boolean HasState(int state) {
        if ((state & ComponentItem.Clicked) != 0)
            return !Mod.IsHidden();

        return super.HasState(state);
    }
}
