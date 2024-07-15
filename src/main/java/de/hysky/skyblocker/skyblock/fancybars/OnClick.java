package de.hysky.skyblocker.skyblock.fancybars;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.DrawContext;

@FunctionalInterface
public interface OnClick {
    void onClick(StatusBar statusBar, int button, int mouseX, int mouseY);

    default void renderCursor(DrawContext context, int mouseX, int mouseY, float delta, StatusBar statusBar) {
        int temp_x = statusBar.getX();
        int temp_y = statusBar.getY();
        int temp_width = statusBar.getWidth();
        boolean temp_ghost = statusBar.isInMouse();

        statusBar.setX(mouseX);
        statusBar.setY(mouseY);
        statusBar.setWidth(100);
        statusBar.setInMouse(false);

        StatusBarRenderer.render(statusBar, context, mouseX, mouseY, delta);

        statusBar.setX(temp_x);
        statusBar.setY(temp_y);
        statusBar.setWidth(temp_width);
        statusBar.setInMouse(temp_ghost);
    }

    default void loadFromJson(JsonObject object, StatusBar statusBar) {
        StatusBarConfig.loadFromJson(object, statusBar);
    }
}
