package de.hysky.skyblocker.skyblock.item.tooltip;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class DrawPreviewParameters {
    private final DrawContext context;
    private final ItemStack stack;
    private final List<Text> tooltips;
    private final String type;
    private final String size;
    private final int x;
    private final int y;

    public DrawPreviewParameters(DrawContext context, ItemStack stack, List<Text> tooltips, String type, String size, int x, int y) {
        this.context = context;
        this.stack = stack;
        this.tooltips = tooltips;
        this.type = type;
        this.size = size;
        this.x = x;
        this.y = y;
    }

    public DrawContext getContext() {
        return context;
    }

    public ItemStack getStack() {
        return stack;
    }

    public List<Text> getTooltips() {
        return tooltips;
    }

    public String getType() {
        return type;
    }

    public String getSize() {
        return size;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
