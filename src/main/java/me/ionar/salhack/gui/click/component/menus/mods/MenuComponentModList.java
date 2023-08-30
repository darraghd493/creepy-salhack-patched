package me.ionar.salhack.gui.click.component.menus.mods;

import me.ionar.salhack.gui.click.component.MenuComponent;
import me.ionar.salhack.gui.click.component.item.*;
import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ui.ClickGuiModule;
import me.ionar.salhack.module.ui.ColorsModule;

public class MenuComponentModList extends MenuComponent {

    public MenuComponentModList(String displayName, ModuleType type, float x1, float y1, String image, ColorsModule colors, ClickGuiModule click) {
        super(displayName, x1, y1, 100f, 105f, image, colors, click);

        final float Width = 105f;
        final float Height = 11f;

        for (Module mod : ModuleManager.Get().GetModuleList(type)) {
            ComponentItemListener listener = new ComponentItemListener() {
                @Override
                public void OnEnabled() {
                }

                @Override
                public void OnToggled() {
                    mod.toggle();

                    //  SalHack.INSTANCE.getNotificationManager().addNotification("ClickGUI", "Toggled " + mod.getDisplayName());
                }

                @Override
                public void OnDisabled() {
                }

                @Override
                public void OnHover() {

                }

                @Override
                public void OnMouseEnter() {

                }

                @Override
                public void OnMouseLeave() {

                }
            };

            int flags = ComponentItem.Clickable | ComponentItem.Hoverable | ComponentItem.Tooltip;

            if (!mod.getValueList().isEmpty())
                flags |= ComponentItem.HasValues;

            int state = 0;

            if (mod.isEnabled())
                state |= ComponentItem.Clicked;

            ComponentItem item = new ComponentItemMod(mod, mod.getDisplayName(), mod.getDesc(), flags, state, listener, Width, Height);

            for (Value val : mod.getValueList()) {
                listener = new ComponentItemListener() {
                    @Override
                    public void OnEnabled() {
                    }

                    @Override
                    public void OnToggled() {
                    }

                    @Override
                    public void OnDisabled() {
                    }

                    @Override
                    public void OnHover() {

                    }

                    @Override
                    public void OnMouseEnter() {

                    }

                    @Override
                    public void OnMouseLeave() {

                    }
                };
                ComponentItemValue valItem = new ComponentItemValue(val, val.getName(), val.getDesc(), ComponentItem.Clickable | ComponentItem.Hoverable | ComponentItem.Tooltip, 0, listener, Width, Height);

                item.DropdownItems.add(valItem);
            }

            listener = new ComponentItemListener() {
                @Override
                public void OnEnabled() {
                }

                @Override
                public void OnToggled() {
                    mod.setHidden(!mod.isHidden());
                }

                @Override
                public void OnDisabled() {
                }

                @Override
                public void OnHover() {

                }

                @Override
                public void OnMouseEnter() {

                }

                @Override
                public void OnMouseLeave() {

                }
            };

            ComponentItem hideButton = new ComponentItemHiddenMod(mod, "Hidden", "Hides " + mod.getDisplayName() + " from the arraylist", ComponentItem.Clickable | ComponentItem.Hoverable | ComponentItem.Tooltip | ComponentItem.RectDisplayOnClicked | ComponentItem.DontDisplayClickableHighlight, 0, listener, Width, Height);

            item.DropdownItems.add(hideButton);

            item.DropdownItems.add(new ComponentItemKeybind(mod, "Keybind:" + mod.getDisplayName(), mod.getDesc(), ComponentItem.Clickable | ComponentItem.Hoverable | ComponentItem.Tooltip, 0, null, Width, Height));

            AddItem(item);
        }

    }

}
