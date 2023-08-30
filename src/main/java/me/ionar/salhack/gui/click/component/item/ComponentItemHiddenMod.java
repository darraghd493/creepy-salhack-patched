package me.ionar.salhack.gui.click.component.item;

import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;
import me.ionar.salhack.module.Module;

public class ComponentItemHiddenMod extends ComponentItem {
    final Module Mod;

    public ComponentItemHiddenMod(Module mod1, String displayText, String description1, int flags, int state, ComponentItemListener listener, float width1, float height1) {
        super(displayText, description1, flags, state, listener, width1, height1);
        Mod = mod1;
    }

    @Override
    public boolean HasState(int state) {
        if ((state & ComponentItem.Clicked) != 0)
            return Mod.isHidden();

        return super.HasState(state);
    }
}
