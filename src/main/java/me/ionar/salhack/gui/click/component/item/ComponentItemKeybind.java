package me.ionar.salhack.gui.click.component.item;

import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

public class ComponentItemKeybind extends ComponentItem {
    final Module Mod;
    public boolean Listening = false;
    private String LastKey = "";
    private final Timer timer = new Timer();
    private String DisplayString = "";

    public ComponentItemKeybind(Module mod1, String displayText, String description1, int flags, int state, ComponentItemListener listener, float width1, float height1) {
        super(displayText, description1, flags, state, listener, width1, height1);
        Mod = mod1;

        Flags |= ComponentItem.RectDisplayAlways;
    }

    @Override
    public String GetDisplayText() {
        if (Listening)
            return "Press a Key...";

        String displayText = "Keybind " + Mod.getKey();

        if (HasState(ComponentItem.Hovered) && RenderUtil.getStringWidth(displayText) > GetWidth() - 3) {
            if (DisplayString == null)
                DisplayString = "Keybind " + Mod.getKey() + " ";

            displayText = DisplayString;
            float width = RenderUtil.getStringWidth(displayText);

            while (width > GetWidth() - 3) {
                width = RenderUtil.getStringWidth(displayText);
                displayText = displayText.substring(0, displayText.length() - 1);
            }

            if (timer.passed(75) && DisplayString.length() > 0) {
                String firstChar = String.valueOf(DisplayString.charAt(0));

                DisplayString = DisplayString.substring(1) + firstChar;

                timer.reset();
            }

            return displayText;
        } else
            DisplayString = null;

        float width = RenderUtil.getStringWidth(displayText);

        while (width > GetWidth() - 3) {
            width = RenderUtil.getStringWidth(displayText);
            displayText = displayText.substring(0, displayText.length() - 1);
        }

        return displayText;
    }

    @Override
    public String GetDescription() {
        return "Sets the key of the Module: " + Mod.getDisplayName();
    }

    @Override
    public void OnMouseClick(int mouseX, int mouseY, int mouseButton) {
        super.OnMouseClick(mouseX, mouseY, mouseButton);

        LastKey = "";

        if (mouseButton == 0)
            Listening = !Listening;
        else if (mouseButton == 1)
            Listening = false;
        else if (mouseButton == 2) {
            Mod.setKey("NONE");
            SalHack.SendMessage("Unbinded the module: " + Mod.getDisplayName());
            Listening = false;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (Listening) {
            String key = String.valueOf(Keyboard.getKeyName(keyCode)).toUpperCase();

            if (key.length() < 1) {
                Listening = false;
                return;
            }

            if (key.equals("END") || key.equals("BACK") || key.equals("DELETE")) {
                key = "NONE";
            }

            if (!key.equals("NONE") && !key.contains("CONTROL") && !key.contains("SHIFT") && !key.contains("MENU")) {
                if (GuiScreen.isAltKeyDown())
                    key = (Keyboard.isKeyDown(56) ? "LMENU" : "RMENU") + " + " + key;
                else if (GuiScreen.isCtrlKeyDown()) {
                    String ctrlKey = "";

                    if (Minecraft.IS_RUNNING_ON_MAC)
                        ctrlKey = Keyboard.isKeyDown(219) ? "LCONTROL" : "RCONTROL";
                    else
                        ctrlKey = Keyboard.isKeyDown(29) ? "LCONTROL" : "RCONTROL";

                    key = ctrlKey + " + " + key;
                } else if (GuiScreen.isShiftKeyDown())
                    key = (Keyboard.isKeyDown(42) ? "LSHIFT" : "RSHIFT") + " + " + key;
            }

            LastKey = key;
        }
    }

    @Override
    public void Update() {
        if (!Keyboard.getEventKeyState() && Listening && LastKey != "") {
            Mod.setKey(LastKey);
            SalHack.SendMessage("Set the key of " + Mod.getDisplayName() + " to " + LastKey);
            Listening = false;
        }
    }
}
