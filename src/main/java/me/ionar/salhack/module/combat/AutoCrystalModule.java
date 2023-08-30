package me.ionar.salhack.module.combat;

import me.ionar.salhack.events.MinecraftEvent.Era;
import me.ionar.salhack.events.client.EventClientTick;
import me.ionar.salhack.events.network.EventNetworkPacketEvent;
import me.ionar.salhack.events.player.EventPlayerMotionUpdate;
import me.ionar.salhack.events.render.RenderEvent;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.misc.AutoMendArmorModule;
import me.ionar.salhack.util.BlockInteractionHelper;
import me.ionar.salhack.util.BlockInteractionHelper.ValidResult;
import me.ionar.salhack.util.CrystalUtils;
import me.ionar.salhack.util.RotationSpoof;
import me.ionar.salhack.util.entity.EntityUtil;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.ionar.salhack.util.render.RenderUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.passive.AbstractChestHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class AutoCrystalModule extends Module {
    /// Values must be static
    public static final Value<Modes> Mode = new Value<Modes>("Mode", new String[]{"M"}, "Mode of updating to use", Modes.ClientTick);
    public static final Value<PlaceModes> PlaceMode = new Value<PlaceModes>("PlaceMode", new String[]{""}, "Automatically place mode", PlaceModes.Nearest);
    public static final Value<DestroyModes> DestroyMode = new Value<DestroyModes>("DestroyMode", new String[]{""}, "Automatically Destroy mode", DestroyModes.Smart);

    public static final Value<Integer> Ticks = new Value<Integer>("Ticks", new String[]{"Ticks"}, "Ticks", 1, 0, 10, 1);

    /// Range
    public static final Value<Float> DestroyDistance = new Value<Float>("DestroyDistance", new String[]{""}, "Destrou crystal range", 4.0f, 0.0f, 5.0f, 0.5f);
    public static final Value<Float> PlaceDistance = new Value<Float>("PlaceDistance", new String[]{""}, "Place range", 4.0f, 0.0f, 5.0f, 0.5f);
    public static final Value<Float> WallsRange = new Value<Float>("WallsRange", new String[]{""}, "Max distance through walls", 3.5f, 0.0f, 5.0f, 0.5f);

    /// Damage
    public static final Value<Float> MinDMG = new Value<Float>("MinDMG", new String[]{""}, "Minimum dmg for placing crystals near target", 4.0f, 0.0f, 20.0f, 1.0f);
    public static final Value<Float> MaxSelfDMG = new Value<Float>("MaxSelfDMG", new String[]{""}, "Max self dmg for breaking crystals that will deal tons of dmg", 4.0f, 0.0f, 20.0f, 1.0f);
    public static final Value<Float> FacePlace = new Value<Float>("FacePlace", new String[]{""}, "Required target health for faceplacing", 8.0f, 0.0f, 20.0f, 0.5f);

    public static final Value<Boolean> NoSuicide = new Value<Boolean>("NoSuicide", new String[]{"NS"}, "Doesn't commit suicide/pop if you are going to take fatal damage from self placed crystal", true);
    public static final Value<Boolean> PauseWhileEating = new Value<Boolean>("PauseWhileEating", new String[]{"PauseWhileEating"}, "Pause while eating", true);
    public static final Value<Boolean> AntiWeakness = new Value<Boolean>("AntiWeakness", new String[]{"AntiWeakness"}, "Uses a tool or sword to hit the crystals", true);
    public static final Value<Boolean> GhostHand = new Value<Boolean>("GhostHand", new String[]{"GhostHand"}, "Allows you to place crystals by spoofing item packets", false);
    public static final Value<Boolean> GhostHandWeakness = new Value<Boolean>("GhostHandWeakness", new String[]{"GhostHandWeakness"}, "Breaks crystals with sword with ghosthand", false);
    public static final Value<Boolean> ChatMsgs = new Value<Boolean>("ChatMsgs", new String[]{"ChatMsgs"}, "Displays ChatMsgs", false);

    /// Targets
    public static final Value<Boolean> Players = new Value<Boolean>("Players", new String[]{"Players"}, "Place on players", true);
    public static final Value<Boolean> Hostile = new Value<Boolean>("Hostile", new String[]{"Hostile"}, "Place on Hostile", false);
    public static final Value<Boolean> Animals = new Value<Boolean>("Animals", new String[]{"Animals"}, "Place on Animals", false);
    public static final Value<Boolean> Tamed = new Value<Boolean>("Tamed", new String[]{"Tamed"}, "Place on Tamed", false);
    public static final Value<Boolean> ResetRotationNoTarget = new Value<Boolean>("ResetRotationNoTarget", new String[]{"ResetRotationNoTarget"}, "ResetRotationNoTarget", false);

    /// More options
    public static final Value<Boolean> Multiplace = new Value<Boolean>("Multiplace", new String[]{"Multiplace"}, "Multiplace", true);
    public static final Value<Boolean> OnlyPlaceWithCrystal = new Value<Boolean>("OnlyPlaceWithCrystal ", new String[]{"OPWC"}, "Only places when you're manually using a crystal in your main or offhand", false);
    public static final Value<Boolean> PlaceObsidianIfNoValidSpots = new Value<Boolean>("PlaceObsidianIfNoValidSpots ", new String[]{"POINVS"}, "Automatically places obsidian if there are no available crystal spots, so you can crystal your opponent", false);
    public static final Value<Boolean> MinHealthPause = new Value<Boolean>("MinHealthPause", new String[]{"MHP"}, "Automatically pauses if you are below RequiredHealth", false);
    public static final Value<Float> RequiredHealth = new Value<Float>("RequiredHealth", new String[]{""}, "RequiredHealth for autocrystal to function, must be above or equal to this amount.", 11.0f, 0.0f, 20.0f, 1.0f);
    public static final Value<Boolean> AutoMultiplace = new Value<Boolean>("AutoMultiplace", new String[]{""}, "Automatically enables/disables multiplace", false);
    public static final Value<Float> HealthBelowAutoMultiplace = new Value<Float>("HealthBelowAutoMultiplace", new String[]{""}, "RequiredHealth for target to be for automatic multiplace toggling.", 11.0f, 0.0f, 20.0f, 1.0f);
    public static final Value<Boolean> PauseIfHittingBlock = new Value<Boolean>("PauseIfHittingBlock", new String[]{""}, "Pauses when your hitting a block with a pickaxe", false);

    public static final Value<Boolean> Render = new Value<Boolean>("Render", new String[]{"Render"}, "Allows for rendering of block placements", true);
    public static final Value<Integer> Red = new Value<Integer>("Red", new String[]{"Red"}, "Red for rendering", 0x33, 0, 255, 5);
    public static final Value<Integer> Green = new Value<Integer>("Green", new String[]{"Green"}, "Green for rendering", 0xFF, 0, 255, 5);
    public static final Value<Integer> Blue = new Value<Integer>("Blue", new String[]{"Blue"}, "Blue for rendering", 0xF3, 0, 255, 5);
    public static final Value<Integer> Alpha = new Value<Integer>("Alpha", new String[]{"Alpha"}, "Alpha for rendering", 0x99, 0, 255, 5);
    /// Variables
    private int m_WaitTicks = 0;
    private final ArrayList<CPacketPlayer.PositionRotation> Packets = new ArrayList<CPacketPlayer.PositionRotation>();
    private int m_SpoofTimerResetTicks = 0;
    private AimbotModule Aimbot;
    private final ICamera camera = new Frustum();
    private EntityLivingBase m_Target = null;
    private final ArrayList<BlockPos> PlacedCrystals = new ArrayList<BlockPos>();
    private SurroundModule Surround = null;
    private AutoTrapFeet AutoTrapFeet = null;
    private AutoMendArmorModule AutoMend = null;
    private SelfTrapModule SelfTrap = null;
    private HoleFillerModule HoleFiller = null;
    private AutoCityModule AutoCity = null;
    @EventHandler
    private final Listener<EventClientTick> OnTick = new Listener<>(event ->
    {
        if (Mode.getValue() != Modes.ClientTick)
            return;

        if (PauseWhileEating.getValue() && PlayerUtil.IsEating()) {
            /// Reset ticks
            m_WaitTicks = 0;
            return;
        }

        if (NeedPause()) {
            /// Reset ticks
            m_WaitTicks = 0;
            /// Reset rotation
            Aimbot.m_RotationSpoof = null;
            return;
        }

        EntityEnderCrystal crystal = mc.world.loadedEntityList.stream()
                .filter(entity -> IsValidCrystal(entity))
                .map(entity -> (EntityEnderCrystal) entity)
                .min(Comparator.comparing(entity -> mc.player.getDistance(entity)))
                .orElse(null);

        int waitValue = Ticks.getValue();

        if (m_WaitTicks < waitValue) {
            ++m_WaitTicks;
            return;
        }

        m_WaitTicks = 0;

        if (m_SpoofTimerResetTicks > 0) {
            --m_SpoofTimerResetTicks;
        }

        if (ResetRotationNoTarget.getValue()) {
            if (m_Target == null && Aimbot.m_RotationSpoof != null)
                Aimbot.m_RotationSpoof = null;
        } else {
            if (m_SpoofTimerResetTicks == 0) {
                m_SpoofTimerResetTicks = 200;

                Aimbot.m_RotationSpoof = null;
            }
        }

        /*if (m_HoleToFill != null)
        {
            HoleFillerModule.FillHole(m_HoleToFill);
            m_HoleToFill = null;
            return;
        }*/

        if (Multiplace.getValue()) {
            if (DestroyMode.getValue() != DestroyModes.None) {
                HandleBreakCrystals(crystal, null);
                //            if (HandleBreakCrystals(crystal))
                //              return;
            }

            try {
                if (PlaceMode.getValue() != PlaceModes.None)
                    HandlePlaceCrystal(null);
            } catch (Exception e) {

            }
        } else {
            try {
                if (!HandleBreakCrystals(crystal, null))
                    HandlePlaceCrystal(null);
            } catch (Exception e) {

            }
        }
    });
    @EventHandler
    private final Listener<EventPlayerMotionUpdate> OnPlayerMotionUpdate = new Listener<>(event ->
    {
        if (event.getEra() != Era.PRE)
            return;

        if (Mode.getValue() != Modes.MotionTick)
            return;

        if (PauseWhileEating.getValue() && PlayerUtil.IsEating()) {
            /// Reset ticks
            m_WaitTicks = 0;
            return;
        }

        if (NeedPause()) {
            /// Reset ticks
            m_WaitTicks = 0;
            /// Reset rotation
            Aimbot.m_RotationSpoof = null;
            return;
        }

        EntityEnderCrystal crystal = mc.world.loadedEntityList.stream()
                .filter(entity -> IsValidCrystal(entity))
                .map(entity -> (EntityEnderCrystal) entity)
                .min(Comparator.comparing(entity -> mc.player.getDistance(entity)))
                .orElse(null);

        int waitValue = Ticks.getValue();

        if (m_WaitTicks < waitValue) {
            ++m_WaitTicks;
            return;
        }

        if (Multiplace.getValue()) {
            boolean result = false;

            if (DestroyMode.getValue() != DestroyModes.None)
                result = !HandleBreakCrystals(crystal, event);

            if (PlaceMode.getValue() != PlaceModes.None) {
                try {
                    final BlockPos pos = HandlePlaceCrystal(event);

                    if (!result && pos != BlockPos.ORIGIN)
                        result = true;
                } catch (Exception e) {

                }
            }

            if (result)
                m_WaitTicks = Ticks.getValue();
        } else {
            if (!HandleBreakCrystals(crystal, event)) {
                try {
                    final BlockPos pos = HandlePlaceCrystal(event);

                    if (pos != BlockPos.ORIGIN)
                        m_WaitTicks = Ticks.getValue();
                } catch (Exception e) {

                }
            }
        }
    });
    @EventHandler
    private final Listener<EventNetworkPacketEvent> PacketEvent = new Listener<>(event ->
    {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();

            if (packet.getCategory() == SoundCategory.BLOCKS
                    && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                for (Entity entity : new ArrayList<Entity>(mc.world.loadedEntityList)) {
                    if (entity instanceof EntityEnderCrystal) {
                        if (entity.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6.0) {
                            entity.setDead();
                        }


                        PlacedCrystals.removeIf(pos -> pos.getDistance((int) packet.getX(), (int) packet.getY(), (int) packet.getZ()) <= 6.0);
                        /*Iterator<BlockPos> itr = PlacedCrystals.iterator();
                        while (itr.hasNext())
                        {
                            BlockPos pos = (BlockPos)itr.next();
                            if (pos.getDistance((int)packet.getX(), (int)packet.getY(), (int)packet.getZ()) <= 6.0)
                                itr.remove();
                        }*/
                    }
                }
            }
        }
    });
    @EventHandler
    private final Listener<RenderEvent> OnRenderEvent = new Listener<>(event ->
    {
        if (mc.getRenderManager() == null || !Render.getValue())
            return;

        ArrayList<BlockPos> placedCrystalsCopy = new ArrayList<BlockPos>(PlacedCrystals);

        for (BlockPos pos : placedCrystalsCopy) {
            if (pos == null)
                continue;

            final AxisAlignedBB bb = new AxisAlignedBB(pos.getX() - mc.getRenderManager().viewerPosX,
                    pos.getY() - mc.getRenderManager().viewerPosY, pos.getZ() - mc.getRenderManager().viewerPosZ,
                    pos.getX() + 1 - mc.getRenderManager().viewerPosX,
                    pos.getY() + (1) - mc.getRenderManager().viewerPosY,
                    pos.getZ() + 1 - mc.getRenderManager().viewerPosZ);

            camera.setPosition(mc.getRenderViewEntity().posX, mc.getRenderViewEntity().posY,
                    mc.getRenderViewEntity().posZ);

            if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX,
                    bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ,
                    bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY,
                    bb.maxZ + mc.getRenderManager().viewerPosZ))) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableDepth();
                GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                glEnable(GL_LINE_SMOOTH);
                glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
                glLineWidth(1.5f);

                int color = (Alpha.getValue() << 24) | (Red.getValue() << 16) | (Green.getValue() << 8) | Blue.getValue();

                RenderUtil.drawBoundingBox(bb, 1.0f, color);
                RenderUtil.drawFilledBox(bb, color);
                glDisable(GL_LINE_SMOOTH);
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    });

    public AutoCrystalModule() {
        super("AutoCrystal", new String[]{"AC"}, "Automatically places and destroys crystals around targets, if they meet the requirements", "NONE", 0x24CADB, ModuleType.COMBAT);
    }

    @Override
    public void SendMessage(String msg) {
        if (ChatMsgs.getValue())
            super.SendMessage(msg);
    }

    /// Overrides
    @Override
    public void onEnable() {
        super.onEnable();

        Packets.clear();

        Aimbot = (AimbotModule) ModuleManager.Get().GetMod(AimbotModule.class);
        Surround = (SurroundModule) ModuleManager.Get().GetMod(SurroundModule.class);
        AutoTrapFeet = (AutoTrapFeet) ModuleManager.Get().GetMod(AutoTrapFeet.class);
        AutoMend = (AutoMendArmorModule) ModuleManager.Get().GetMod(AutoMendArmorModule.class);
        SelfTrap = (SelfTrapModule) ModuleManager.Get().GetMod(SelfTrapModule.class);
        HoleFiller = (HoleFillerModule) ModuleManager.Get().GetMod(HoleFillerModule.class);
        AutoCity = (AutoCityModule) ModuleManager.Get().GetMod(AutoCityModule.class);

        //  if (!Holes.isEnabled())
        //     Holes.toggle();

        if (!Aimbot.isEnabled())
            Aimbot.toggle();

        Aimbot.m_RotationSpoof = null;
        m_WaitTicks = Ticks.getValue();

        PlacedCrystals.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        Packets.clear();
        Aimbot.m_RotationSpoof = null;
        m_WaitTicks = Ticks.getValue();

        PlacedCrystals.clear();
    }

    @Override
    public void toggleNoSave() {

    }

    @Override
    public String getMetaData() {
        String result = m_Target != null ? m_Target.getName() : null;

        if (AutoMultiplace.getValue() && Multiplace.getValue() && result != null)
            result += " Multiplacing";

        return result;
    }

    private boolean IsValidCrystal(Entity entity) {
        if (!(entity instanceof EntityEnderCrystal))
            return false;

        if (entity.getDistance(mc.player) > (!mc.player.canEntityBeSeen(entity) ? WallsRange.getValue() : DestroyDistance.getValue()))
            return false;

        switch (DestroyMode.getValue()) {
            case Always:
                return true;
            case OnlyOwn:
                /// create copy
                for (BlockPos pos : new ArrayList<BlockPos>(PlacedCrystals)) {
                    if (pos != null && pos.getDistance((int) entity.posX, (int) entity.posY, (int) entity.posZ) <= 3.0)
                        return true;
                }
                break;
            case Smart:
                EntityLivingBase target = m_Target != null ? m_Target : GetNearTarget(entity);

                if (target == null)
                    return false;

                float targetDMG = CrystalUtils.calculateDamage(mc.world, entity.posX + 0.5, entity.posY + 1.0, entity.posZ + 0.5, target, 0);
                float selfDMG = CrystalUtils.calculateDamage(mc.world, entity.posX + 0.5, entity.posY + 1.0, entity.posZ + 0.5, mc.player, 0);

                float minDmg = MinDMG.getValue();

                /// FacePlace
                if (target.getHealth() + target.getAbsorptionAmount() <= FacePlace.getValue())
                    minDmg = 1f;

                if (targetDMG > minDmg && selfDMG < MaxSelfDMG.getValue())
                    return true;

                break;
            default:
                break;
        }

        return false;
    }

    private boolean HandleBreakCrystals(EntityEnderCrystal crystal, @Nullable EventPlayerMotionUpdate event) {
        if (crystal != null) {
            final double[] pos = EntityUtil.calculateLookAt(
                    crystal.posX + 0.5,
                    crystal.posY - 0.5,
                    crystal.posZ + 0.5,
                    mc.player);

            if (Mode.getValue() == Modes.ClientTick) {
                Aimbot.m_RotationSpoof = new RotationSpoof((float) pos[0], (float) pos[1]);

                Random rand = new Random(2);

                Aimbot.m_RotationSpoof.Yaw += (rand.nextFloat() / 100);
                Aimbot.m_RotationSpoof.Pitch += (rand.nextFloat() / 100);
            }

            int prevSlot = -1;

            if (AntiWeakness.getValue() && mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                if (mc.player.getHeldItemMainhand() == ItemStack.EMPTY || (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) && !(mc.player.getHeldItemMainhand().getItem() instanceof ItemTool))) {
                    for (int i = 0; i < 9; ++i) {
                        ItemStack stack = mc.player.inventory.getStackInSlot(i);

                        if (stack == ItemStack.EMPTY)
                            continue;

                        if (stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemSword) {
                            prevSlot = mc.player.inventory.currentItem;
                            mc.player.inventory.currentItem = i;
                            mc.playerController.updateController();
                            break;
                        }
                    }
                }
            }

            if (Mode.getValue() == Modes.MotionTick && event != null) ///< event should not null
            {
                event.cancel();

                SpoofRotationsTo((float) pos[0], (float) pos[1]);
            }

            mc.playerController.attackEntity(mc.player, crystal);
            mc.player.swingArm(EnumHand.MAIN_HAND);

            if (GhostHandWeakness.getValue() && prevSlot != -1) {
                mc.player.inventory.currentItem = prevSlot;
                mc.playerController.updateController();
            }

            return true;
        }

        return false;
    }

    private boolean IsValidTarget(Entity entity) {
        if (entity == null)
            return false;

        if (!(entity instanceof EntityLivingBase))
            return false;

        if (FriendManager.Get().IsFriend(entity))
            return false;

        if (entity.isDead || ((EntityLivingBase) entity).getHealth() <= 0.0f)
            return false;

        if (entity.getDistance(mc.player) > 20.0f)
            return false;

        if (entity instanceof EntityPlayer && Players.getValue()) {
            return entity != mc.player;
        }

        if (Hostile.getValue() && EntityUtil.isHostileMob(entity))
            return true;
        if (Animals.getValue() && EntityUtil.isPassive(entity))
            return true;
        return Tamed.getValue() && entity instanceof AbstractChestHorse && ((AbstractChestHorse) entity).isTame();
    }

    private EntityLivingBase GetNearTarget(Entity distanceTarget) {
        return mc.world.loadedEntityList.stream()
                .filter(entity -> IsValidTarget(entity))
                .map(entity -> (EntityLivingBase) entity)
                .min(Comparator.comparing(entity -> distanceTarget.getDistance(entity)))
                .orElse(null);
    }

    private void FindNewTarget() {
        m_Target = GetNearTarget(mc.player);
    }

    private BlockPos HandlePlaceCrystal(@Nullable EventPlayerMotionUpdate event) throws Exception {
        if (OnlyPlaceWithCrystal.getValue()) {
            /// if we don't have crystal in main or offhand, don't place, this was a request from issue #25
            if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL)
                return BlockPos.ORIGIN;
        }

        List<BlockPos> availableBlockPositions = CrystalUtils.findCrystalBlocks(mc.player, PlaceDistance.getValue());

        switch (PlaceMode.getValue()) {
            case Nearest:
                FindNewTarget();
                break;
            case Priority:
                if (m_Target == null || m_Target.getDistance(mc.player) > PlaceDistance.getValue() + 2f || m_Target.isDead || m_Target.getHealth() <= 0.0f) ///< Allow 2 tolerence
                    FindNewTarget();
                break;
            case MostDamage: {
                if (availableBlockPositions.isEmpty()) {
                    FindNewTarget();
                } else {
                    EntityLivingBase target = null;

                    float minDmg = MinDMG.getValue();
                    float maxSelfDmg = MaxSelfDMG.getValue();
                    float dMG = 0.0f;

                    /// Iterate through all players
                    for (EntityPlayer player : mc.world.playerEntities) {
                        if (!IsValidTarget(player))
                            continue;

                        /// Iterate block positions for this entity
                        for (BlockPos pos : availableBlockPositions) {
                            if (player.getDistanceSq(pos) >= 169.0D)
                                continue;

                            float tempDMG = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player, 0);

                            if (tempDMG < minDmg)
                                continue;

                            float selfTempDMG = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, mc.player, 0);

                            if (selfTempDMG > maxSelfDmg)
                                continue;

                            if (WallsRange.getValue() > 0) {
                                if (!PlayerUtil.CanSeeBlock(pos))
                                    if (pos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) > WallsRange.getValue())
                                        continue;
                            }

                            if (tempDMG > dMG) {
                                dMG = tempDMG;
                                target = player;
                            }
                        }
                    }

                    if (target == null)
                        target = GetNearTarget(mc.player);

                    if (m_Target != null && target != m_Target && target != null) {
                        SendMessage(String.format("Found new target %s", target.getName()));
                    }

                    m_Target = target;
                }
                break;
            }
            default:
                break;
        }

        if (availableBlockPositions.isEmpty()) {
            if (PlaceObsidianIfNoValidSpots.getValue() && m_Target != null) {
                int slot = AutoTrapFeet.findStackHotbar(Blocks.OBSIDIAN);

                if (slot != -1) {
                    if (mc.player.inventory.currentItem != slot) {
                        mc.player.inventory.currentItem = slot;
                        mc.playerController.updateController();
                        return BlockPos.ORIGIN;
                    }

                    float range = PlaceDistance.getValue();

                    float targetDMG = 0.0f;
                    float minDmg = MinDMG.getValue();

                    /// FacePlace
                    if (m_Target.getHealth() + m_Target.getAbsorptionAmount() <= FacePlace.getValue())
                        minDmg = 1f;

                    BlockPos targetPos = null;

                    for (BlockPos pos : BlockInteractionHelper.getSphere(PlayerUtil.GetLocalPlayerPosFloored(), PlaceDistance.getValue(), (int) range, false, true, 0)) {
                        ValidResult result = BlockInteractionHelper.valid(pos);

                        if (result != ValidResult.Ok)
                            continue;

                        if (!CrystalUtils.CanPlaceCrystalIfObbyWasAtPos(pos))
                            continue;

                        float tempDMG = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, m_Target, 0);

                        if (tempDMG < minDmg)
                            continue;

                        if (tempDMG >= targetDMG) {
                            targetPos = pos;
                            targetDMG = tempDMG;
                        }
                    }

                    if (targetPos != null) {
                        BlockInteractionHelper.place(targetPos, PlaceDistance.getValue(), true, false); ///< sends a new packet, might be bad for ncp flagging tomany packets..
                        SendMessage(String.format("Tried to place obsidian at %s would deal %s dmg", targetPos, targetDMG));
                    }
                }
            }

            return BlockPos.ORIGIN;
        }


        if (m_Target == null)
            return BlockPos.ORIGIN;

        if (AutoMultiplace.getValue()) {
            if (m_Target.getHealth() + m_Target.getAbsorptionAmount() <= HealthBelowAutoMultiplace.getValue())
                Multiplace.setValue(true);
            else
                Multiplace.setValue(false);
        }

        float minDmg = MinDMG.getValue();
        float maxSelfDmg = MaxSelfDMG.getValue();
        float facePlaceHealth = FacePlace.getValue();

        /// FacePlace
        if (m_Target.getHealth() <= facePlaceHealth)
            minDmg = 1f;

        /// AntiSuicide
        if (NoSuicide.getValue()) {
            while (mc.player.getHealth() + mc.player.getAbsorptionAmount() < maxSelfDmg)
                maxSelfDmg /= 2;
        }

        BlockPos bestPosition = null;
        float dMG = 0.0f;

        /// todo: use this, but we will lose dmg... maybe new option, for LeastDMGToSelf? but seems useless
        float selfDMG = 0.0f;

        for (BlockPos pos : availableBlockPositions) {
            if (m_Target.getDistanceSq(pos) >= 169.0D)
                continue;

            float tempDMG = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, m_Target, 0);

            if (tempDMG < minDmg)
                continue;

            float selfTempDMG = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, mc.player, 0);

            if (selfTempDMG > maxSelfDmg)
                continue;

            if (WallsRange.getValue() > 0) {
                if (!PlayerUtil.CanSeeBlock(pos))
                    if (pos.getDistance((int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ) > WallsRange.getValue())
                        continue;
            }

            if (tempDMG > dMG) {
                dMG = tempDMG;
                selfDMG = selfTempDMG;
                bestPosition = pos;
            }
        }

        if (bestPosition == null)
            return BlockPos.ORIGIN;

        /*for (Hole hole : Holes.GetHoles())
        {
            float holeFillDmg = CrystalUtils.calculateDamage(mc.world, hole.getX() + 0.5, hole.getY() + 1.0, hole.getZ() + 0.5, player, 0);

            if (holeFillDmg > dMG)
            {
                m_HoleToFill = hole;
                dMG = holeFillDmg;
            }
        }

        if (m_HoleToFill != null)
        {
            SalHack.INSTANCE.logChat("Filling the hole at " + m_HoleToFill.toString() + " will deal " + dMG);
           // return;
        }*/

        int prevSlot = -1;


        if (!GhostHand.getValue()) {
            if (SwitchHandToItemIfNeed(Items.END_CRYSTAL))
                return BlockPos.ORIGIN;
        } else {
            if (mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
                for (int i = 0; i < 9; ++i) {
                    ItemStack stack = mc.player.inventory.getStackInSlot(i);

                    if (stack == ItemStack.EMPTY)
                        continue;

                    if (stack.getItem() == Items.END_CRYSTAL) {
                        prevSlot = mc.player.inventory.currentItem;
                        mc.player.inventory.currentItem = i;
                        mc.playerController.updateController();
                    }
                }
            }
        }

        final double[] pos = EntityUtil.calculateLookAt(
                bestPosition.getX() + 0.5,
                bestPosition.getY() - 0.5,
                bestPosition.getZ() + 0.5,
                mc.player);

        if (Mode.getValue() == Modes.ClientTick) {
            Aimbot.m_RotationSpoof = new RotationSpoof((float) pos[0], (float) pos[1]);

            Random rand = new Random(2);

            Aimbot.m_RotationSpoof.Yaw += (rand.nextFloat() / 100);
            Aimbot.m_RotationSpoof.Pitch += (rand.nextFloat() / 100);
        }

        RayTraceResult result = mc.world.rayTraceBlocks(
                new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ),
                new Vec3d(bestPosition.getX() + 0.5, bestPosition.getY() - 0.5,
                        bestPosition.getZ() + 0.5));

        EnumFacing facing;

        if (result == null || result.sideHit == null)
            facing = EnumFacing.UP;
        else
            facing = result.sideHit;

        if (Mode.getValue() == Modes.MotionTick && event != null) ///< event should not null
        {
            event.cancel();

            SpoofRotationsTo((float) pos[0], (float) pos[1]);
        }

        mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(bestPosition, facing,
                mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));
        // mc.playerController.processRightClickBlock(mc.player, mc.world, bestPosition, EnumFacing.UP, new Vec3d(0, 0, 0), EnumHand.MAIN_HAND);
        // SalHack.INSTANCE.logChat(String.format("%s%s DMG and SelfDMG %s %s %S", ChatFormatting.LIGHT_PURPLE, dMG, selfDMG, facing, m_Target.getName()));

        PlacedCrystals.add(bestPosition);

        if (prevSlot != -1 && GhostHand.getValue()) {
            mc.player.inventory.currentItem = prevSlot;
            mc.playerController.updateController();
        }

        return bestPosition;
    }

    private boolean SwitchHandToItemIfNeed(Item item) {
        if (mc.player.getHeldItemMainhand().getItem() == item || mc.player.getHeldItemOffhand().getItem() == item)
            return false;

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack == ItemStack.EMPTY)
                continue;

            if (stack.getItem() == item) {
                mc.player.inventory.currentItem = i;
                mc.playerController.updateController();
                return true;
            }
        }

        return true;
    }

    private void SpoofRotationsTo(float yaw, float pitch) {
        boolean isSprinting = mc.player.isSprinting();

        if (isSprinting != mc.player.serverSprintState) {
            if (isSprinting) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            } else {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            mc.player.serverSprintState = isSprinting;
        }

        boolean isSneaking = mc.player.isSneaking();

        if (isSneaking != mc.player.serverSneakState) {
            if (isSneaking) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            } else {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }

            mc.player.serverSneakState = isSneaking;
        }

        if (PlayerUtil.isCurrentViewEntity()) {

            AxisAlignedBB axisalignedbb = mc.player.getEntityBoundingBox();
            double posXDifference = mc.player.posX - mc.player.lastReportedPosX;
            double posYDifference = axisalignedbb.minY - mc.player.lastReportedPosY;
            double posZDifference = mc.player.posZ - mc.player.lastReportedPosZ;
            double yawDifference = yaw - mc.player.lastReportedYaw;
            double rotationDifference = pitch - mc.player.lastReportedPitch;
            ++mc.player.positionUpdateTicks;
            boolean movedXYZ = posXDifference * posXDifference + posYDifference * posYDifference + posZDifference * posZDifference > 9.0E-4D || mc.player.positionUpdateTicks >= 20;
            boolean movedRotation = yawDifference != 0.0D || rotationDifference != 0.0D;

            if (mc.player.isRiding()) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999.0D, mc.player.motionZ, yaw, pitch, mc.player.onGround));
                movedXYZ = false;
            } else if (movedXYZ && movedRotation) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, axisalignedbb.minY, mc.player.posZ, yaw, pitch, mc.player.onGround));
            } else if (movedXYZ) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, axisalignedbb.minY, mc.player.posZ, mc.player.onGround));
            } else if (movedRotation) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, mc.player.onGround));
            } else if (mc.player.prevOnGround != mc.player.onGround) {
                mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
            }

            if (movedXYZ) {
                mc.player.lastReportedPosX = mc.player.posX;
                mc.player.lastReportedPosY = axisalignedbb.minY;
                mc.player.lastReportedPosZ = mc.player.posZ;
                mc.player.positionUpdateTicks = 0;
            }

            if (movedRotation) {
                mc.player.lastReportedYaw = yaw;
                mc.player.lastReportedPitch = pitch;
            }

            mc.player.prevOnGround = mc.player.onGround;
            mc.player.autoJumpEnabled = mc.player.mc.gameSettings.autoJump;
        }
    }

    public boolean NeedPause() {
        /// We need to pause if we have surround enabled, and don't have obsidian
        if (Surround.isEnabled() && !Surround.IsSurrounded(mc.player) && Surround.HasObsidian()) {
            if (!Surround.ActivateOnlyOnShift.getValue())
                return true;

            if (!mc.gameSettings.keyBindSneak.isKeyDown())
                return true;
        }

        if (AutoTrapFeet.isEnabled() && !AutoTrapFeet.IsCurrentTargetTrapped() && AutoTrapFeet.HasObsidian())
            return true;

        if (AutoMend.isEnabled())
            return true;

        if (SelfTrap.isEnabled() && !SelfTrap.IsSelfTrapped() && Surround.HasObsidian())
            return true;

        if (MinHealthPause.getValue() && (mc.player.getHealth() + mc.player.getAbsorptionAmount()) < RequiredHealth.getValue())
            return true;

        if (HoleFiller.isEnabled() && HoleFiller.IsProcessing())
            return true;

        if (PauseIfHittingBlock.getValue() && mc.playerController.isHittingBlock && mc.player.getHeldItemMainhand().getItem() instanceof ItemTool)
            return true;

        return AutoCity.isEnabled();
    }

    private enum Modes {
        ClientTick,
        MotionTick,
    }

    private enum DestroyModes {
        None,
        Smart,
        Always,
        OnlyOwn,
    }

    private enum PlaceModes {
        None,
        Nearest,
        Priority,
        MostDamage,
    }
}
