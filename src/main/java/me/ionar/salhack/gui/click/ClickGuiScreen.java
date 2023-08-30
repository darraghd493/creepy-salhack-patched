package me.ionar.salhack.gui.click;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.ionar.salhack.gui.SalGuiScreen;
import me.ionar.salhack.gui.click.component.MenuComponent;
import me.ionar.salhack.gui.click.component.menus.mods.MenuComponentModList;
import me.ionar.salhack.gui.click.component.menus.mods.MenuComponentPresetsList;
import me.ionar.salhack.gui.click.effects.Snow;
import me.ionar.salhack.managers.ImageManager;
import me.ionar.salhack.managers.PresetsManager;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.ui.ClickGuiModule;
import me.ionar.salhack.module.ui.ColorsModule;
import me.ionar.salhack.util.imgs.SalDynamicTexture;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ClickGuiScreen extends SalGuiScreen {
    private final ArrayList<MenuComponent> MenuComponents = new ArrayList<MenuComponent>();
    private final SalDynamicTexture Watermark = ImageManager.Get().GetDynamicTexture("SalHackWatermark");
    private final SalDynamicTexture BlueBlur = ImageManager.Get().GetDynamicTexture("BlueBlur");
    private final ArrayList<Snow> _snowList = new ArrayList<Snow>();

    private float OffsetY = 0;
    private final ClickGuiModule ClickGuiMod;

    public ClickGuiScreen(ClickGuiModule mod1, ColorsModule colors) {
        // COMBAT, EXPLOIT, MOVEMENT, RENDER, WORLD, MISC, HIDDEN, UI
        MenuComponents.add(new MenuComponentModList("Combat", ModuleType.COMBAT, 10, 3, "Shield", colors, mod1));
        MenuComponents.add(new MenuComponentModList("Exploit", ModuleType.EXPLOIT, 120, 3, "skull", colors, mod1));
        // MenuComponents.add(new MenuComponentModList("Hidden", ModuleType.HIDDEN, 320,
        // 3));
        MenuComponents.add(new MenuComponentModList("Misc", ModuleType.MISC, 230, 3, "questionmark", colors, mod1));
        MenuComponents.add(new MenuComponentModList("Movement", ModuleType.MOVEMENT, 340, 3, "Arrow", colors, mod1));
        MenuComponents.add(new MenuComponentModList("Render", ModuleType.RENDER, 450, 3, "Eye", colors, mod1));
        MenuComponents.add(new MenuComponentModList("UI", ModuleType.UI, 560, 3, "mouse", colors, mod1));
        MenuComponents.add(new MenuComponentModList("World", ModuleType.WORLD, 670, 3, "blockimg", colors, mod1));
        //   MenuComponents.add(new MenuComponentModList("Bot", ModuleType.BOT, 780, 3, "robotimg", colors));
        MenuComponents.add(new MenuComponentModList("Schematica", ModuleType.SCHEMATICA, 10, 247, "robotimg", colors, mod1));
        MenuComponents.add(new MenuComponentModList("Chat", ModuleType.CHAT, 120, 203, "Shield", colors, mod1));

        MenuComponentPresetsList presetList = null;

        MenuComponents.add(presetList = new MenuComponentPresetsList("Presets", ModuleType.SCHEMATICA, 120, 170, "robotimg", colors, mod1));

        PresetsManager.Get().InitalizeGUIComponent(presetList);

        ClickGuiMod = mod1;

        /// Load settings
        for (MenuComponent component : MenuComponents) {
            File exists = new File("SalHack/GUI/" + component.GetDisplayName() + ".json");
            if (!exists.exists()) continue;

            try {
                // create Gson instance
                Gson gson = new Gson();

                // create a reader
                Reader reader = Files.newBufferedReader(Paths.get("SalHack/GUI/" + component.GetDisplayName() + ".json"));

                // convert JSON file to map
                Map<?, ?> map = gson.fromJson(reader, Map.class);

                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();

                    if (key.equals("PosX")) component.SetX(Float.parseFloat(value));
                    else if (key.equals("PosY")) component.SetY(Float.parseFloat(value));
                }

                reader.close();
            } catch (Exception e) {

            }
        }

        Random random = new Random();

        for (int i = 0; i < 100; ++i) {
            for (int y = 0; y < 3; ++y) {
                Snow snow = new Snow(25 * i, y * -50, random.nextInt(3) + 1, random.nextInt(2) + 1);
                _snowList.add(snow);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (MenuComponent menu : MenuComponents) {
            if (menu.MouseClicked(mouseX, mouseY, mouseButton, OffsetY)) break;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        for (MenuComponent menu : MenuComponents) {
            menu.MouseReleased(mouseX, mouseY, state);
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (MenuComponent menu : MenuComponents) {
            menu.MouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        }

        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        final ScaledResolution res = new ScaledResolution(mc);

        if (!_snowList.isEmpty() && ClickGuiMod.Snowing.getValue()) {
            _snowList.forEach(snow -> snow.Update(res));
        }

        if (Watermark != null && ClickGuiMod.Watermark.getValue()) {
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();

            mc.renderEngine.bindTexture(Watermark.GetResourceLocation());
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            RenderUtil.drawTexture(0, res.getScaledHeight() - Watermark.GetHeight() - 5, Watermark.GetWidth() / 2, Watermark.GetHeight() / 2, 0, 0, 1, 1);

            GlStateManager.popMatrix();
        }

        GlStateManager.pushMatrix();

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();

        MenuComponent lastHovered = null;

        for (MenuComponent menu : MenuComponents)
            if (menu.Render(mouseX, mouseY, true, AllowsOverflow(), OffsetY)) lastHovered = menu;

        if (lastHovered != null) {
            /// Add to the back of the list for rendering
            MenuComponents.remove(lastHovered);
            MenuComponents.add(lastHovered);
        }

        RenderHelper.enableGUIStandardItemLighting();

        GlStateManager.enableRescaleNormal();
        GlStateManager.popMatrix();

        int scrolling = Mouse.getEventDWheel();

        /// up
        if (scrolling > 0) {
            OffsetY = Math.max(0, OffsetY - 1);
        }
        /// down
        else if (scrolling < 0) {
            OffsetY = Math.min(100, OffsetY + 1);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        for (MenuComponent menu : MenuComponents) {
            menu.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        if (ClickGuiMod.isEnabled()) ClickGuiMod.toggle();

        /// Save Settings

        for (MenuComponent component : MenuComponents) {
            try {
                GsonBuilder builder = new GsonBuilder();

                Gson gson = builder.setPrettyPrinting().create();

                Writer writer = Files.newBufferedWriter(Paths.get("SalHack/GUI/" + component.GetDisplayName() + ".json"));
                Map<String, String> map = new HashMap<>();

                map.put("PosX", String.valueOf(component.GetX()));
                map.put("PosY", String.valueOf(component.GetY()));

                gson.toJson(map, writer);
                writer.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public boolean AllowsOverflow() {
        return ClickGuiMod.AllowOverflow.getValue();
    }

    public void ResetToDefaults() {
        MenuComponents.forEach(comp -> comp.Default());
    }
}
