package me.ionar.salhack.module.misc;

import me.ionar.salhack.events.player.EventPlayerUpdate;
import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import me.ionar.salhack.util.entity.PlayerUtil;
import me.zero.alpine.fork.listener.EventHandler;
import me.zero.alpine.fork.listener.Listener;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class AutoEatModule extends Module {
    public final Value<Float> HealthToEatAt = new Value<Float>("HealthToEatAt", new String[]{"Health"}, "Will eat gaps at required health", 15.0f, 0.0f, 36.0f, 3.0f);
    public final Value<Float> RequiredHunger = new Value<Float>("Hunger", new String[]{"Hunger"}, "Required hunger to eat", 18.0f, 0.0f, 20.0f, 1.0f);
    private boolean m_WasEating = false;
    @EventHandler
    private final Listener<EventPlayerUpdate> OnPlayerUpdate = new Listener<>(event ->
    {
        float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();

        if (HealthToEatAt.getValue() >= health && !PlayerUtil.IsEating()) {
            if (mc.player.getHeldItemMainhand().getItem() != Items.GOLDEN_APPLE) {
                for (int i = 0; i < 9; ++i) {
                    if (mc.player.inventory.getStackInSlot(i).isEmpty() || mc.player.inventory.getStackInSlot(i).getItem() != Items.GOLDEN_APPLE)
                        continue;

                    mc.player.inventory.currentItem = i;
                    mc.playerController.updateController();
                    break;
                }

                if (mc.currentScreen == null)
                    mc.gameSettings.keyBindUseItem.pressed = true;
                else
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                m_WasEating = true;
            }
            return;
        }

        if (!PlayerUtil.IsEating() && RequiredHunger.getValue() >= mc.player.getFoodStats().getFoodLevel()) {
            boolean canEat = false;

            for (int i = 0; i < 9; ++i) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);

                if (mc.player.inventory.getStackInSlot(i).isEmpty())
                    continue;

                if (stack.getItem() instanceof ItemFood) {
                    canEat = true;
                    mc.player.inventory.currentItem = i;
                    mc.playerController.updateController();
                    break;
                }
            }

            if (canEat) {
                if (mc.currentScreen == null)
                    mc.gameSettings.keyBindUseItem.pressed = true;
                else
                    mc.playerController.processRightClick(mc.player, mc.world, EnumHand.MAIN_HAND);

                m_WasEating = true;
            }
        }

        if (m_WasEating) {
            m_WasEating = false;
            mc.gameSettings.keyBindUseItem.pressed = false;
        }
    });

    public AutoEatModule() {
        super("AutoEat", new String[]{"Eat"}, "Automatically eats food, depending on hunger, or health", "NONE", 0xFFFB11, ModuleType.MISC);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (m_WasEating) {
            m_WasEating = false;
            mc.gameSettings.keyBindUseItem.pressed = false;
        }
    }
}
