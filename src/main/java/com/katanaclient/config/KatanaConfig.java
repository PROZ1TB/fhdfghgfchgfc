package com.katanaclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.katanaclient.util.HexColor;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Persistent settings: config/katana-client.json */
public class KatanaConfig {
    private static final Logger LOG = LoggerFactory.getLogger("katanaclient");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final int DEFAULT_ACCENT = 0xE5304F;
    private static KatanaConfig instance;

    // modules
    public boolean watermark = true;
    public boolean showFps = true;
    public boolean showCps = true;
    public boolean showPing = true;
    public boolean showServer = true;
    public boolean keystrokes = true;
    public boolean armor = true;

    // layout
    public Corner panelCorner = Corner.TOP_LEFT;
    public Corner keysCorner = Corner.BOTTOM_LEFT;
    public Corner armorCorner = Corner.BOTTOM_RIGHT;

    // theme
    public int accentRgb = DEFAULT_ACCENT;
    public boolean rainbow = false;
    public float scale = 1.0f;       // 0.5 .. 2.0
    public int bgOpacity = 55;       // percent

    // freecam
    public float freecamSpeed = 1.0f; // 0.2 .. 5.0

    public static synchronized KatanaConfig get() {
        if (instance == null) load();
        return instance;
    }

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("katana-client.json");
    }

    public static synchronized void load() {
        Path p = path();
        KatanaConfig c = null;
        if (Files.exists(p)) {
            try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
                c = GSON.fromJson(r, KatanaConfig.class);
            } catch (Exception e) {
                LOG.warn("Config unreadable, using defaults: {}", e.toString());
            }
        }
        if (c == null) c = new KatanaConfig();
        c.normalize();
        instance = c;
        save();
    }

    public static synchronized void save() {
        if (instance == null) return;
        Path p = path();
        Path tmp = p.resolveSibling("katana-client.json.tmp");
        try {
            Files.createDirectories(p.getParent());
            Files.writeString(tmp, GSON.toJson(instance), StandardCharsets.UTF_8);
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.warn("Could not save config: {}", e.toString());
        }
    }

    private void normalize() {
        if (panelCorner == null) panelCorner = Corner.TOP_LEFT;
        if (keysCorner == null) keysCorner = Corner.BOTTOM_LEFT;
        if (armorCorner == null) armorCorner = Corner.BOTTOM_RIGHT;
        accentRgb &= 0xFFFFFF;
        scale = Math.max(0.5f, Math.min(2.0f, scale));
        bgOpacity = Math.max(0, Math.min(100, bgOpacity));
        freecamSpeed = Math.max(0.2f, Math.min(5.0f, freecamSpeed));
    }

    /** Current accent color (animated when rainbow is on), as 0xRRGGBB. */
    public int accent() {
        if (!rainbow) return accentRgb;
        float hue = (System.currentTimeMillis() % 6000L) / 6000f;
        return HexColor.hsvToRgb(hue, 0.75f, 1f);
    }
}
