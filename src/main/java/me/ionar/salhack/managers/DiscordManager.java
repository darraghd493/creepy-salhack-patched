package me.ionar.salhack.managers;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.misc.DiscordRPCModule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

// TODO: look into lib potentially not loading
public class DiscordManager {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final DiscordRPC lib = DiscordRPC.INSTANCE;
    private final long startTime = System.currentTimeMillis() / 1000L; // epoch second
    private DiscordRPCModule _rpcModule = null;
    private volatile ScheduledFuture<?> task;
    private boolean enabled;

    public static DiscordManager Get() {
        return SalHack.GetDiscordManager();
    }

    public void enable() {
        if (task != null) {
            task.cancel(true);
        }

        if (lib == null) {
            System.out.println("Discord RPC lib not loaded. Not enabling Discord RPC.");
            return;
        }

        _rpcModule = (DiscordRPCModule) ModuleManager.Get().GetMod(DiscordRPCModule.class);
        enabled = true;

        String applicationId = "1145889949317615646";
        String steamId = "";
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = (user) -> System.out.println("Ready!");
        DiscordRichPresence presence = new DiscordRichPresence();
        lib.Discord_Initialize(applicationId, handlers, true, steamId);
        lib.Discord_UpdatePresence(presence);
        presence.startTimestamp = startTime;
        presence.largeImageKey = "logo";
        presence.largeImageText = "https://discord.gg/B8Tx5HYYBm Join The SalHack Development Discord!";

        task = executor.scheduleWithFixedDelay(() -> {
            lib.Discord_RunCallbacks();
            presence.details = _rpcModule.generateDetails();
            presence.state = _rpcModule.generateState();
            lib.Discord_UpdatePresence(presence);
        }, 0, 2, TimeUnit.SECONDS);
    }

    public void disable() {
        if (task != null) {
            if (lib != null) {
                lib.Discord_Shutdown();
            } else {
                System.out.println("Discord RPC lib not loaded. Not disabling Discord RPC.");
            }

            task.cancel(true);
            task = null;
        }
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
