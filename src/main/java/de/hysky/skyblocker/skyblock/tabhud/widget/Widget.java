package de.hysky.skyblocker.skyblock.tabhud.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for a Widget.
 * Widgets are containers for components with a border and a title.
 * Their size is dependent on the components inside,
 * the position may be changed after construction.
 */
public abstract class Widget {

    private final List<Component> components = new ArrayList<>();
    private int width = 0, height = 0;
    private int x = 0, y = 0;
    private final int color;
    private final Text title;

    private static final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    int PADDING_VERTICAL = 4;
    int PADDING_HORIZONTAL = 8;
    private static final int BORDER_SIZE_N = textRenderer.fontHeight + 4;
    private static final int BORDER_SIZE_S = 4;
    private static final int BORDER_SIZE_W = 4;
    private static final int BORDER_SIZE_E = 4;
    private static final int COLOR_BACKGROUND_BOX = 0xc00c0c0c;

    public Widget(MutableText title, Integer colorValue) {
        this.title = title;
        this.color = 0xff000000 | colorValue;
    }

    public final int getX() {
        return this.x;
    }

    public final void setX(int x) {
        this.x = x;
    }

    public final int getY() {
        return this.y;
    }

    public final void setY(int y) {
        this.y = y;
    }

    public final int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public final int getHeight() {
        return this.height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setDimensions(int size) {
        setDimensions(size, size);
    }

    public void setDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }


    public final void addComponent(Component component) {
        this.components.add(component);
    }

    public final void update() {
        this.components.clear();
        this.updateContent();
        this.pack();
    }

    public abstract void updateContent();

    public final void addSimpleIcoText(ItemStack ico, String string, Formatting format, int idx) {
        Text text = Widget.simpleEntryText(idx, string, format);
        if (text != null) {
            this.addComponent(new IcoTextComponent(ico, text));
        }
    }

    public final void addSimpleIcoText(ItemStack ico, String string, Formatting format, String content) {
        Text text = Widget.simpleEntryText(content, string, format);
        if (text != null) {
            this.addComponent(new IcoTextComponent(ico, text));
        }
    }

    private void pack() {
        int totalHeight = 0;
        this.width = 0;

        for (Component component : this.components) {
            totalHeight += component.getHeight() + Component.PADDING_VERTICAL;
            this.width = Math.max(this.width, component.getWidth() + Component.PADDING_HORIZONTAL);
        }

        totalHeight -= Component.PADDING_VERTICAL / 2;
        totalHeight += BORDER_SIZE_N + BORDER_SIZE_S - 2;
        this.width += BORDER_SIZE_E + BORDER_SIZE_W;

        // Minimum width based on title
        this.width = Math.max(this.width, BORDER_SIZE_W + BORDER_SIZE_E + textRenderer.getWidth(this.title) + 8);

        this.height = totalHeight;
    }

    public final void render(DrawContext context) {
        this.render(context, true);
    }

    public final void render(DrawContext context, boolean hasBackground) {
        MatrixStack matrixStack = context.getMatrices();

        RenderSystem.enableDepthTest();
        matrixStack.push();

        float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100f;
        matrixStack.scale(scale, scale, 1);

        matrixStack.translate(0, 0, 200);

        if (hasBackground) {
            context.fill(x + 1, y, x + width - 1, y + height, COLOR_BACKGROUND_BOX);
            context.fill(x, y + 1, x + 1, y + height - 1, COLOR_BACKGROUND_BOX);
            context.fill(x + width - 1, y + 1, x + width, y + height - 1, COLOR_BACKGROUND_BOX);
        }

        matrixStack.translate(0, 0, 100);

        int stringHalfHeight = textRenderer.fontHeight / 2;
        int stringAreaWidth = textRenderer.getWidth(title) + 4;

        context.drawText(textRenderer, title, x + 8, y + 2, this.color, false);

        this.drawHorizontalLine(context, x + 2, y + 1 + stringHalfHeight, 4);
        this.drawHorizontalLine(context, x + 2 + stringAreaWidth + 4, y + 1 + stringHalfHeight, width - 4 - 4 - stringAreaWidth);
        this.drawHorizontalLine(context, x + 2, y + height - 2, width - 4);

        this.drawVerticalLine(context, x + 1, y + 2 + stringHalfHeight, height - 4 - stringHalfHeight);
        this.drawVerticalLine(context, x + width - 2, y + 2 + stringHalfHeight, height - 4 - stringHalfHeight);

        int yOffset = y + BORDER_SIZE_N;

        for (Component component : components) {
            component.render(context, x + BORDER_SIZE_W, yOffset);
            yOffset += component.getHeight() + Component.PADDING_VERTICAL;
        }

        matrixStack.pop();
        RenderSystem.disableDepthTest();
    }

    private void drawHorizontalLine(DrawContext context, int xPos, int yPos, int width) {
        context.fill(xPos, yPos, xPos + width, yPos + 1, this.color);
    }

    private void drawVerticalLine(DrawContext context, int xPos, int yPos, int height) {
        context.fill(xPos, yPos, xPos + 1, yPos + height, this.color);
    }

    public static Text simpleEntryText(int idx, String entryName, Formatting contentFormat) {
        String source = PlayerListMgr.strAt(idx);

        if (source == null || !source.contains(":")) {
            return null;
        }

        String[] parts = source.split(":", 2);
        String content = parts[1].trim();
        return Widget.simpleEntryText(content, entryName, contentFormat);
    }

    public static Text simpleEntryText(String entryContent, String entryName, Formatting contentFormat) {
        String formattedContent = contentFormat.toString() + entryContent;
        String fullEntry = entryName + formattedContent;
        return Text.of(fullEntry);
    }

    public static Text plainEntryText(int idx) {
        String source = PlayerListMgr.strAt(idx);
        return (source != null) ? Text.of(source) : null;
    }

    // Getters and setters for x, y, width, height omitted for brevity
}
