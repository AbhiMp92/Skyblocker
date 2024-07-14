package de.hysky.skyblocker.utils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class JoinWorldPlaceholderScreen extends Screen {
    public JoinWorldPlaceholderScreen() {
        super(Text.translatable("connect.joining"));
    }
}
