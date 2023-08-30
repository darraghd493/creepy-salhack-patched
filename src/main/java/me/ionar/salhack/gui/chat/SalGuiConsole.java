package me.ionar.salhack.gui.chat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.command.Command;
import me.ionar.salhack.managers.CommandManager;
import me.ionar.salhack.module.ui.ConsoleModule;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SalGuiConsole extends GuiChat {
    private Command CurrentCommand = null;
    private final ConsoleModule Console;
    public SalGuiConsole(ConsoleModule console) {
        Console = console;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.sentHistoryCursor = this.mc.ingameGUI.getChatGUI().getSentMessages().size();

        this.inputField = new SalGuiTextField(2, this.fontRenderer, this.width - 295, 5, 595, 12, true);
        this.inputField.setMaxStringLength(256);
        this.inputField.setEnableBackgroundDrawing(false);
        this.inputField.setFocused(true);
        this.inputField.setText("");
        this.inputField.setCanLoseFocus(false);
        this.tabCompleter = new GuiChat.ChatTabCompleter(this.inputField);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.tabCompleter.resetRequested();

        if (keyCode == 15) {
            this.tabCompleter.complete();
        } else {
            this.tabCompleter.resetDidComplete();
        }

        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
        } else if (keyCode != 28 && keyCode != 156) {
            if (keyCode == 200) {
                this.getSentHistory(-1);
            } else if (keyCode == 208) {
                this.getSentHistory(1);
            } else if (keyCode == 201) {
                this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1);
            } else if (keyCode == 209) {
                this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
            } else {
                this.inputField.textboxKeyTyped(typedChar, keyCode);
            }
        } else {
            String s = this.inputField.getText().trim();

            this.sendChatMessage(s);
        }
    }

    @Override
    public void sendChatMessage(String msg) {
        if (CurrentCommand != null)
            CurrentCommand.ProcessCommand(msg);
    }

    @Override
    public void setWorldAndResolution(Minecraft mc, int width, int height) {
        this.mc = mc;
        this.itemRender = mc.getRenderItem();
        this.fontRenderer = mc.fontRenderer;
        this.width = width / 2;
        this.height = height / 2;
        if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Pre(this, this.buttonList))) {
            this.buttonList.clear();
            this.initGui();
        }
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post(this, this.buttonList));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(width - 300, 0, width + 300, 17, Integer.MIN_VALUE);
        this.inputField.drawTextBox();
        ITextComponent itextcomponent = this.mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (itextcomponent != null && itextcomponent.getStyle().getHoverEvent() != null) {
            this.handleComponentHover(itextcomponent, mouseX, mouseY);
        }

        ArrayList<String> commands = new ArrayList<String>();

        String s = this.inputField.getText();

        if (s.isEmpty()) {
            drawRect(width - 300, 23, width + 300, 40, Integer.MIN_VALUE);
            RenderUtil.drawStringWithShadow(String.format("Type a %scommand%s to get help.", ChatFormatting.GREEN, ChatFormatting.RESET), width - 295, 26, 0xFFFFFF);

            return;
        }

        String[] split = s.split(" ");

        if (split == null || split.length == 0)
            return;

        List<Command> commandsLike = CommandManager.Get().GetCommandsLike(split[0]);

        if (commandsLike == null || commandsLike.isEmpty() || commandsLike.size() == 0) {
            drawRect(width - 300, 23, width + 300, 40, Integer.MIN_VALUE);
            RenderUtil.drawStringWithShadow("No commands found...", width - 295, 26, 0xFF0000);
            return;
        }

        final float divider = 17;

        CurrentCommand = commandsLike.get(0);

        int realItr = 0;

        for (int i = 0; i < commandsLike.size(); ++i) {
            Command command = commandsLike.get(i);

            int color = 0xFFFFFF;

            if (command.GetName().equalsIgnoreCase(split[0])) {
                for (String addon : command.GetChunks()) {
                    String[] addonSplit = addon.split(" ");

                    color = 0xFFFFFF;

                    String toWrite = ChatFormatting.GREEN + command.GetName() + ChatFormatting.RESET + " " + addon;

                    if (addonSplit != null && addonSplit.length > 0 && split.length > 1) {
                        if (addonSplit[0].toLowerCase().startsWith(split[1].toLowerCase())) {
                            toWrite = ChatFormatting.GREEN + command.GetName() + " ";

                            for (int y = 0; y < addonSplit.length; ++y) {
                                if (y == 0)
                                    toWrite += ChatFormatting.GREEN + addonSplit[y] + ChatFormatting.RESET;
                                else
                                    toWrite += " " + addonSplit[y];
                            }

                            drawRect(width - 300, 23 + (int) (realItr * divider), width + 300, 40 + (int) (realItr * divider), Integer.MIN_VALUE);
                            RenderUtil.drawStringWithShadow(toWrite, width - 295, 26 + (realItr++ * divider), color);
                        }
                    } else {
                        drawRect(width - 300, 23 + (int) (realItr * divider), width + 300, 40 + (int) (realItr * divider), Integer.MIN_VALUE);
                        RenderUtil.drawStringWithShadow(toWrite, width - 295, 26 + (realItr++ * divider), color);
                    }
                }

                if (split.length == 1) {
                    color = 0xFFEC00;
                    drawRect(width - 300, 23 + (int) (realItr * divider), width + 300, 40 + (int) (realItr * divider), Integer.MIN_VALUE);
                    RenderUtil.drawStringWithShadow(ChatFormatting.GREEN + command.GetName() + ChatFormatting.RESET + " " + command.GetDescription(), width - 295, 26 + (realItr++ * divider), color);
                }
            } else {
                drawRect(width - 300, 23 + (int) (realItr * divider), width + 300, 40 + (int) (realItr * divider), Integer.MIN_VALUE);
                RenderUtil.drawStringWithShadow(command.GetName(), width - 295, 26 + (realItr++ * divider), color);
            }
        }

        drawRect(2, this.height, this.width, this.height, Integer.MIN_VALUE);

        //super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        if (Console.isEnabled())
            Console.toggle();
    }
}
