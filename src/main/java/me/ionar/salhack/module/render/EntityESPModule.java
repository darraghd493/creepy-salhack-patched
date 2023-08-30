package me.ionar.salhack.module.render;

import me.ionar.salhack.events.entity.EventEntityAdded;
import me.ionar.salhack.events.entity.EventEntityRemoved;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.ValueListeners;
import me.ionar.salhack.util.entity.EntityUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Objects;

import static me.ionar.salhack.util.render.ESPUtil.RenderCSGO;

public class EntityESPModule extends Module {
    public final Value<ESPMode> Mode = new Value<ESPMode>("Mode", new String[]{"ESPMode"}, "Mode of rendering to use for ESP", ESPMode.Shader);

    /// Entities
    public final Value<Boolean> Players = new Value<Boolean>("Players", new String[]{"Players"}, "Highlights players", true);
    public final Value<Boolean> Monsters = new Value<Boolean>("Monsters", new String[]{"Monsters"}, "Highlights Monsters", false);
    public final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[]{"Animals"}, "Highlights Animals", false);
    public final Value<Boolean> Vehicles = new Value<Boolean>("Vehicles", new String[]{"Vehicles"}, "Highlights Vehicles", false);
    public final Value<Boolean> Others = new Value<Boolean>("Others", new String[]{"Others"}, "Highlights Others", false);
    public final Value<Boolean> Items = new Value<Boolean>("Items", new String[]{"Items"}, "Highlights Items", false);
    public final Value<Boolean> Tamed = new Value<Boolean>("Tamed", new String[]{"Tamed"}, "Highlights Tamed", false);
    private final ICamera camera = new Frustum();
    @EventHandler
    private final Listener<RenderEvent> OnRenderEvent = new Listener<>(event ->
    {
        if (mc.getRenderManager() == null || mc.getRenderManager().options == null)
            return;

        GlStateManager.pushMatrix();
        if (Objects.requireNonNull(Mode.getValue()) == ESPMode.CSGO) {
            RenderCSGO(camera, this, event);
            /// Currently broken.
            /*case Outline:
                RenderOutline(camera, event);
                break;*/
        }
        GlStateManager.popMatrix();
    });
    @EventHandler
    private final Listener<EventEntityAdded> OnEntityAdded = new Listener<>(event ->
    {
        if (Mode.getValue() != ESPMode.Shader)
            return;

        boolean setGlowing = event.GetEntity() instanceof EntityPlayer && Players.getValue();

        /// TODO: func this

        if (EntityUtil.isFriendlyMob(event.GetEntity()) && Animals.getValue())
            setGlowing = true;

        if (EntityUtil.isHostileMob(event.GetEntity()) && Monsters.getValue())
            setGlowing = true;

        if (EntityUtil.IsVehicle(event.GetEntity()) && Vehicles.getValue())
            setGlowing = true;

        if (event.GetEntity() instanceof EntityItem && Items.getValue())
            setGlowing = true;

        if (event.GetEntity() instanceof EntityEnderCrystal && Others.getValue())
            setGlowing = true;


        event.GetEntity().setGlowing(setGlowing);
    });
    @EventHandler
    private final Listener<EventEntityRemoved> OnEntityRemove = new Listener<>(event ->
    {
        event.GetEntity().setGlowing(false);
    });

    public EntityESPModule() {
        super("EntityESP", new String[]{""}, "Highlights different kind of storages", "NONE", -1, ModuleType.RENDER);

        Mode.Listener = new ValueListeners() {
            @Override
            public void OnValueChange(Value val) {
                if (mc.world == null)
                    return;

                if (val.getValue() == ESPMode.Outline)
                    SendMessage("Outline is not yet implemented!");

                UpdateShaders();
            }
        };

        ValueListeners listener = new ValueListeners() {
            @Override
            public void OnValueChange(Value val) {
                if (mc.world == null)
                    return;

                if (Mode.getValue() == ESPMode.Shader)
                    UpdateShaders();
            }
        };

        /// Update all of these when the value is changed
        Players.Listener = listener;
        Monsters.Listener = listener;
        Animals.Listener = listener;
        Vehicles.Listener = listener;
        Others.Listener = listener;
        Items.Listener = listener;
        Tamed.Listener = listener;
    }

    private void UpdateShaders() {
        /// Try catch, because this can be accessed from another thread while entities are being added/removed.
        mc.world.loadedEntityList.forEach(entity ->
        {
            try {
                if (entity != null) {
                    boolean setGlowing = false;

                    if (Mode.getValue() == ESPMode.Shader) {
                        /// TODO: func this
                        if (entity instanceof EntityPlayer && Players.getValue())
                            setGlowing = true;

                        if (EntityUtil.isFriendlyMob(entity) && Animals.getValue())
                            setGlowing = true;

                        if (EntityUtil.isHostileMob(entity) && Monsters.getValue())
                            setGlowing = true;

                        if (EntityUtil.IsVehicle(entity) && Vehicles.getValue())
                            setGlowing = true;

                        if (entity instanceof EntityItem && Items.getValue())
                            setGlowing = true;

                        if (entity instanceof EntityEnderCrystal && Others.getValue())
                            setGlowing = true;
                    }

                    entity.setGlowing(setGlowing);
                }
            } catch (Exception e) {
                //       SendMessage(e.toString());
            }
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();

        mc.world.loadedEntityList.forEach(entity ->
        {
            if (entity != null) {
                entity.setGlowing(false);
            }
        });
    }

    private enum ESPMode {
        None,
        Outline,
        CSGO,
        Shader,
    }
}
