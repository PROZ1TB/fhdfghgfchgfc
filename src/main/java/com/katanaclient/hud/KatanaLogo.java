package com.katanaclient.hud;

import net.minecraft.client.gui.GuiGraphics;

/** Pixel-art katana drawn with fills (no texture needed). 16x16 grid. */
public final class KatanaLogo {
    public static final int SIZE = 16;

    private KatanaLogo() {}

    public static void draw(GuiGraphics g, int x, int y, int px, int accentRgb) {
        int handleA = 0xFF000000 | accentRgb;
        int handleB = 0xFF000000 | darken(accentRgb);
        // handle wrapped in two alternating colors, pommel at the corner
        for (int i = 0; i <= 4; i++) {
            put(g, x, y, px, i, 15 - i, i == 0 ? 0xFFD4AF37 : (i % 2 == 0 ? handleA : handleB));
        }
        // guard (tsuba)
        put(g, x, y, px, 4, 9, 0xFFD4AF37);
        put(g, x, y, px, 5, 10, 0xFFF2CF5B);
        put(g, x, y, px, 6, 11, 0xFFD4AF37);
        // blade: bright edge + shaded underside
        for (int bx = 6; bx <= 14; bx++) {
            put(g, x, y, px, bx, 15 - bx, 0xFFEFF4F8);
            put(g, x, y, px, bx, 16 - bx, 0xFF8D99A6);
        }
        put(g, x, y, px, 15, 0, 0xFFFFFFFF); // tip
    }

    private static void put(GuiGraphics g, int ox, int oy, int px, int cx, int cy, int argb) {
        g.fill(ox + cx * px, oy + cy * px, ox + (cx + 1) * px, oy + (cy + 1) * px, argb);
    }

    private static int darken(int rgb) {
        int r = ((rgb >> 16) & 0xFF) * 6 / 10;
        int g = ((rgb >> 8) & 0xFF) * 6 / 10;
        int b = (rgb & 0xFF) * 6 / 10;
        return (r << 16) | (g << 8) | b;
    }
}
