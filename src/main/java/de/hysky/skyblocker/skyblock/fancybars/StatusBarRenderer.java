package de.hysky.skyblocker.skyblock.fancybars;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.awt.*;

import static de.hysky.skyblocker.skyblock.fancybars.IconPosition.LEFT;
import static de.hysky.skyblocker.skyblock.fancybars.IconPosition.RIGHT;

public class StatusBarRenderer {
    private static final Identifier BAR_FILL = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_fill");
    private static final Identifier BAR_BACK = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_back");

    public static void render(StatusBar statusBar, DrawContext context, int mouseX, int mouseY, float delta) {
        int width = statusBar.getWidth();
        if (width <= 0) return;

        int x = statusBar.getX();
        int y = statusBar.getY();
        float fill = statusBar.getFill();
        float overflowFill = statusBar.getOverflowFill();
        Color[] colors = statusBar.getColors();
        boolean inMouse = statusBar.isInMouse();
        IconPosition iconPosition = statusBar.getIconPosition();

        if (inMouse) context.setShaderColor(1f, 1f, 1f, 0.25f);
        switch (iconPosition) {
            case LEFT -> context.drawGuiTexture(statusBar.getIcon(), x, y, 9, 9);
            case RIGHT -> context.drawGuiTexture(statusBar.getIcon(), x + width - 9, y, 9, 9);
        }

        int barWith = iconPosition.equals(IconPosition.OFF) ? width : width - 10;
        int barX = iconPosition.equals(IconPosition.LEFT) ? x + 10 : x;
        context.drawGuiTexture(BAR_BACK, barX, y + 1, barWith, 7);
        RenderHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, y + 2, (int) ((barWith - 2) * fill), 5, colors[0]);

        if (statusBar.hasOverflow() && overflowFill > 0) {
            RenderHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, y + 2, (int) ((barWith - 2) * overflowFill), 5, colors[1]);
        }
        if (inMouse) context.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void renderText(StatusBar statusBar, DrawContext context) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int barWith = statusBar.getIconPosition().equals(IconPosition.OFF) ? statusBar.getWidth() : statusBar.getWidth() - 10;
        int barX = statusBar.getIconPosition().equals(IconPosition.LEFT) ? statusBar.getX() + 11 : statusBar.getX();
        String text = statusBar.getValue().toString();
        int x = barX + (barWith - textRenderer.getWidth(text)) / 2;
        int y = statusBar.getY() - 3;
        Color textColor = statusBar.getTextColor() != null ? statusBar.getTextColor() : statusBar.getColors()[0];

        final int[] offsets = new int[]{-1, 1};
        for (int i : offsets) {
            context.drawText(textRenderer, text, x + i, y, 0, false);
            context.drawText(textRenderer, text, x, y + i, 0, false);
        }
        context.drawText(textRenderer, text, x, y, textColor.getRGB(), false);
    }
}
