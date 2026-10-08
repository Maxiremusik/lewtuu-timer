package de.lewtuutimer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

final class TimerDisplay {
    private static final long FADE_DURATION_MILLIS = 2200;
    private static final double LETTER_PHASE_OFFSET = 0.38;

    private TimerDisplay() {
    }

    static Component status(String message) {
        return Component.literal(message).withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    static void showTimer(Minecraft client, Timer timer) {
        if (client.player == null || !timer.exists()) return;
        String suffix = timer.isPaused() ? " - Pausiert" : "";
        client.player.sendOverlayMessage(gradient(timer.formatted() + suffix));
    }

    static void clearActionBar() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) client.player.sendOverlayMessage(Component.empty());
    }

    private static Component gradient(String value) {
        MutableComponent result = Component.empty();
        double phase = (System.currentTimeMillis() % FADE_DURATION_MILLIS)
            / (double) FADE_DURATION_MILLIS * Math.PI * 2.0;

        for (int i = 0; i < value.length(); i++) {
            double amount = (Math.sin(phase + i * LETTER_PHASE_OFFSET) + 1.0) / 2.0;
            int red = (int) (255 + (199 - 255) * amount);
            int green = (int) (255 + (125 - 255) * amount);
            int color = (red << 16) | (green << 8) | 255;
            result.append(Component.literal(String.valueOf(value.charAt(i))).withColor(color));
        }

        return result;
    }
}
