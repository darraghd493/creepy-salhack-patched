package me.ionar.salhack.module.chat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.util.text.TextComponentString;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class ChatModificationsModule extends Module {

    public final Value<TimeModes> TimeMode = new Value<TimeModes>("TimeMode", new String[]
            {"TimeModes", "Time"}, "Time format, 12 hour (NA) or 24 hour (EU).", TimeModes.NA);
    public final Value<Boolean> AntiEZ = new Value<Boolean>("AntiEZ", new String[]{"NoEZ"}, "Prevents EZ from being rendered in chat, very useful for 2b2tpvp", true);
    public final Value<Boolean> NoDiscord = new Value<Boolean>("NoDiscord", new String[]{"NoEZ"}, "Prevents discord from being rendered in chat", true);
    public final Value<Boolean> NameHighlight = new Value<Boolean>("NameHighlight", new String[]{"Highlight"}, "Highlights your name in gold in chat", true);
    public final Value<Integer> ChatLength = new Value<Integer>("ChatLength", new String[]{"ChatLength"}, "ChatLength number for more chat length", 100, 0, 0xFFFFFF, 1000);
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof SPacketChat) {
            final SPacketChat packet = (SPacketChat) event.getPacket();

            if (packet.getChatComponent() instanceof TextComponentString) {
                final TextComponentString component = (TextComponentString) packet.getChatComponent();

                String date = "";

                switch (this.TimeMode.getValue()) {
                    case NA:
                        date = new SimpleDateFormat("h:mm a").format(new Date());
                        break;
                    case EU:
                        date = new SimpleDateFormat("k:mm").format(new Date());
                        break;
                }

                component.text = "\2477[" + date + "]\247r " + component.getText();

                if (component.getFormattedText().contains("> ")) {
                    String text = component.getFormattedText().substring(component.getFormattedText().indexOf("> "));

                    if (text.toLowerCase().contains("ez") && AntiEZ.getValue())
                        event.cancel();

                    if (NoDiscord.getValue() && text.toLowerCase().contains("discord"))
                        event.cancel();

                    if (event.isCancelled())
                        return;
                }

                String text = component.getFormattedText();

                if (NameHighlight.getValue() && mc.player != null) {
                    if (text.toLowerCase().contains(mc.player.getName().toLowerCase())) {
                        text = text.replaceAll("(?i)" + mc.player.getName(), ChatFormatting.GOLD + mc.player.getName() + ChatFormatting.RESET);
                        event.cancel();
                        SalHack.SendMessage(text);
                    }
                }
            }
        }
    });

    public ChatModificationsModule() {
        super("ChatModifications", new String[]
                {"ChatStamp", "ChatStamps"}, "Allows for chat modifications", "NONE", 0xDB2450, ModuleType.CHAT);
    }

    @Override
    public String getMetaData() {
        return this.TimeMode.getValue().toString();
    }

    private enum TimeModes {
        NA, EU
    }
}
