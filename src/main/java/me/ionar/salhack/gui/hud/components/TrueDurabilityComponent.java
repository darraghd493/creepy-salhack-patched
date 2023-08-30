package me.ionar.salhack.gui.hud.components;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

public class TrueDurabilityComponent extends HudComponentItem {
    public TrueDurabilityComponent() {
        super("TrueDurability", 2, 260);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        ItemStack stack = mc.player.getHeldItemMainhand();

        if (!stack.isEmpty() && (stack.getItem() instanceof ItemTool || stack.getItem() instanceof ItemArmor || stack.getItem() instanceof ItemSword)) {
            final String durability = ChatFormatting.WHITE + "Durability: " + ChatFormatting.GREEN + (stack.getMaxDamage() - stack.getItemDamage());

            RenderUtil.drawStringWithShadow(durability, GetX(), GetY(), -1);

            SetWidth(RenderUtil.getStringWidth(durability));
            SetHeight(RenderUtil.getStringHeight(durability));
        }
    }
}
