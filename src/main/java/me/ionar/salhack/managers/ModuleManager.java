package me.ionar.salhack.managers;

import me.ionar.salhack.SalHackMod;
import me.ionar.salhack.main.SalHack;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Module.ModuleType;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.module.chat.ChatModificationsModule;
import me.ionar.salhack.module.chat.MessageModifierModule;
import me.ionar.salhack.module.chat.PopBobSexDupeModule;
import me.ionar.salhack.module.combat.*;
import me.ionar.salhack.module.exploit.*;
import me.ionar.salhack.module.misc.*;
import me.ionar.salhack.module.movement.*;
import me.ionar.salhack.module.render.*;
import me.ionar.salhack.module.schematica.PrinterBypassModule;
import me.ionar.salhack.module.schematica.PrinterModule;
import me.ionar.salhack.module.ui.*;
import me.ionar.salhack.module.world.*;
import me.ionar.salhack.preset.Preset;
import me.ionar.salhack.util.ReflectionUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {
    public ArrayList<Module> Mods = new ArrayList<Module>();
    private ArrayList<Module> ArrayListAnimations = new ArrayList<Module>();
    private KeybindsModule Keybinds = null;
    public ModuleManager() {
    }

    public static ModuleManager Get() {
        return SalHack.GetModuleManager();
    }

    public void Init() {
        /// Combat
        Add(new AimbotModule());
        Add(new AntiBots());
        Add(new AntiCityBossModule());
        Add(new Auto32kModule());
        Add(new AutoArmorModule());
        Add(new AutoCityModule());
        Add(new AutoCrystalModule());
        Add(new AutoCrystalRewrite());
        Add(new AutoTotemModule());
        Add(new AutoTrap());
        Add(new AutoTrapFeet());
        Add(new BowSpamModule());
        Add(new CriticalsModule());
        Add(new HoleFillerModule());
        Add(new KillAuraModule());
        Add(new MiddleClickPearlModule());
        Add(new OldAutoCrystalRewrite());
        Add(new SelfTrapModule());
        Add(new SurroundModule());
        Add(new VelocityModule());

        /// Exploit
        Add(new CrashExploitModule());
        Add(new EntityDesyncModule());
        Add(new GhostModule());
        Add(new LiquidInteractModule());
        Add(new MountBypassModule());
        Add(new NoInteractModule());
        Add(new NoMiningTrace());
        Add(new NewChunksModule());
        Add(new PacketCancellerModule());
        Add(new PacketFlyModule());
        Add(new PortalGodModeModule());

        /// Misc
        Add(new AntiAFKModule());
        Add(new AutoDuperModule());
        Add(new AutoEatModule());
        Add(new AutoMendArmorModule());
        Add(new AutoMountModule());
        Add(new AutoReconnectModule());
        Add(new AutoRespawnModule());
        Add(new AutoSignModule());
        Add(new AutoTameModule());
        Add(new BuildHeightModule());
        Add(new ChatNotifierModule());
        Add(new ChestStealerModule());
        Add(new ChestSwapModule());
        Add(new DiscordRPCModule());
        Add(new FakePlayer());
        Add(new FriendsModule());
        Add(new GlobalLocationModule());
        Add(new HotbarCacheModule());
        Add(new ManualDupeModule());
        Add(new MiddleClickFriendsModule());
        Add(new StopWatchModule());
        Add(new TotemPopNotifierModule());
        Add(new VisualRangeModule());
        Add(new XCarryModule());

        /// Movement
        Add(new AntiLevitationModule());
        Add(new AutoWalkModule());
        Add(new BlinkModule());
        Add(new ElytraFlyModule());
        Add(new EntityControlModule());
        Add(new EntitySpeedModule());
        Add(new FlightModule());
        Add(new GlideModule());
        Add(new HorseJump());
        Add(new JesusModule());
        Add(new NoFallModule());
        Add(new NoRotateModule());
        Add(new NoSlowModule());
        Add(new ParkourJump());
        Add(new SafeWalkModule());
        Add(new SneakModule());
        Add(new SpeedModule());
        Add(new SprintModule());
        Add(new StepModule());
        Add(new YawModule());

        /// Render
        Add(new AntiFog());
        Add(new BlockHighlightModule());
        Add(new BreakHighlightModule());
        Add(new BrightnessModule());
        Add(new CityESPModule());
        Add(new EntityESPModule());
        Add(new FreecamModule());
        Add(new HoleESPModule());
        Add(new MobOwnerModule());
        Add(new NametagsModule());
        Add(new NoRenderModule());
        Add(new ShulkerPreviewModule());
        Add(new SkeletonModule());
        Add(new SmallShield());
        Add(new StorageESPModule());
        Add(new TracersModule());
        Add(new TrajectoriesModule());
        Add(new ViewClipModule());
        Add(new VoidESPModule());
        Add(new WaypointsModule());

        /// UI
        Add(new ChestModule());
        Add(new DupeModule());

        Add(new ColorsModule());
        Add(new ConsoleModule());
        Add(new ClickGuiModule());
        Add(new HudEditorModule());
        Add(new HudModule());
        Add(Keybinds = new KeybindsModule());
        Add(new ReliantChatModule());

        /// World
        Add(new AutoBuilderModule());
        Add(new AutoHighwayBuilder());
        Add(new AutoNameTagModule());
        Add(new AutoToolModule());
        Add(new AutoTunnelModule());
        Add(new AutoWitherModule());
        Add(new CoordsSpooferModule());
        Add(new EnderChestFarmer());
        Add(new FastPlaceModule());
        Add(new NoGlitchBlocksModule());
        Add(new NoWeatherModule());
        Add(new NukerModule());
        Add(new ScaffoldModule());
        Add(new SpeedyGonzales());
        Add(new StashFinderModule());
        Add(new StashLoggerModule());
        Add(new TimerModule());
        Add(new TorchAnnihilatorModule());

        /// Schematica
        Add(new PrinterModule());
        Add(new PrinterBypassModule());

        // Chat
        Add(new ChatModificationsModule());
        Add(new MessageModifierModule());
        Add(new PopBobSexDupeModule());

        LoadExternalModules();

        Mods.sort((mod1, mod2) -> mod1.getDisplayName().compareTo(mod2.getDisplayName()));

        final Preset preset = PresetsManager.Get().getActivePreset();

        Mods.forEach(mod ->
        {
            preset.initValuesForMod(mod);
        });

        Mods.forEach(mod ->
        {
            mod.init();
        });
    }

    public void Add(Module mod) {
        try {
            for (Field field : mod.getClass().getDeclaredFields()) {
                if (Value.class.isAssignableFrom(field.getType())) {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    final Value val = (Value) field.get(mod);
                    val.InitalizeMod(mod);
                    mod.getValueList().add(val);
                }
            }
            Mods.add(mod);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final List<Module> GetModuleList(ModuleType type) {
        List<Module> list = new ArrayList<>();
        for (Module module : Mods) {
            if (module.getType().equals(type)) {
                list.add(module);
            }
        }
        // Organize alphabetically
        list.sort(Comparator.comparing(Module::getDisplayName));

        return list;
    }

    public final List<Module> GetModuleList() {
        return Mods;
    }

    public void OnKeyPress(String string) {
        if (string == null || string.isEmpty() || string.equalsIgnoreCase("NONE"))
            return;

        Mods.forEach(mod1 ->
        {
            if (mod1.IsKeyPressed(string)) {
                mod1.toggle();
            }
        });
    }

    public Module GetMod(Class class1) {
        /*Mods.forEach(mod1 ->
        {
           if (mod1.getClass() == class)
               return mod1;
        });*/

        for (Module mod : Mods) {
            if (mod.getClass() == class1)
                return mod;
        }

        SalHackMod.log.error("Could not find the class " + class1.getName() + " in Mods list");
        return null;
    }

    public Module GetModLike(String string) {
        for (Module mod : Mods) {
            if (mod.GetArrayListDisplayName().toLowerCase().startsWith(string.toLowerCase()))
                return mod;
        }

        return null;
    }

    public void OnModEnable(Module mod1) {
        ArrayListAnimations.remove(mod1);
        ArrayListAnimations.add(mod1);

        final Comparator<Module> comparator = (first, second) ->
        {
            final String firstName = first.GetFullArrayListDisplayName();
            final String secondName = second.GetFullArrayListDisplayName();
            final float dif = RenderUtil.getStringWidth(secondName) - RenderUtil.getStringWidth(firstName);
            return dif != 0 ? (int) dif : secondName.compareTo(firstName);
        };

        ArrayListAnimations = (ArrayList<Module>) ArrayListAnimations.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    public void Update() {
        if (ArrayListAnimations.isEmpty())
            return;

        Module mod = ArrayListAnimations.get(0);

        if ((mod.RemainingXAnimation -= (RenderUtil.getStringWidth(mod.GetFullArrayListDisplayName()) / 10)) <= 0) {
            ArrayListAnimations.remove(mod);
            mod.RemainingXAnimation = 0;
        }
    }

    public boolean IgnoreStrictKeybinds() {
        if (GuiScreen.isAltKeyDown() && !Keybinds.Alt.getValue())
            return true;
        if (GuiScreen.isCtrlKeyDown() && !Keybinds.Ctrl.getValue())
            return true;
        return GuiScreen.isShiftKeyDown() && !Keybinds.Shift.getValue();
    }

    public void LoadExternalModules() {
        // from seppuku
        try {
            final File dir = new File("SalHack/CustomMods");

            for (Class newClass : ReflectionUtil.getClassesEx(dir.getPath())) {
                if (newClass == null)
                    continue;

                // if we have found a class and the class inherits "Module"
                if (Module.class.isAssignableFrom(newClass)) {
                    //create a new instance of the class
                    final Module module = (Module) newClass.newInstance();

                    if (module != null) {
                        // initialize the modules
                        Add(module);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
