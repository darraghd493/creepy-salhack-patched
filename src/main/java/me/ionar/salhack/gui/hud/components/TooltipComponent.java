package me.ionar.salhack.gui.hud.components;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.realmsclient.gui.ChatFormatting;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.FriendManager;
import me.ionar.salhack.util.MathUtil;
import me.ionar.salhack.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.text.DecimalFormat;
import java.util.*;

public final class TooltipComponent extends HudComponentItem {
    final Minecraft mc = Minecraft.getMinecraft();
    private EntityPlayer m_LastPlayer = null;
    private final ArrayList<ItemStack> HotbarGuesser = new ArrayList<ItemStack>();
    private final int[] InternalThreatLevel = new int[4];

    public TooltipComponent() {
        super("Tooltip", 700, 600);
    }

    private void drawCharacter(float posX, float posY, int size, int cursorX, int cursorY, EntityPlayer ent) {
        GameProfile profile = new GameProfile(ent.getUniqueID(), "");

        /*
         * EntityOtherPlayerMP ent = new EntityOtherPlayerMP(mc.world, profile); ent.copyLocationAndAnglesFrom(ent); ent.rotationYaw = ent.rotationYaw; ent.rotationYawHead =
         * ent.rotationYawHead; ent.inventory.copyInventory(ent.inventory);
         */

        final float mouseX = posX - cursorX;
        final float mouseY = posY - size * 1.67F - cursorY;

        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GuiInventory.drawEntityOnScreen((int) posX, (int) posY, size, mouseX, mouseY, ent);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.player == null)
            return;

        EntityPlayer entity = null;

        entity = mc.world.loadedEntityList.stream()
                .filter(entity1 -> entity1 instanceof EntityPlayer && entity1 != mc.player && !entity1.getName().equals(mc.player.getName()) && !FriendManager.Get().IsFriend(entity1))
                .map(entity1 -> (EntityPlayer) entity1).min(Comparator.comparing(c -> mc.player.getDistance(c))).orElse(null);

        if (entity == null)
            return;

        if (m_LastPlayer == null || entity.getName() != m_LastPlayer.getName()) {
            m_LastPlayer = entity;
            HotbarGuesser.clear();
        }

        SetWidth(260);
        SetHeight(120);

        InternalThreatLevel[0] = 2;
        InternalThreatLevel[1] = 2;
        InternalThreatLevel[2] = 2;
        InternalThreatLevel[3] = 2;

        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        RenderUtil.drawRect(GetX(), this.GetY(), GetX() + this.GetWidth(), this.GetY() + this.GetHeight(), 0x75101010); // background
        drawCharacter(GetX() + 25, GetY() + 95, 40, (int) GetX(), (int) GetY(), entity);

        int responseTime = -1;
        try {
            responseTime = (int) MathUtil.clamp(mc.getConnection().getPlayerInfo(entity.getUniqueID()).getResponseTime(), 0, 300);
        } catch (NullPointerException np) {
        }

        RenderUtil.drawStringWithShadow(responseTime + "ms", GetX() + GetWidth() - (RenderUtil.getStringWidth(responseTime + "ms") + 5), GetY() + 5, 0xFFFFFF);

        RenderUtil.drawStringWithShadow(entity.getName(), GetX() + 52, GetY() + 2, 0xFFFFFF);

        DecimalFormat double1 = new DecimalFormat("#.##");

        double distance = mc.player.getDistance(entity);

        String line1 = "Distance: " + double1.format(distance);

        if (!line1.contains("."))
            line1 += ".00";
        else {
            String[] split = line1.split("\\.");

            if (split[1] != null && split[1].length() != 2)
                line1 += 0;
        }

        float theirHealth = entity.getHealth() + entity.getAbsorptionAmount();

        line1 += " | Health: " + double1.format(theirHealth);

        RenderUtil.drawStringWithShadow(line1, GetX() + 52, GetY() + 15, 0xFFFFFF);

        final Iterator<ItemStack> items = entity.getArmorInventoryList().iterator();
        final ArrayList<ItemStack> stacks = new ArrayList<>();

        while (items.hasNext()) {
            final ItemStack stack = items.next();
            if (stack != null && stack.getItem() != Items.AIR) {
                stacks.add(stack);
            }
        }

        Collections.reverse(stacks);

        int x = (int) (GetX() + 50);
        int y = (int) (GetY() + 10);

        int textY = (int) (GetY() + 35);

        int itr = 0;

        ThreatLevels threatLevel = ThreatLevels.None;

        for (ItemStack stack : stacks) {
            if (stack == null)
                continue;

            final Item item = stack.getItem();
            if (item != Items.AIR) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();

                // x += 20;
                y += 22;

                mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
                mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, x, y);

                // int lineY = (int) (GetY() + 55) + itr*18;
                // RenderUtil.drawRect(x, lineY, x+121, lineY+1, 0x99F0FF00);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.popMatrix();

                final List<String> stringsToDraw = Lists.newArrayList();

                if (stack.getEnchantmentTagList() != null) {
                    final NBTTagList tags = stack.getEnchantmentTagList();
                    final ArrayList<NBTTagCompound> enchantmentList = new ArrayList<NBTTagCompound>();
                    for (int i = 0; i < tags.tagCount(); i++) {
                        final NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
                        if (tagCompound != null && Enchantment.getEnchantmentByID(tagCompound.getByte("id")) != null) {
                            final Enchantment enchantment = Enchantment.getEnchantmentByID(tagCompound.getShort("id"));
                            final short lvl = tagCompound.getShort("lvl");
                            if (enchantment != null) {
                                String ench = "";
                                if (enchantment.isCurse()) {
                                    if (enchantment.getTranslatedName(lvl).contains("Vanish"))
                                        ench = ChatFormatting.RED + "Vanishing";
                                    else
                                        ench = ChatFormatting.RED + "Binding";

                                    stringsToDraw.add(ench);
                                    continue;
                                }
                                /*else if (ItemUtil.isIllegalEnchant(enchantment, lvl))
                                {
                                    ench = ChatFormatting.AQUA + enchantment.getTranslatedName(lvl);
                                }*/
                                else {
                                    ench = enchantment.getTranslatedName(lvl);
                                }
                                stringsToDraw.add(ench);
                                /*
                                 * enchantmentList.add(tagCompound);
                                 *
                                 * String[] enchantString = ench.split(" ");
                                 *
                                 * String builtString = "";
                                 *
                                 * boolean ignoreCheck = lvl > 0 && !ench.contains("Mending");
                                 *
                                 * for (int i = 0; i < (ignoreCheck ? enchantString.length - 1 : enchantString.length); ++i) builtString += enchantString[i].substring(0, 1);
                                 *
                                 * if (ignoreCheck) builtString += lvl;
                                 *
                                 * stringsToDraw.add(builtString);
                                 */
                            }
                        }
                    }

                    CompareEnchantsToSelf(itr, enchantmentList);

                    float lastStringWidth = 0.0f;
                    int offsetY = 0;
                    for (int i = 0; i < stringsToDraw.size(); ++i) {
                        String string = stringsToDraw.get(i);

                        if (i != stringsToDraw.size() - 1)
                            string += "   ";

                        if (i > 6)
                            break;

                        int offsetX = (int) (x + (i % 2) * lastStringWidth + 18);
                        offsetY = textY + (i / 2) * 8;

                        lastStringWidth = RenderUtil.getStringWidth(string);

                        RenderUtil.drawStringWithShadow(string, offsetX, offsetY, -1);
                    }

                    textY = y + 23;
                    /*
                     * String enchString = "";
                     *
                     * for (String s : stringsToDraw) enchString += s + " ";
                     *
                     * RenderUtil.drawStringWithShadow(enchString, x+16, y+4, -1);
                     */
                }
            }

            itr++;
        }

        if (entity.getHeldItemMainhand() != null && entity.getHeldItemMainhand().getItem() != Items.AIR) {
            final List<String> stringsToDraw = Lists.newArrayList();

            if (entity.getHeldItemMainhand().getEnchantmentTagList() != null) {
                final NBTTagList tags = entity.getHeldItemMainhand().getEnchantmentTagList();
                for (int i = 0; i < tags.tagCount(); i++) {
                    final NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
                    if (tagCompound != null && Enchantment.getEnchantmentByID(tagCompound.getByte("id")) != null) {
                        final Enchantment enchantment = Enchantment.getEnchantmentByID(tagCompound.getShort("id"));
                        final short lvl = tagCompound.getShort("lvl");
                        if (enchantment != null) {
                            String ench = "";
                            if (enchantment.isCurse()) {
                                if (enchantment.getTranslatedName(lvl).contains("Vanish"))
                                    ench = ChatFormatting.RED + "Vanishing";
                                else
                                    ench = ChatFormatting.RED + "Binding";
                            }
                            /*else if (ItemUtil.isIllegalEnchant(enchantment, lvl))
                            {
                                ench = ChatFormatting.AQUA + enchantment.getTranslatedName(lvl);
                            }*/
                            else {
                                ench = enchantment.getTranslatedName(lvl);
                            }

                            stringsToDraw.add(ench);
                        }
                    }
                }
            }

            if (!stringsToDraw.isEmpty())
                stringsToDraw.sort(Comparator.comparing(String::length).thenComparing(s -> !s.contains("Vanishing")));

            String string = entity.getHeldItemMainhand().getDisplayName();

            if (string.length() > 25)
                string = string.substring(0, 25);

            float x2 = GetX() + GetWidth() - (RenderUtil.getStringWidth(string) + 5);
            float y2 = GetY() + GetHeight() - (6 * stringsToDraw.size()) - 5 - RenderUtil.getStringHeight(string) - 16;

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(entity.getHeldItemMainhand(), (int) x2 - 17, (int) y2 - 5);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, entity.getHeldItemMainhand(), (int) x2 - 17, (int) y2 - 5);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
            RenderUtil.drawStringWithShadow(ChatFormatting.LIGHT_PURPLE + string, x2, y2, 0xFFFFFF);

            int i = 0;
            for (String s : stringsToDraw) {
                if (i > 8)
                    break;

                x2 = GetX() + GetWidth() - (RenderUtil.getStringWidth(s) + 5);
                y2 = GetY() + GetHeight() - 5 - RenderUtil.getStringHeight(s) - (6 * i++) - 16;

                RenderUtil.drawStringWithShadow(s, x2, y2, 0xFFFFFF);
            }
        }

        if (entity.getHeldItemOffhand() != null && entity.getHeldItemOffhand().getItem() != Items.AIR) {
            final List<String> stringsToDraw = Lists.newArrayList();

            String string = entity.getHeldItemOffhand().getDisplayName();

            if (string.length() > 25)
                string = string.substring(0, 25);

            float x2 = GetX() + GetWidth() - (RenderUtil.getStringWidth(string) + 5);
            float y2 = GetY() + GetHeight() - (6 * stringsToDraw.size()) - 5 - RenderUtil.getStringHeight(string) - 3;

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(entity.getHeldItemOffhand(), (int) x2 - 17, (int) y2 - 5);
            mc.getRenderItem().renderItemOverlays(mc.fontRenderer, entity.getHeldItemOffhand(), (int) x2 - 17, (int) y2 - 5);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
            RenderUtil.drawStringWithShadow(ChatFormatting.LIGHT_PURPLE + string, x2, y2, 0xFFFFFF);
        }

        if (entity.getHeldItemMainhand() != ItemStack.EMPTY) {
            ItemStack itemToRemove = null;

            boolean readd = true;

            for (ItemStack prevItem : HotbarGuesser) {
                if (prevItem == ItemStack.EMPTY || prevItem.getItem() == Items.AIR)
                    continue;

                if (prevItem.getItem() == entity.getHeldItemMainhand().getItem()) {
                    if (prevItem.getCount() != entity.getHeldItemMainhand().getCount())
                        itemToRemove = prevItem;
                    else
                        readd = false;

                    break;
                }
            }

            if (itemToRemove != null)
                HotbarGuesser.remove(itemToRemove);
            else if (readd)
                HotbarGuesser.add(entity.getHeldItemMainhand());
        }

        if (HotbarGuesser.size() > 9)
            HotbarGuesser.remove(0);

        {
            int i = 0;
            for (ItemStack itemStack : HotbarGuesser) {
                int offsetX = (int) GetX() + 16 * i++;
                int offsetY = (int) (GetY() + GetHeight()) - 16;
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, offsetX, offsetY);
                mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, offsetX, offsetY, null);
                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.popMatrix();
            }
        }

        /// Render the threat
        /// Compare gear
        int noThreatMatches = 0;
        int matchedThreatMatches = 0;
        int highThreatMatches = 0;

        for (int i = 0; i < 4; ++i) {
            switch (InternalThreatLevel[i]) {
                case 0:
                    ++highThreatMatches;
                    break;
                case 1:
                    ++matchedThreatMatches;
                    break;
                case 2:
                    ++noThreatMatches;
                    break;
            }
        }

        if (highThreatMatches > matchedThreatMatches && highThreatMatches > noThreatMatches) {
            threatLevel = ThreatLevels.High;
        } else if ((matchedThreatMatches > highThreatMatches && matchedThreatMatches > noThreatMatches) || matchedThreatMatches == highThreatMatches) {
            threatLevel = ThreatLevels.Matched;

            float ourHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();

            if (ourHealth >= theirHealth)
                threatLevel = ThreatLevels.Matched;
        } else
            threatLevel = ThreatLevels.None;

        String string = "";

        switch (threatLevel) {
            case None:
                string = ChatFormatting.GREEN + "Kill";
                break;
            case Matched:
                string = ChatFormatting.YELLOW + "Matched";
                break;
            case High:
                string = ChatFormatting.RED + "Threat";
                break;
        }

        RenderUtil.drawStringWithShadow(string, (GetX() + 27) - RenderUtil.getStringWidth(string), GetY() + 5, 0xFFFFFF);
        RenderHelper.disableStandardItemLighting();
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.popMatrix();
    }

    private void CompareEnchantsToSelf(int itr, ArrayList<NBTTagCompound> enchantmentList) {
        ItemStack stack = mc.player.inventory.armorInventory.get(itr);
        if (stack == ItemStack.EMPTY) {
            InternalThreatLevel[itr] = 0;
            return;
        }

        if (stack.getEnchantmentTagList() != null) {
            final NBTTagList tags = stack.getEnchantmentTagList();
            for (int i = 0; i < tags.tagCount(); i++) {
                final NBTTagCompound tagCompound = tags.getCompoundTagAt(i);
                if (tagCompound != null && Enchantment.getEnchantmentByID(tagCompound.getByte("id")) != null) {
                    final Enchantment enchantment = Enchantment.getEnchantmentByID(tagCompound.getShort("id"));
                    final short lvl = tagCompound.getShort("lvl");
                    if (enchantment != null) {
                        if (enchantment.isCurse())
                            continue;

                        for (NBTTagCompound enemyTag : enchantmentList) {
                            if (enemyTag.getShort("id") == tagCompound.getShort("id")) {
                                /// todo: check all enchants
                                short enemyLevel = enemyTag.getShort("lvl");
                                if (enemyLevel == lvl) {
                                    InternalThreatLevel[itr] = 1;
                                    return;
                                } else if (lvl < enemyLevel) {
                                    InternalThreatLevel[itr] = 0;
                                    return;
                                } else {
                                    InternalThreatLevel[itr] = 2;
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }

        InternalThreatLevel[itr] = 0;
    }

    private String ToRomainNumerals(int input) {
        switch (input) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
        }

        return "";
    }

    private enum ThreatLevels {
        None,
        Matched,
        High,
    }
}
