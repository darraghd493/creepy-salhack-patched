package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class ArrayListComponent extends HudComponentItem {
    public final Value<Boolean> RainbowVal = new Value<Boolean>("Rainbow", new String[]
            {""}, "Makes a dynamic rainbow", true);
    public final Value<Boolean> NoBackground = new Value<Boolean>("NoBackground", new String[]
            {""}, "NoBackground on arraylist", false);
    private final HashMap<Module, String> m_StaticModuleNames = new HashMap<Module, String>();
    private final Timer ReorderTimer = new Timer();
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);

    public ArrayListComponent() {
        super("ArrayList", 0, 0);
        SetHidden(false);
        ClampLevel = 1;
    }

    public String GenerateModuleDisplayName(final Module mod1) {
        String displayName = mod1.GetArrayListDisplayName();
        if (mod1.getMetaData() != null) {
            displayName = displayName + " " + ChatFormatting.GRAY + "[" + ChatFormatting.GRAY + mod1.getMetaData() + ChatFormatting.GRAY + "]";
        }
        return displayName;
    }

    public String GetStaticModuleNames(final Module mod1) {
        if (!this.m_StaticModuleNames.containsKey(mod1)) {
            this.m_StaticModuleNames.put(mod1, this.GenerateModuleDisplayName(mod1));
        }
        return this.m_StaticModuleNames.get(mod1);
    }

    @Override
    public void SetHidden(final boolean hidden) {
        super.SetHidden(hidden);
        ModuleManager.Get().GetModuleList().forEach(mod1 -> {
            if (mod1 != null && mod1.getType() != Module.ModuleType.HIDDEN && mod1.isEnabled() && !mod1.isHidden()) {
                mod1.RemainingXAnimation = RenderUtil.getStringWidth(mod1.GetFullArrayListDisplayName()) + 10.0f;
                ModuleManager.Get().OnModEnable(mod1);
            }
        });
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        ModuleManager.Get().Update();
        final ArrayList<Module> mods = new ArrayList<Module>();
        for (final Module mod : ModuleManager.Get().GetModuleList()) {
            if (mod != null && mod.getType() != Module.ModuleType.HIDDEN && mod.isEnabled() && !mod.isHidden()) {
                mods.add(mod);
            }
        }
        if (ReorderTimer.passed(1000.0)) {
            ReorderTimer.reset();
            m_StaticModuleNames.clear();
        }
        final Comparator<Module> comparator = (first, second) -> {
            final String firstName = GetStaticModuleNames(first);
            final String secondName = GetStaticModuleNames(second);
            final float dif = RenderUtil.getStringWidth(secondName) - RenderUtil.getStringWidth(firstName);
            return (dif != 0.0f) ? ((int) dif) : secondName.compareTo(firstName);
        };
        mods.sort(comparator);
        float xOffset = 0.0f;
        float yOffset = 1.0f;
        float maxWidth = 0.0f;
        Rainbow.OnRender();
        int i = 0;
        for (final Module mod2 : mods) {
            if (mod2 != null && mod2.getType() != Module.ModuleType.HIDDEN && mod2.isEnabled() && !mod2.isHidden()) {
                final String name = GenerateModuleDisplayName(mod2);
                final float width = RenderUtil.getStringWidth(name);
                if (width >= maxWidth) {
                    maxWidth = width;
                }
                final float stringYHeight = 11.0f;
                final float remainingXOffset = mod2.GetRemainingXArraylistOffset();
                switch (Side) {
                    case 0:
                    case 1: {
                        xOffset = GetWidth() - RenderUtil.getStringWidth(name) + remainingXOffset;
                        break;
                    }
                    case 2:
                    case 3: {
                        xOffset = -remainingXOffset;
                        break;
                    }
                }
                i += 20;
                if (i >= 355) {
                    i = 0;
                }
                switch (Side) {
                    case 0:
                    case 3: {
                        if (!NoBackground.getValue()) {
                            RenderUtil.drawRect(GetX() + xOffset + mod2.GetRemainingXArraylistOffset(), GetY() + yOffset, GetX() + xOffset + RenderUtil.getStringWidth(name) + 10.0f, GetY() + yOffset + (stringYHeight + 1.5f), 1963986960);
                        }
                        RenderUtil.drawStringWithShadow(name, GetX() + xOffset, GetY() + yOffset, RainbowVal.getValue() ? Rainbow.GetRainbowColorAt(i) : mod2.getColor());
                        yOffset += stringYHeight + 1.5f;
                        continue;
                    }
                    case 1:
                    case 2: {
                        if (!NoBackground.getValue()) {
                            RenderUtil.drawRect(GetX() + xOffset + mod2.GetRemainingXArraylistOffset(), GetY() + (GetHeight() - stringYHeight) + yOffset, GetX() + xOffset + RenderUtil.getStringWidth(name) + 10.0f, GetY() + (GetHeight() - stringYHeight) + yOffset + (stringYHeight + 1.5f), 1963986960);
                        }
                        RenderUtil.drawStringWithShadow(name, GetX() + xOffset, GetY() + (GetHeight() - stringYHeight) + yOffset, RainbowVal.getValue() ? Rainbow.GetRainbowColorAt(i) : mod2.getColor());
                        yOffset -= stringYHeight + 1.5f;
                        continue;
                    }
                }
            }
        }
        if (ClampLevel > 0) {
            final ScaledResolution res = new ScaledResolution(mc);
            switch (Side) {
                case 0: {
                    SetX(res.getScaledWidth() - maxWidth + 8.0f);
                    if (ClampLevel == 2) {
                        SetY(Math.max(GetY(), 1.0f));
                        break;
                    }
                    SetY(1.0f);
                    break;
                }
                case 1: {
                    SetX(res.getScaledWidth() - maxWidth + 8.0f);
                    if (ClampLevel == 2) {
                        SetY(Math.min(GetY(), res.getScaledWidth() + yOffset));
                        break;
                    }
                    SetY(res.getScaledWidth() + yOffset);
                    break;
                }
                case 2: {
                    SetX(1.0f);
                    if (ClampLevel == 2) {
                        SetY(Math.min(GetY(), res.getScaledWidth() + yOffset));
                        break;
                    }
                    SetY(res.getScaledWidth() + yOffset);
                    break;
                }
                case 3: {
                    SetX(1.0f);
                    if (ClampLevel == 2) {
                        SetY(Math.max(GetY(), 1.0f));
                        break;
                    }
                    SetY(1.0f);
                    break;
                }
            }
        }
        SetWidth(maxWidth - 10.0f);
        SetHeight(Math.abs(yOffset));
    }
}
