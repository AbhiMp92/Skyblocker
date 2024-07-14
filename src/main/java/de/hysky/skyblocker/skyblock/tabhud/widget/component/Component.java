package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Abstract base class for a component that may be added to a Widget.
 */
public abstract class Component {

    public static final int PADDING_VERTICAL = 4;
    public static final int PADDING_HORIZONTAL = 8;
    static final int ICO_DIM = 16;
    public static final int PAD_S = 2;
    public static final int PAD_L = 4;

    static final TextRenderer txtRend = MinecraftClient.getInstance().textRenderer;

    // these should always be the content dimensions without any padding.
    int width, height;

    public abstract void render(DrawContext context, int x, int y);

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

}
