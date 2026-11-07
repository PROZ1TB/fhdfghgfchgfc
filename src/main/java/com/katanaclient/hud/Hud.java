package com.katanaclient.hud;

import com.katanaclient.config.Corner;
import com.katanaclient.config.KatanaConfig;
import com.katanaclient.util.ClickCounter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** All HUD drawing. Every element is laid out in local pixels and scaled by the theme size. */
public final class Hud {
    private static final int MARGIN = 6;
    private static final ClickCounter LEFT = new ClickCounter();
    private static final ClickCounter RIGHT = new ClickCounter();
    private static final float[] KEY_ANIM = new float[7]; // W A S D LMB RMB SPACE
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private Hud() {}

    public static void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;
        KatanaConfig cfg = KatanaConfig.get();
        long now = System.currentTimeMillis();
        LEFT.update(mc.options.keyAttack.isDown(), now);
        RIGHT.update(mc.options.keyUse.isDown(), now);

        int accent = cfg.accent();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        Font font = mc.font;

        drawPanel(g, mc, font, cfg, accent, sw, sh, now);
        if (cfg.keystrokes) drawKeys(g, mc, font, cfg, accent, sw, sh, now);
        if (cfg.armor) drawArmor(g, mc, font, cfg, accent, sw, sh);
    }

    // ------------------------------------------------------------------ panel

    private static void drawPanel(GuiGraphics g, Minecraft mc, Font font, KatanaConfig cfg, int accent,
                                  int sw, int sh, long now) {
        String[][] rows = new String[4][];
        int n = 0;
        if (cfg.showFps) rows[n++] = new String[] {"FPS", Integer.toString(mc.getFps()), "fps"};
        if (cfg.showCps) rows[n++] = new String[] {"CPS", LEFT.cps(now) + " | " + RIGHT.cps(now), ""};
        if (cfg.showPing) rows[n++] = new String[] {"PING", pingText(mc), "ping"};
        if (cfg.showServer) rows[n++] = new String[] {"SERVER", serverText(mc), ""};
        if (!cfg.watermark && n == 0) return;

        Component title = Component.literal("KATANA CLIENT").withStyle(ChatFormatting.BOLD);
        String credit = "made by bluxy62";
        int w = 110;
        int headerH = cfg.watermark ? 26 : 0;
        if (cfg.watermark) w = Math.max(w, 26 + Math.max(font.width(title), font.width(credit)) + 8);
        for (int i = 0; i < n; i++) w = Math.max(w, 10 + font.width(rows[i][0]) + 8 + font.width(rows[i][1]) + 8);
        int h = headerH + n * 12 + (n > 0 ? 6 : 0);

        float s = cfg.scale;
        int x = cfg.panelCorner.x(sw, Math.round(w * s), MARGIN);
        int y = cfg.panelCorner.y(sh, Math.round(h * s), MARGIN);
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(s, s);

        card(g, 0, 0, w, h, cfg, accent);
        if (cfg.watermark) {
            KatanaLogo.draw(g, 5, 5, 1, accent);
            g.drawString(font, title, 26, 5, 0xFF000000 | accent);
            g.drawString(font, credit, 26, 15, 0xFF9AA0AA);
            if (n > 0) g.fill(5, headerH, w - 5, headerH + 1, 0x33FFFFFF);
        }
        int ry = headerH + 4;
        for (int i = 0; i < n; i++) {
            g.drawString(font, rows[i][0], 8, ry, 0xFF8A93A0);
            int color = switch (rows[i][2]) {
                case "fps" -> fpsColor(mc.getFps());
                case "ping" -> pingColor(pingMs(mc));
                default -> 0xFFFFFFFF;
            };
            g.drawString(font, rows[i][1], w - 8 - font.width(rows[i][1]), ry, color);
            ry += 12;
        }
        g.pose().popMatrix();
    }

    private static int pingMs(Minecraft mc) {
        if (mc.getConnection() == null || mc.player == null) return -1;
        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info == null ? -1 : info.getLatency();
    }

    private static String pingText(Minecraft mc) {
        if (mc.getCurrentServer() == null) return "local";
        int p = pingMs(mc);
        return p < 0 ? "..." : p + " ms";
    }

    private static String serverText(Minecraft mc) {
        ServerData sd = mc.getCurrentServer();
        if (sd == null || sd.ip == null) return "Singleplayer";
        return sd.ip.length() > 24 ? sd.ip.substring(0, 23) + "~" : sd.ip;
    }

    private static int fpsColor(int fps) {
        return fps >= 60 ? 0xFF55FF7F : fps >= 30 ? 0xFFFFD455 : 0xFFFF5555;
    }

    private static int pingColor(int ms) {
        if (ms < 0) return 0xFFFFFFFF;
        return ms <= 80 ? 0xFF55FF7F : ms <= 160 ? 0xFFFFD455 : 0xFFFF5555;
    }

    // -------------------------------------------------------------- keystrokes

    private static void drawKeys(GuiGraphics g, Minecraft mc, Font font, KatanaConfig cfg, int accent,
                                 int sw, int sh, long now) {
        int k = 20, gap = 2;
        int w = k * 3 + gap * 2;
        int h = k * 3 + gap * 3 + 14;
        float s = cfg.scale;
        int x = cfg.keysCorner.x(sw, Math.round(w * s), MARGIN);
        int y = cfg.keysCorner.y(sh, Math.round(h * s), MARGIN);
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(s, s);

        key(g, font, cfg, accent, 0, k + gap, 0, k, k, label(mc.options.keyUp), mc.options.keyUp.isDown(), null);
        key(g, font, cfg, accent, 1, 0, k + gap, k, k, label(mc.options.keyLeft), mc.options.keyLeft.isDown(), null);
        key(g, font, cfg, accent, 2, k + gap, k + gap, k, k, label(mc.options.keyDown), mc.options.keyDown.isDown(), null);
        key(g, font, cfg, accent, 3, (k + gap) * 2, k + gap, k, k, label(mc.options.keyRight), mc.options.keyRight.isDown(), null);
        int bw = (w - gap) / 2;
        int by = (k + gap) * 2;
        key(g, font, cfg, accent, 4, 0, by, bw, k, "LMB", mc.options.keyAttack.isDown(), LEFT.cps(now) + " cps");
        key(g, font, cfg, accent, 5, bw + gap, by, w - bw - gap, k, "RMB", mc.options.keyUse.isDown(), RIGHT.cps(now) + " cps");
        key(g, font, cfg, accent, 6, 0, by + k + gap, w, 12, "", mc.options.keyJump.isDown(), null);
        // space bar glyph
        int cx = w / 2;
        int cy = by + k + gap + 6;
        g.fill(cx - 12, cy - 1, cx + 12, cy + 1, 0xFFFFFFFF);

        g.pose().popMatrix();
    }

    private static String label(KeyMapping km) {
        String s = km.getTranslatedKeyMessage().getString().toUpperCase();
        return s.length() > 3 ? s.substring(0, 3) : s;
    }

    private static void key(GuiGraphics g, Font font, KatanaConfig cfg, int accent, int idx, int x, int y, int w, int h,
                            String text, boolean down, String sub) {
        KEY_ANIM[idx] += ((down ? 1f : 0f) - KEY_ANIM[idx]) * 0.35f;
        float a = KEY_ANIM[idx];
        int bg = blend(0xFF000000 | scaleAlpha(0x14161C, cfg), 0xFF000000 | accent, a, cfg.bgOpacity, a);
        roundRect(g, x, y, x + w, y + h, bg);
        // thin accent outline on top edge for depth
        g.fill(x + 1, y, x + w - 1, y + 1, blendRgb(0x30343E, accent, a) | 0xFF000000);
        int textColor = a > 0.5f ? 0xFF101018 : 0xFFFFFFFF;
        if (!text.isEmpty()) {
            if (sub == null) {
                g.drawString(font, text, x + (w - font.width(text)) / 2, y + (h - 8) / 2, textColor);
            } else {
                g.drawString(font, text, x + (w - font.width(text)) / 2, y + 3, textColor);
                g.pose().pushMatrix();
                g.pose().translate(x + w / 2f - font.width(sub) * 0.35f, y + 12);
                g.pose().scale(0.7f, 0.7f);
                g.drawString(font, sub, 0, 0, a > 0.5f ? 0xFF2A2A33 : 0xFF9AA0AA);
                g.pose().popMatrix();
            }
        }
    }

    private static int scaleAlpha(int rgb, KatanaConfig cfg) {
        return rgb;
    }

    // ------------------------------------------------------------------ armor

    private static void drawArmor(GuiGraphics g, Minecraft mc, Font font, KatanaConfig cfg, int accent, int sw, int sh) {
        int slot = 22;
        int w = slot * 4 + 6;
        int h = 18 + 12;
        float s = cfg.scale;
        int x = cfg.armorCorner.x(sw, Math.round(w * s), MARGIN);
        int y = cfg.armorCorner.y(sh, Math.round(h * s), MARGIN);
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(s, s);
        card(g, 0, 0, w, h + 4, cfg, accent);
        for (int i = 0; i < 4; i++) {
            ItemStack st = mc.player.getItemBySlot(ARMOR[i]);
            int ix = 4 + i * slot;
            if (st.isEmpty()) {
                g.fill(ix, 4, ix + 16, 20, 0x22FFFFFF);
                continue;
            }
            g.renderItem(st, ix, 4);
            if (st.isDamageableItem()) {
                int max = st.getMaxDamage();
                int pct = Math.max(0, Math.round((max - st.getDamageValue()) * 100f / max));
                String t = pct + "%";
                int color = pct > 60 ? 0xFF55FF7F : pct > 25 ? 0xFFFFD455 : 0xFFFF5555;
                g.pose().pushMatrix();
                g.pose().translate(ix + 8 - font.width(t) * 0.35f, 22);
                g.pose().scale(0.7f, 0.7f);
                g.drawString(font, t, 0, 0, color);
                g.pose().popMatrix();
            }
        }
        g.pose().popMatrix();
    }

    // ---------------------------------------------------------------- drawing

    /** Glassy card: gradient body, rounded corners, accent top line and left bar. */
    private static void card(GuiGraphics g, int x, int y, int w, int h, KatanaConfig cfg, int accent) {
        int a = Math.round(255 * cfg.bgOpacity / 100f);
        int top = (a << 24) | 0x1B1E27;
        int bot = (a << 24) | 0x0D0F14;
        g.fillGradient(x + 1, y, x + w - 1, y + h, top, bot);
        g.fillGradient(x, y + 1, x + 1, y + h - 1, top, bot);
        g.fillGradient(x + w - 1, y + 1, x + w, y + h - 1, top, bot);
        g.fill(x + 1, y, x + w - 1, y + 1, 0xFF000000 | accent);
        g.fill(x, y + 2, x + 1, y + h - 2, 0xCC000000 | accent);
    }

    private static void roundRect(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1 + 1, y1, x2 - 1, y2, color);
        g.fill(x1, y1 + 1, x2, y2 - 1, color);
    }

    private static int blend(int base, int accentArgb, float t, int opacityPct, float press) {
        int baseA = Math.round(255 * Math.max(0.25f, opacityPct / 100f));
        int aA = Math.round(baseA + (235 - baseA) * press);
        int rgb = blendRgb(base & 0xFFFFFF, accentArgb & 0xFFFFFF, t);
        return (aA << 24) | rgb;
    }

    private static int blendRgb(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int g = Math.round(((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bl = Math.round((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return (r << 16) | (g << 8) | bl;
    }
}
