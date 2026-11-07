package com.katanaclient.ui;

import com.katanaclient.config.Corner;
import com.katanaclient.config.KatanaConfig;
import com.katanaclient.freecam.Freecam;
import com.katanaclient.hud.KatanaLogo;
import com.katanaclient.util.HexColor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.OptionalInt;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/** Settings menu: Modules / Theme / Freecam pages. The live HUD stays visible behind it as a preview. */
public class KatanaMenuScreen extends Screen {
    private enum Page { MODULES("Modules"), THEME("Theme"), FREECAM("Freecam");
        final String label;
        Page(String l) { label = l; }
    }

    private static final int W = 300;
    private static Page page = Page.MODULES;

    private final Screen parent;
    private final KatanaConfig cfg = KatanaConfig.get();
    private float h, s, v;
    private boolean suppress = false;
    private boolean hexValid = true;
    private EditBox hexBox;
    private ValueSlider hueS, satS, valS;

    public KatanaMenuScreen(Screen parent) {
        super(Component.literal("Katana Client"));
        this.parent = parent;
        float[] a = HexColor.rgbToHsv(cfg.accentRgb);
        h = a[0]; s = a[1]; v = a[2];
    }

    private int left() { return (this.width - W) / 2; }

    private int accent() { return 0xFF000000 | cfg.accent(); }

    @Override
    protected void init() {
        int l = left();
        int x = l;
        for (Page p : Page.values()) {
            Button b = Button.builder(Component.literal(p.label), btn -> { page = p; rebuildWidgets(); })
                    .bounds(x, 40, 96, 20).build();
            b.active = p != page;
            addRenderableWidget(b);
            x += 102;
        }
        switch (page) {
            case MODULES -> initModules(l);
            case THEME -> initTheme(l);
            case FREECAM -> initFreecam(l);
        }
    }

    // ---------------------------------------------------------------- modules

    private void initModules(int l) {
        int y = 68;
        toggle(l, y, "Watermark", () -> cfg.watermark, b -> cfg.watermark = b);
        toggle(l + 152, y, "Keystrokes", () -> cfg.keystrokes, b -> cfg.keystrokes = b);
        y += 22;
        toggle(l, y, "FPS", () -> cfg.showFps, b -> cfg.showFps = b);
        toggle(l + 152, y, "Armor status", () -> cfg.armor, b -> cfg.armor = b);
        y += 22;
        toggle(l, y, "CPS", () -> cfg.showCps, b -> cfg.showCps = b);
        toggle(l + 152, y, "Ping", () -> cfg.showPing, b -> cfg.showPing = b);
        y += 22;
        toggle(l, y, "Server", () -> cfg.showServer, b -> cfg.showServer = b);
        y += 28;
        cycle(l, y, 98, "Panel", () -> cfg.panelCorner, c -> cfg.panelCorner = c);
        cycle(l + 101, y, 98, "Keys", () -> cfg.keysCorner, c -> cfg.keysCorner = c);
        cycle(l + 202, y, 98, "Armor", () -> cfg.armorCorner, c -> cfg.armorCorner = c);
    }

    private void toggle(int x, int y, String name, BooleanSupplier get, Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(toggleLabel(name, get.getAsBoolean()), b -> {
            set.accept(!get.getAsBoolean());
            b.setMessage(toggleLabel(name, get.getAsBoolean()));
        }).bounds(x, y, 148, 20).build());
    }

    private static Component toggleLabel(String name, boolean on) {
        return Component.literal(name + ": ").append(
                Component.literal(on ? "ON" : "OFF").withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    private void cycle(int x, int y, int w, String name, java.util.function.Supplier<Corner> get, Consumer<Corner> set) {
        addRenderableWidget(Button.builder(Component.literal(name + ": " + get.get().label), b -> {
            set.accept(get.get().next());
            b.setMessage(Component.literal(name + ": " + get.get().label));
        }).bounds(x, y, w, 20).build());
    }

    // ------------------------------------------------------------------ theme

    private void initTheme(int l) {
        hueS = new ValueSlider(l, 68, 148, "Hue", 0, 360, h * 360, d -> String.valueOf(Math.round(d)),
                d -> { h = (float) (d / 360); fromSliders(); });
        satS = new ValueSlider(l, 90, 148, "Saturation", 0, 100, s * 100, d -> Math.round(d) + "%",
                d -> { s = (float) (d / 100); fromSliders(); });
        valS = new ValueSlider(l, 112, 148, "Brightness", 0, 100, v * 100, d -> Math.round(d) + "%",
                d -> { v = (float) (d / 100); fromSliders(); });
        addRenderableWidget(hueS);
        addRenderableWidget(satS);
        addRenderableWidget(valS);

        addRenderableWidget(new ValueSlider(l + 152, 68, 148, "Size", 50, 200, cfg.scale * 100,
                d -> Math.round(d) + "%", d -> cfg.scale = (float) (d / 100)));
        addRenderableWidget(new ValueSlider(l + 152, 90, 148, "Background", 0, 100, cfg.bgOpacity,
                d -> Math.round(d) + "%", d -> cfg.bgOpacity = (int) Math.round(d)));
        toggle(l + 152, 112, "Rainbow", () -> cfg.rainbow, b -> cfg.rainbow = b);

        hexBox = new EditBox(this.font, l, 140, 100, 20, Component.literal("Hex"));
        hexBox.setMaxLength(7);
        hexBox.setHint(Component.literal("#RRGGBB"));
        hexBox.setValue(HexColor.format(cfg.accentRgb));
        hexBox.setResponder(this::onHex);
        addRenderableWidget(hexBox);

        addRenderableWidget(Button.builder(Component.literal("Reset color"), b -> {
            cfg.accentRgb = KatanaConfig.DEFAULT_ACCENT;
            float[] a = HexColor.rgbToHsv(cfg.accentRgb);
            h = a[0]; s = a[1]; v = a[2];
            rebuildWidgets();
        }).bounds(l + 152, 140, 148, 20).build());
    }

    private void fromSliders() {
        cfg.accentRgb = HexColor.hsvToRgb(h, s, v);
        suppress = true;
        hexBox.setValue(HexColor.format(cfg.accentRgb));
        suppress = false;
        hexValid = true;
        hexBox.setTextColor(0xFFE0E0E0);
    }

    private void onHex(String text) {
        if (suppress) return;
        OptionalInt c = HexColor.parse(text);
        if (c.isPresent()) {
            hexValid = true;
            hexBox.setTextColor(0xFFE0E0E0);
            cfg.accentRgb = c.getAsInt();
            float[] a = HexColor.rgbToHsv(cfg.accentRgb);
            h = a[0]; s = a[1]; v = a[2];
            hueS.set(h * 360);
            satS.set(s * 100);
            valS.set(v * 100);
        } else {
            hexValid = false;
            hexBox.setTextColor(0xFFFF5555);
        }
    }

    // ---------------------------------------------------------------- freecam

    private void initFreecam(int l) {
        addRenderableWidget(new ValueSlider(l, 68, 300, "Speed", 20, 500, cfg.freecamSpeed * 100,
                d -> String.format("%.1fx", d / 100), d -> cfg.freecamSpeed = (float) (d / 100)));
        addRenderableWidget(Button.builder(Component.literal("Toggle Freecam now"), b -> {
            this.minecraft.setScreen(null);
            Freecam.toggle();
        }).bounds(l, 94, 300, 20).build());
    }

    // ----------------------------------------------------------------- render

    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float pt) {
        super.renderBackground(g, mx, my, pt);
        int l = left();
        int top = 10;
        int bottom = 178 + (page == Page.THEME ? 12 : 0);
        g.fillGradient(l - 10, top, l + W + 10, bottom, 0xF0191C25, 0xF00B0D12);
        g.fill(l - 10, top, l + W + 10, top + 2, accent());
        g.fill(l - 10, top, l - 8, bottom, 0x99000000 | cfg.accent());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        int l = left();
        KatanaLogo.draw(g, l, 16, 1, cfg.accent());
        g.drawString(this.font, Component.literal("KATANA CLIENT").withStyle(ChatFormatting.BOLD), l + 22, 18, accent());
        g.drawString(this.font, "made by bluxy62", l + 22, 28, 0xFF8A93A0);

        if (page == Page.THEME) {
            g.fill(l + 106, 142, l + 136, 158, 0xFF000000 | cfg.accentRgb);
            if (!hexValid) g.drawString(this.font, "Invalid hex: use #RRGGBB or #RGB", l, 164, 0xFFFF5555);
        } else if (page == Page.FREECAM) {
            g.drawString(this.font, "Hotkey: F7 (rebind in Controls > Misc).", l, 124, 0xFFCCCCCC);
            g.drawString(this.font, "WASD moves, Space/Shift up/down, Sprint = 3x.", l, 136, 0xFFCCCCCC);
            g.drawString(this.font, "Your real player stays put and can still be hit.", l, 148, 0xFFFFD455);
            g.drawString(this.font, "Check server rules: some servers ban freecam.", l, 160, 0xFFFFD455);
        }
    }

    @Override
    public void removed() {
        KatanaConfig.save();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Slider with a live text label. */
    private static class ValueSlider extends AbstractSliderButton {
        private final String label;
        private final double min, max;
        private final DoubleFunction<String> fmt;
        private final DoubleConsumer onChange;

        ValueSlider(int x, int y, int w, String label, double min, double max, double initial,
                    DoubleFunction<String> fmt, DoubleConsumer onChange) {
            super(x, y, w, 20, Component.empty(), (clamp(initial, min, max) - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.fmt = fmt;
            this.onChange = onChange;
            updateMessage();
        }

        void set(double real) {
            this.value = (clamp(real, min, max) - min) / (max - min);
            updateMessage();
        }

        private double real() { return min + this.value * (max - min); }

        private static double clamp(double d, double lo, double hi) { return Math.max(lo, Math.min(hi, d)); }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + fmt.apply(real())));
        }

        @Override
        protected void applyValue() {
            onChange.accept(real());
        }
    }
}
