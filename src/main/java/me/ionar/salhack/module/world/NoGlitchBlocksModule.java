package me.ionar.salhack.module.world;

import me.ionar.salhack.events.player.EventPlayerDestroyBlock;
import me.ionar.salhack.events.world.EventWorldSetBlockState;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;

public class NoGlitchBlocksModule extends Module {
    public final Value<Boolean> Destroy = new Value<Boolean>("Destroy", new String[]
            {"destroy"}, "Syncs Destroying", true);
    public final Value<Boolean> Place = new Value<Boolean>("Place", new String[]
            {"placement"}, "Syncs placement.", true);
    @EventHandler
    private final Listener<EventPlayerDestroyBlock> OnPlayerDestroyBlock = new Listener<>(event ->
    {
        if (!Destroy.getValue())
            return;
        // Wait for server to process this, and send back a packet later.
        event.cancel();
    });

    /*@EventHandler
    private Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof SPacketBlockChange)
        {
            SPacketBlockChange packet = (SPacketBlockChange)event.getPacket();

            SendMessage(String.format("%s %s", packet.getBlockPosition().toString(), packet.getBlockState().toString()));
        }
    });*/
    @EventHandler
    private final Listener<EventWorldSetBlockState> OnSetBlockState = new Listener<>(event ->
    {
        if (!Place.getValue())
            return;
        /**
         * Flag 1 will cause a block update. Flag 2 will send the change to clients. Flag 4 will prevent the block from
         * being re-rendered, if this is a client world. Flag 8 will force any re-renders to run on the main thread instead
         * of the worker pool, if this is a client world and flag 4 is clear. Flag 16 will prevent observers from seeing
         * this change. Flags can be OR-ed
         */
        /// Flag 3 is from the packet
        if (event.Flags != 3)
            event.cancel();
    });

    public NoGlitchBlocksModule() {
        super("NoGlitchBlocks", new String[]
                {"AntiGhostBlocks"}, "Synchronizes client and server communication by canceling clientside destroy/place for blocks", "NONE", -1, ModuleType.WORLD);
    }
}
