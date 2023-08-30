package me.ionar.salhack.module.misc;

import me.ionar.salhack.module.Module;
import me.ionar.salhack.module.Value;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;

public class ChestSwapModule extends Module {
    public final Value<Boolean> PreferElytra = new Value<Boolean>("PreferElytra", new String[]{"Elytra"}, "Prefers elytra when this is toggled with no equipped item", true);
    public final Value<Boolean> Curse = new Value<Boolean>("Curse", new String[]{"Curses"}, "Prevents you from equipping armor with cursed enchantments.", false);

    public ChestSwapModule() {
        super("ChestSwap", new String[]
                {"ElytraOneButton"}, "Will attempt to instantly swap your chestplate with an elytra or vice versa, depending on what is already equipped", "NONE", 0x24DBD4, ModuleType.MISC);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (mc.player == null)
            return;

        ItemStack chestSlot = mc.player.inventoryContainer.getSlot(6).getStack();

        if (chestSlot.isEmpty()) {
            int slot = FindChestItem(PreferElytra.getValue());

            if (!PreferElytra.getValue() && slot == -1)
                slot = FindChestItem(true);

            if (slot != -1) {
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
                mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
            }

            toggle();
            return;
        }

        int slot = FindChestItem(chestSlot.getItem() instanceof ItemArmor);

        if (slot != -1) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 6, 0, ClickType.PICKUP, mc.player);
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slot, 0, ClickType.PICKUP, mc.player);
        }

        toggle();
    }

    private int FindChestItem(boolean elytra) {
        int slot = -1;
        float damage = 0;

        for (int i = 0; i < mc.player.inventoryContainer.getInventory().size(); ++i) {
            /// @see: https://wiki.vg/Inventory, 0 is crafting slot, and 5,6,7,8 are Armor slots
            if (i == 0 || i == 5 || i == 6 || i == 7 || i == 8)
                continue;

            ItemStack s = mc.player.inventoryContainer.getInventory().get(i);
            if (s != null && s.getItem() != Items.AIR) {
                if (s.getItem() instanceof ItemArmor) {
                    final ItemArmor armor = (ItemArmor) s.getItem();
                    if (armor.armorType == EntityEquipmentSlot.CHEST) {
                        final float currentDamage = (armor.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, s));

                        final boolean cursed = Curse.getValue() && (EnchantmentHelper.hasBindingCurse(s));

                        if (currentDamage > damage && !cursed) {
                            damage = currentDamage;
                            slot = i;
                        }
                    }
                } else if (elytra && s.getItem() instanceof ItemElytra)
                    return i;
            }
        }

        return slot;
    }

}
