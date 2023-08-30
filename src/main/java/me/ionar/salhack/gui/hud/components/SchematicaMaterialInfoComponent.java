package me.ionar.salhack.gui.hud.components;

import com.github.lunatrius.core.client.gui.GuiHelper;
import com.github.lunatrius.schematica.client.printer.SchematicPrinter;
import com.github.lunatrius.schematica.client.util.BlockList;
import com.github.lunatrius.schematica.util.ItemStackSortType;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.ModuleManager;
import me.ionar.salhack.module.ui.HudModule;
import me.ionar.salhack.util.colors.SalRainbowUtil;
import me.ionar.salhack.util.render.RenderUtil;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class SchematicaMaterialInfoComponent extends HudComponentItem {
    private final HudModule hud = (HudModule) ModuleManager.Get().GetMod(HudModule.class);
    private final SalRainbowUtil Rainbow = new SalRainbowUtil(9);
    private final int i = 0;
    public SchematicaMaterialInfoComponent() {
        super("SchematicaMaterialInfo", 300, 300);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        GL11.glPushMatrix();

        RenderUtil.drawRect(GetX(), GetY(), GetX() + GetWidth(), GetY() + GetHeight(), 0x75101010);

        if (SchematicPrinter.INSTANCE.getSchematic() == null) {
            final String string = "No Schematic loaded";

            Rainbow.OnRender();
            RenderUtil.drawStringWithShadow(string, GetX(), GetY(), hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1);
            SetWidth(RenderUtil.getStringWidth(string));
            SetHeight(RenderUtil.getStringHeight(string));
            GL11.glPopMatrix();
            return;
        }

        final List<BlockList.WrappedItemStack> blockList = new BlockList().getList(mc.player, SchematicPrinter.INSTANCE.getSchematic(), mc.world);

        ItemStackSortType.fromString("SIZE_DESC").sort(blockList);

        float height = 0;
        float maxWidth = 0;

        for (BlockList.WrappedItemStack stack : blockList) {
            String string = String.format("%s: %s", stack.getItemStackDisplayName(), stack.getFormattedAmount(), stack.getFormattedAmount());

            GuiHelper.drawItemStack(stack.itemStack, (int) GetX(), (int) (GetY() + height));
            Rainbow.OnRender();
            float width = RenderUtil.drawStringWithShadow(string, GetX() + 20, GetY() + height + 4, hud.Rainbow.getValue() ? Rainbow.GetRainbowColorAt(Rainbow.getRainbowColorNumber()) : -1) + 22;

            if (width >= maxWidth)
                maxWidth = width;

            height += 16;
        }

        SetWidth(maxWidth);
        SetHeight(height);

        GL11.glPopMatrix();
    }

}
