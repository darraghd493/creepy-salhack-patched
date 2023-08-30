package me.ionar.salhack.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class SalGuiScreen extends GuiScreen {
    // private NoSlowModule NoSlow = null;

    public static void UpdateRotationPitch(float amount) {
        final Minecraft mc = Minecraft.getMinecraft();

        float newRotation = mc.player.rotationPitch + amount;

        newRotation = Math.max(newRotation, -90.0f);
        newRotation = Math.min(newRotation, 90.0f);

        mc.player.rotationPitch = newRotation;
    }

    public static void UpdateRotationYaw(float amount) {
        final Minecraft mc = Minecraft.getMinecraft();

        float newRotation = mc.player.rotationYaw + amount;

        // newRotation = Math.min(newRotation, -360.0f);
        // newRotation = Math.max(newRotation, 360.0f);

        mc.player.rotationYaw = newRotation;
    }

    private boolean InventoryMoveEnabled() {
        /*if (NoSlow == null)
            NoSlow = (NoSlowModule)SalHack.INSTANCE.getModuleManager().find(NoSlowModule.class);

        return NoSlow != null && NoSlow.InventoryMove.getValue();*/
        return true;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (!InventoryMoveEnabled())
            return;

        if (Keyboard.isKeyDown(200)) {
            UpdateRotationPitch(-2.5f);
        }
        if (Keyboard.isKeyDown(208)) {
            UpdateRotationPitch(2.5f);
        }
        if (Keyboard.isKeyDown(205)) {
            UpdateRotationYaw(2.5f);
        }

        if (Keyboard.isKeyDown(203)) {
            UpdateRotationYaw(-2.5f);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }
}
