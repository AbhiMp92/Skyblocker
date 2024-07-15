package de.hysky.skyblocker.skyblock.fancybars;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.awt.*;

public class StatusBarConfig {
    public static void loadFromJson(JsonObject object, StatusBar statusBar) {
        if (object.has("colors")) {
            JsonArray colors = object.get("colors").getAsJsonArray();
            if (colors.size() < 2 && statusBar.hasOverflow()) {
                throw new IllegalStateException("Missing second color of bar that has overflow");
            }
            Color[] newColors = new Color[colors.size()];
            for (int i = 0; i < colors.size(); i++) {
                JsonElement jsonElement = colors.get(i);
                newColors[i] = new Color(Integer.parseInt(jsonElement.getAsString(), 16));
            }
            statusBar.setColors(newColors);
        }

        if (object.has("text_color")) {
            statusBar.setTextColor(new Color(Integer.parseInt(object.get("text_color").getAsString(), 16)));
        }

        String maybeAnchor = object.get("anchor").getAsString().trim();
        statusBar.setAnchor(maybeAnchor.equals("null") ? null : BarPositioner.BarAnchor.valueOf(maybeAnchor));
        statusBar.setSize(object.get("size").getAsInt());
        statusBar.setGridX(object.get("x").getAsInt());
        statusBar.setGridY(object.get("y").getAsInt());

        if (object.has("icon_position")) {
            statusBar.setIconPosition(IconPosition.valueOf(object.get("icon_position").getAsString().trim()));
        }
        if (object.has("show_text")) {
            statusBar.setShowText(object.get("show_text").getAsBoolean());
        }
    }

    public static JsonObject toJson(StatusBar statusBar) {
        JsonObject object = new JsonObject();
        JsonArray colors = new JsonArray();
        for (Color color : statusBar.getColors()) {
            colors.add(Integer.toHexString(color.getRGB()).substring(2));
        }
        object.add("colors", colors);

        if (statusBar.getTextColor() != null) {
            object.addProperty("text_color", Integer.toHexString(statusBar.getTextColor().getRGB()).substring(2));
        }
        object.addProperty("size", statusBar.getSize());
        if (statusBar.getAnchor() != null) {
            object.addProperty("anchor", statusBar.getAnchor().toString());
        } else {
            object.addProperty("anchor", "null");
        }
        object.addProperty("x", statusBar.getGridX());
        object.addProperty("y", statusBar.getGridY());
        object.addProperty("icon_position", statusBar.getIconPosition().toString());
        object.addProperty("show_text", statusBar.showText());
        return object;
    }
}
