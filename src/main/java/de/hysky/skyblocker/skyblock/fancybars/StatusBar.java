package de.hysky.skyblocker.skyblock.fancybars;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.navigation.GuiNavigation;
import net.minecraft.client.gui.navigation.GuiNavigationPath;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.function.Consumer;

public class StatusBar implements Widget, Drawable, Element, Selectable {
    private final Identifier icon;
    private Color[] colors;
    private final boolean hasOverflow;
    private @Nullable Color textColor;
    private final Text name;
    @Nullable OnClick onClick = null;
    public int gridX = 0;
    public int gridY = 0;
    public @Nullable BarPositioner.BarAnchor anchor = null;
    public int size = 1;
    private int width = 0;
    public float fill = 0;
    public float overflowFill = 0;
    public boolean inMouse = false;
    public Object value = "";
    private int x = 0;
    private int y = 0;
    private IconPosition iconPosition = IconPosition.LEFT;
    private boolean showText = true;

    public StatusBar(Identifier icon, Color[] colors, boolean hasOverflow, @Nullable Color textColor, Text name) {
        this.icon = icon;
        this.colors = colors;
        this.hasOverflow = hasOverflow;
        this.textColor = textColor;
        this.name = name;
    }

    public StatusBar(Identifier icon, Color[] colors, boolean hasOverflow, @Nullable Color textColor) {
        this(icon, colors, hasOverflow, textColor, Text.empty());
    }

    public StatusBar(Identifier icon, Color[] colors, boolean hasOverflow) {
        this(icon, colors, hasOverflow, null, Text.empty());
    }

    public StatusBar(Identifier icon, Color[] colors) {
        this(icon, colors, false, null, Text.empty());
    }

    public StatusBar(Identifier icon, Color[] colors, @Nullable Color textColor) {
        this(icon, colors, false, textColor, Text.empty());
    }

    public Identifier getIcon() {
        return icon;
    }

    // Getters and setters for the other fields

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        StatusBarRenderer.render(this, context, mouseX, mouseY, delta);
    }

    public void renderText(DrawContext context) {
        if (showText) {
            StatusBarRenderer.renderText(this, context);
        }
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    public boolean isInMouse(int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + 9;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        Element.super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (onClick == null || !isInMouse((int) mouseX, (int) mouseY)) return false;
        onClick.onClick(this, button, (int) mouseX, (int) mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return Element.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return Element.super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return Element.super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return Element.super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return Element.super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return Element.super.charTyped(chr, modifiers);
    }

    @Nullable
    @Override
    public GuiNavigationPath getNavigationPath(GuiNavigation navigation) {
        return Element.super.getNavigationPath(navigation);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return Element.super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void setFocused(boolean focused) {

    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Nullable
    @Override
    public GuiNavigationPath getFocusedPath() {
        return Element.super.getFocusedPath();
    }

    @Override
    public SelectionType getType() {
        return SelectionType.NONE;
    }

    @Override
    public boolean isNarratable() {
        return Selectable.super.isNarratable();
    }

    public JsonObject toJson() {
        return StatusBarConfig.toJson(this);
    }

    public static void loadFromJson(JsonObject object, StatusBar statusBar) {
        StatusBarConfig.loadFromJson(object, statusBar);
    }

    // Getter for anchor
    public @Nullable BarPositioner.BarAnchor getAnchor() {
        return anchor;
    }

    // Other methods to handle the status bar's properties

    public void setOnClick(@Nullable OnClick onClick) {
        this.onClick = onClick;
    }

    public int getGridX() {
        return gridX;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public void setAnchor(@Nullable BarPositioner.BarAnchor anchor) {
        this.anchor = anchor;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public ScreenRect getNavigationFocus() {
        return Widget.super.getNavigationFocus();
    }

    @Override
    public void setPosition(int x, int y) {
        Widget.super.setPosition(x, y);
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {

    }

    public void setWidth(int width) {
        this.width = width;
    }

    public float getFill() {
        return fill;
    }

    public void setFill(float fill) {
        this.fill = fill;
    }

    public float getOverflowFill() {
        return overflowFill;
    }

    public void setOverflowFill(float overflowFill) {
        this.overflowFill = overflowFill;
    }

    public boolean isInMouse() {
        return inMouse;
    }

    public void setInMouse(boolean inMouse) {
        this.inMouse = inMouse;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Color[] getColors() {
        return colors;
    }

    public void setColors(Color[] colors) {
        this.colors = colors;
    }

    public boolean hasOverflow() {
        return hasOverflow;
    }

    public Color getTextColor() {
        return textColor;
    }

    public void setTextColor(@Nullable Color textColor) {
        this.textColor = textColor;
    }

    public Text getName() {
        return name;
    }

    public IconPosition getIconPosition() {
        return iconPosition;
    }

    public void setIconPosition(IconPosition iconPosition) {
        this.iconPosition = iconPosition;
    }

    public boolean showText() {
        return showText;
    }

    public void setShowText(boolean showText) {
        this.showText = showText;
    }

    public void updateValues(float fill, float overflowFill, Object text) {
        this.value = text;
        this.fill = fill;
        this.overflowFill = overflowFill;
    }

    @Override
    public void appendNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public int getNavigationOrder() {
        return Element.super.getNavigationOrder();
    }
}
