package me.ionar.salhack.gui.click.component.item;

import me.ionar.salhack.gui.click.component.listeners.ComponentItemListener;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.render.RenderUtil;
import org.lwjgl.input.Keyboard;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ComponentItemValue extends ComponentItem {
    final Value Val;
    private boolean IsDraggingSlider = false;
    private final Timer timer = new Timer();
    private String DisplayString = "";
    private boolean _isEditingString = false;

    public ComponentItemValue(final Value val, String displayText, String description1, int flags, int state, ComponentItemListener listener, float width1,
                              float height1) {
        super(displayText, description1, flags, state, listener, width1, height1);
        Val = val;

        if (val.getValue() instanceof Number && !(val.getValue() instanceof Enum)) {
            Flags |= ComponentItem.Slider;
            Flags |= ComponentItem.DontDisplayClickableHighlight;
            Flags |= ComponentItem.RectDisplayAlways;

            this.SetCurrentWidth(CalculateXPositionFromValue(val));
        } else if (val.getValue() instanceof Boolean) {
            Flags |= ComponentItem.Boolean;
            Flags |= ComponentItem.RectDisplayOnClicked;
            Flags |= ComponentItem.DontDisplayClickableHighlight;

            if ((Boolean) val.getValue())
                State |= ComponentItem.Clicked;
        } else if (val.getValue() instanceof Enum) {
            Flags |= ComponentItem.Enum;
            Flags |= ComponentItem.DontDisplayClickableHighlight;
            Flags |= ComponentItem.RectDisplayAlways;
        } else if (val.getValue() instanceof String)
            Flags |= ComponentItem.Enum;
    }

    private void SetCurrentWidth(float width1) {
        CurrentWidth = width1;
    }

    @Override
    public void Update() {
    }

    @Override
    public boolean HasState(int state) {
        if ((state & ComponentItem.Clicked) != 0)
            return Val.getValue() instanceof Boolean ? (Boolean) Val.getValue() : true;

        return super.HasState(state);
    }

    public float CalculateXPositionFromValue(final Value val) {
        float minX = GetX();
        float maxX = GetX() + GetWidth();

        if (val.getMax() == null)
            return minX;

        Number val1 = (Number) val.getValue();
        Number max = (Number) val.getMax();

        return (maxX - minX) * (val1.floatValue() / max.floatValue());
    }

    @Override
    public String GetDisplayText() {
        if (Val.getValue() instanceof Boolean) {
            String displayText = Val.getName();

            if (HasState(ComponentItem.Hovered) && RenderUtil.getStringWidth(displayText) > GetWidth() - 3) {
                if (DisplayString == null)
                    DisplayString = Val.getName();

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

        String displayText = Val.getName() + " " + (Val.getValue() == null ? "null" : Val.getValue().toString()) + " ";

        if (HasState(ComponentItem.Hovered) && RenderUtil.getStringWidth(displayText) > GetWidth() - 3) {
            if (DisplayString == null)
                DisplayString = Val.getName() + " " + Val.getValue().toString() + " ";

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
    public void OnMouseClick(int mouseX, int mouseY, int mouseButton) {
        super.OnMouseClick(mouseX, mouseY, mouseButton);

        if (Val.getValue() instanceof Enum)
            Val.setEnumValue(Val.GetNextEnumValue(mouseButton == 1));
        else if (Val.getValue() instanceof String) {
            _isEditingString = !_isEditingString;
            Val.setValue("");
        } else if (Val.getValue() instanceof Boolean) {
            Val.setValue(!(Boolean) Val.getValue());
        } else {
            IsDraggingSlider = !IsDraggingSlider;
        }

        // SalHack.INSTANCE.getNotificationManager().addNotification(Mod.getDisplayName(), "Changed the value of " + Val.getName() + " to " + Val.getValue().toString());
    }

    @Override
    public void OnMouseRelease(int mouseX, int mouseY) {
        if (IsDraggingSlider) {
            IsDraggingSlider = false;
            // SalHack.INSTANCE.getNotificationManager().addNotification(Mod.getDisplayName(), "Changed the value of " + Val.getName() + " to " + Val.getValue().toString());
        }
    }

    @Override
    public void OnMouseMove(float mouseX, float mouseY, float x1, float y1) {
        if (!HasFlag(ComponentItem.Slider))
            return;

        if (!IsDraggingSlider)
            return;

        float x = x1 + GetX();

        if (mouseX >= x && mouseX <= x1 + GetX() + GetWidth())
            x = mouseX;

        if (mouseX > x1 + GetX() + GetWidth())
            x = x1 + GetX() + GetWidth();

        x -= x1;

        SetCurrentWidth(x - GetX());
        // Slider.SetX(x - GetX());

        float pct = (x - GetX()) / GetWidth();

        // stupid hacks below because java sux it shd rly static assert or make compile error instead of crash when it reach this point lol
        // could also fix all values but meh..

        if (Val.getValue().getClass() == Float.class) {
            BigDecimal decimal = new BigDecimal(
                    (this.Val.getMax().getClass() == Float.class ? (Float) this.Val.getMax() : this.Val.getMax().getClass() == Double.class ? (Double) this.Val.getMax() : (Integer) Val.getMax())
                            * pct);

            this.Val.setValue(decimal.setScale(2, RoundingMode.HALF_EVEN).floatValue());
        } else if (Val.getValue().getClass() == Double.class) {
            BigDecimal decimal = new BigDecimal(
                    (this.Val.getMax().getClass() == Double.class ? (Double) this.Val.getMax() : this.Val.getMax().getClass() == Float.class ? (Float) this.Val.getMax() : (Integer) Val.getMax())
                            * pct);

            this.Val.setValue(decimal.setScale(2, RoundingMode.HALF_EVEN).doubleValue());
        } else if (Val.getValue().getClass() == Integer.class)
            this.Val.setValue((int) ((int) this.Val.getMax() * pct));
        // salhack.INSTANCE.logChat("Calculated Pct is " + (x-GetX())/GetWidth());
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (_isEditingString) {
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) || Keyboard.isKeyDown(Keyboard.KEY_RETURN)) {
                _isEditingString = false;
                return;
            }

            String string = (String) Val.getValue();

            if (string == null)
                return;

            if (Keyboard.isKeyDown(Keyboard.KEY_BACK)) {
                if (string.length() > 0)
                    string = string.substring(0, string.length() - 1);
            } else if (Character.isDigit(typedChar) || Character.isLetter(typedChar))
                string += typedChar;

            Val.setValue(string);
        }
    }
}
