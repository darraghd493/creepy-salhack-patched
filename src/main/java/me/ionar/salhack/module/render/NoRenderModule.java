package me.ionar.salhack.module.render;

import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerIsPotionActive;
import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.events.render.*;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.Timer;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;

import java.util.Iterator;
import java.util.Objects;

public class NoRenderModule extends Module {
    public final Value<NoItemsMode> NoItems = new Value<NoItemsMode>("NoItemsMode", new String[]{"NoItems"}, "Prevents items from being rendered", NoItemsMode.Off);
    public final Value<Boolean> Fire = new Value<Boolean>("Fire", new String[]{"Fire"}, "Doesn't render Fire overlay", true);
    public final Value<Boolean> NoHurtCam = new Value<Boolean>("HurtCamera", new String[]{"NHC"}, "Doesn't render the Hurt camera", true);
    public final Value<Boolean> PumpkinOverlay = new Value<Boolean>("PumpkinOverlay", new String[]{"PO"}, "Doesn't render the pumpkin overlay", false);
    public final Value<Boolean> Blindness = new Value<Boolean>("Blindness", new String[]{"Blindness"}, "Doesn't render the blindness effect", true);
    public final Value<Boolean> TotemAnimation = new Value<Boolean>("TotemAnimation", new String[]{"TotemAnimation"}, "Doesn't render the totem animation", false);
    public final Value<Boolean> Skylight = new Value<Boolean>("Skylight", new String[]{"Skylight"}, "Doesn't render skylight updates", false);
    public final Value<Boolean> SignText = new Value<Boolean>("SignText", new String[]{"SignText"}, "Doesn't render SignText", false);
    public final Value<Boolean> NoArmor = new Value<Boolean>("NoArmor", new String[]{"NoArmor"}, "Doesn't render armor", false);
    public final Value<Boolean> NoArmorPlayers = new Value<Boolean>("NoArmorPlayers", new String[]{"NoArmorPlayers"}, "Use in conjunction with the above mod", false);
    public final Value<Boolean> Maps = new Value<Boolean>("Maps", new String[]{"Maps"}, "Doesn't render maps", false);
    public final Value<Boolean> BossHealth = new Value<Boolean>("BossHealth", new String[]{"WitherNames"}, "Doesn't render wither names, and other boss health", false);
    public final Value<Boolean> EnchantingTable = new Value<Boolean>("EnchantingTable", new String[]{"Enchanting"}, "Doesn't render enchanting table books", false);
    public final Value<Boolean> Beacon = new Value<Boolean>("Beacon", new String[]{"Beacon"}, "Doesn't render beacon beam", false);
    private final Timer timer = new Timer();
    @EventHandler
    private final Listener<EventRenderEntity> OnRenderEntity = new Listener<>(event ->
    {
        if (event.GetEntity() instanceof EntityItem && NoItems.getValue() == NoItemsMode.Hide)
            event.cancel();
    });
    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        if (Objects.requireNonNull(NoItems.getValue()) == NoItemsMode.Remove) {
            if (!timer.passed(5000))
                return;

            timer.reset();

            Iterator<Entity> itr = mc.world.loadedEntityList.iterator();

            while (itr.hasNext()) {
                Entity entity = itr.next();

                if (entity != null) {
                    if (entity instanceof EntityItem)
                        mc.world.removeEntity(entity);
                }
            }
        }
    });
    @EventHandler
    private final Listener<EventRenderHurtCameraEffect> OnHurtCameraEffect = new Listener<>(event ->
    {
        if (NoHurtCam.getValue())
            event.cancel();
    });
    @EventHandler
    private final Listener<RenderBlockOverlayEvent> OnBlockOverlayEvent = new Listener<>(event ->
    {
        if (Fire.getValue() && event.getOverlayType() == OverlayType.FIRE)
            event.setCanceled(true);
        if (PumpkinOverlay.getValue() && event.getOverlayType() == OverlayType.BLOCK)
            event.setCanceled(true);
    });
    @EventHandler
    private final Listener<EventPlayerIsPotionActive> IsPotionActive = new Listener<>(event ->
    {
        if (event.potion == MobEffects.BLINDNESS && Blindness.getValue())
            event.cancel();
    });
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (mc.world == null || mc.player == null)
            return;

        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();

            if (packet.getOpCode() == 35) {
                if (TotemAnimation.getValue())
                    event.cancel();
            }
        }
    });
    @EventHandler
    private final Listener<EventRenderUpdateLightmap> OnSkylightUpdate = new Listener<>(event ->
    {
        if (Skylight.getValue())
            event.cancel();
    });
    
    /*@EventHandler
    private Listener<EventParticleEmitParticleAtEntity> OnEmitParticleAtEntity = new Listener<>(event ->
    {
        if (event.Type == EnumParticleTypes.TOTEM && TotemAnimation.getValue())
            event.cancel();
    });*/
    @EventHandler
    private final Listener<EventRenderSign> OnRenderSign = new Listener<>(event ->
    {
        if (SignText.getValue())
            event.cancel();
    });
    @EventHandler
    private final Listener<EventRenderArmorLayer> OnRenderArmorLayer = new Listener<>(event ->
    {
        if (NoArmor.getValue()) {
            if (!(event.Entity instanceof EntityPlayer) && NoArmorPlayers.getValue())
                return;

            event.cancel();
        }
    });
    @EventHandler
    private final Listener<EventRenderMap> OnRenderMap = new Listener<>(event ->
    {
        if (Maps.getValue())
            event.cancel();
    });
    @EventHandler
    private final Listener<EventRenderBossHealth> OnRenderBossHealth = new Listener<>(event ->
    {
        if (BossHealth.getValue())
            event.cancel();
    });
    @EventHandler
    private final Listener<EventRenderBeacon> onRenderBeacon = new Listener<>(event ->
    {
        if (Beacon.getValue())
            event.cancel();
    });
    @EventHandler
    private final Listener<EventRenderEnchantingTableBook> onEnchantingBook = new Listener<>(event ->
    {
        if (EnchantingTable.getValue())
            event.cancel();
    });

    public NoRenderModule() {
        super("NoRender", new String[]{"NR"}, "Doesn't render certain things, if enabled", "NONE", -1, ModuleType.RENDER);
    }

    public enum NoItemsMode {
        Off,
        Remove,
        Hide,
    }

}
