package me.ionar.salhack.module.world;

import me.ionar.salhack.events.entity.EventEntityAdded;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.managers.DirectoryManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketChunkData;

import java.io.FileWriter;
import java.io.IOException;

public class StashLoggerModule extends Module {
    public final Value<Integer> ChestNumberToImportantNotify = new Value<Integer>("MaxCount", new String[]
            {"ChestNumberToImportantNotify"},
            "Number of chests to inform you there was probably unnatural gen chests (a base!)", 5, 0, 20, 1);
    public final Value<Boolean> Chests = new Value<Boolean>("Chests", new String[]{""}, "Logs chests.", true);
    public final Value<Boolean> Shulkers = new Value<Boolean>("Shulkers", new String[]{""}, "Logs Shulkers.", true);
    public final Value<Boolean> ChestedAnimals = new Value<Boolean>("Donkeys", new String[]{""}, "Logs chested animals.", true);
    public final Value<Boolean> WriteToFile = new Value<Boolean>("WriteToFile", new String[]{""}, "Writes what this finds to a file.", true);
    private String WriterName = null;
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof SPacketChunkData) {
            final SPacketChunkData packet = (SPacketChunkData) event.getPacket();

            int chestsCount = 0;
            int shulkers = 0;

            for (NBTTagCompound tag : packet.getTileEntityTags()) {
                String id = tag.getString("id");

                if (id.equals("minecraft:chest") && Chests.getValue())
                    ++chestsCount;
                else if (id.equals("minecraft:shulker_box") && Shulkers.getValue())
                    ++shulkers;
            }

            if (chestsCount >= ChestNumberToImportantNotify.getValue())
                SendMessage(String.format("%s chests located at chunk [%s, %s] Dimension: %s", chestsCount, packet.getChunkX() * 16, packet.getChunkZ() * 16, GetDimensionName()), true);
            if (shulkers > 0)
                SendMessage(String.format("%s shulker boxes at [%s, %s] Dimension: %s", shulkers, packet.getChunkX() * 16, packet.getChunkZ() * 16, GetDimensionName()), true);
        }
    });
    @EventHandler
    private final Listener<EventEntityAdded> OnEntityAdded = new Listener<>(event ->
    {
        if (event.GetEntity() instanceof AbstractChestHorse && ChestedAnimals.getValue()) {
            AbstractChestHorse horse = (AbstractChestHorse) event.GetEntity();

            if (horse.hasChest()) {
                SendMessage(String.format("%s chested animal located at [%s, %s] Dimension: %s", horse.getName(), Math.floor(horse.posX), Math.floor(horse.posZ), GetDimensionName()), true);
            }
        }
    });

    public StashLoggerModule() {
        super("StashLogger", new String[]{"SL"}, "Logs chests, chested donkeys, etc on chunk loads", "NONE", -1, ModuleType.WORLD);
    }

    @Override
    public void toggleNoSave() {

    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (WriteToFile.getValue()) {
            String server = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "singleplayer";

            server = server.replaceAll("\\.", "");

            if (server.contains(":"))
                server = server.substring(0, server.indexOf(":"));

            String name = mc.player.getName();

            String file = server + "_" + name + "_" + System.currentTimeMillis();

            try {
                WriterName = DirectoryManager.Get().GetCurrentDirectory() + "/SalHack/StashFinder/" + file + ".txt";
            } catch (IOException e) {
                e.printStackTrace();
            }

            SendMessage("Created the file named: " + file, false);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private void SendMessage(String message, boolean save) {
        if (WriteToFile.getValue() && save) {
            try {
                FileWriter writer = new FileWriter(WriterName, true);
                writer.write(message + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.SendMessage(message);
    }

    private String GetDimensionName() {
        switch (mc.player.dimension) {
            case -1:
                return "Nether";
            case 0:
                return "Overworld";
            case 1:
                return "End";
        }

        return "Aether";
    }
}
