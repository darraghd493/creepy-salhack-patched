package me.ionar.salhack.gui.hud.components;

import me.ionar.salhack.gui.hud.GuiHudEditor;
import me.ionar.salhack.gui.hud.HudComponentItem;
import me.ionar.salhack.managers.NotificationManager;
import me.ionar.salhack.managers.NotificationManager.Notification;
import me.ionar.salhack.util.render.RenderUtil;

import java.util.Iterator;

public class NotificationComponent extends HudComponentItem {
    public NotificationComponent() {
        super("Notifications", 500, 500);
        SetHidden(false);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);

        if (mc.currentScreen instanceof GuiHudEditor) {
            if (NotificationManager.Get().Notifications.isEmpty()) {
                final String placeholder = "Notifications";
                SetWidth(RenderUtil.getStringWidth(placeholder));
                SetHeight(RenderUtil.getStringHeight(placeholder));
                RenderUtil.drawStringWithShadow(placeholder, GetX(), GetY(), 0xFFFFFF);
                return;
            }
        }

        Iterator<Notification> itr = NotificationManager.Get().Notifications.iterator();

        float y = GetY();
        float maxWidth = 0f;

        while (itr.hasNext()) {
            Notification notification = itr.next();

            if (notification.IsDecayed())
                NotificationManager.Get().Notifications.remove(notification);

            //notification.OnRender();

            float width = RenderUtil.getStringWidth(notification.GetDescription()) + 1.5f;

            RenderUtil.drawRect(GetX() - 1.5f, y, GetX() + width, y + 13, 0x75101010);
            RenderUtil.drawStringWithShadow(notification.GetDescription(), GetX(), y + notification.GetY(), 0xFFFFFF);

            if (width >= maxWidth)
                maxWidth = width;

            y -= 13;
        }

        SetHeight(10f);
        SetWidth(maxWidth);
    }
}
