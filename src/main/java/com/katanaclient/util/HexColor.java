package com.katanaclient.util;

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Hex color parsing/formatting and HSV conversion. No Minecraft dependencies. */
public final class HexColor {
    private static final Pattern HEX = Pattern.compile("^#?([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$");

    private HexColor() {}

    /** Accepts "#RRGGBB", "RRGGBB", "#RGB" or "RGB". Returns 0xRRGGBB. */
    public static OptionalInt parse(String text) {
        if (text == null) return OptionalInt.empty();
        Matcher m = HEX.matcher(text.trim());
        if (!m.matches()) return OptionalInt.empty();
        String h = m.group(1);
        if (h.length() == 3) {
            StringBuilder sb = new StringBuilder(6);
            for (char c : h.toCharArray()) sb.append(c).append(c);
            h = sb.toString();
        }
        return OptionalInt.of(Integer.parseInt(h, 16));
    }

    public static String format(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    /** @return {hue 0..1, saturation 0..1, value 0..1} */
    public static float[] rgbToHsv(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float d = max - min;
        float h;
        if (d == 0) h = 0;
        else if (max == r) h = (((g - b) / d) % 6f) / 6f;
        else if (max == g) h = (((b - r) / d) + 2f) / 6f;
        else h = (((r - g) / d) + 4f) / 6f;
        if (h < 0) h += 1f;
        float s = max == 0 ? 0 : d / max;
        return new float[] {h, s, max};
    }

    public static int hsvToRgb(float h, float s, float v) {
        h = ((h % 1f) + 1f) % 1f;
        s = clamp01(s);
        v = clamp01(v);
        float c = v * s;
        float x = c * (1 - Math.abs((h * 6f) % 2f - 1));
        float m = v - c;
        float r, g, b;
        int sector = (int) (h * 6f);
        switch (sector) {
            case 0 -> { r = c; g = x; b = 0; }
            case 1 -> { r = x; g = c; b = 0; }
            case 2 -> { r = 0; g = c; b = x; }
            case 3 -> { r = 0; g = x; b = c; }
            case 4 -> { r = x; g = 0; b = c; }
            default -> { r = c; g = 0; b = x; }
        }
        int ri = Math.round((r + m) * 255f);
        int gi = Math.round((g + m) * 255f);
        int bi = Math.round((b + m) * 255f);
        return (ri << 16) | (gi << 8) | bi;
    }

    private static float clamp01(float f) {
        return f < 0 ? 0 : Math.min(f, 1);
    }
}
