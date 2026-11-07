package com.katanaclient.freecam;

import com.katanaclient.config.KatanaConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/**
 * Client-side detached camera. The real player stays still (its input is swapped for an
 * empty one) and attack/use/drop clicks are swallowed while active.
 */
public final class Freecam {
    private static boolean active = false;
    private static LocalPlayer owner;
    private static ClientInput savedInput;
    private static double x, y, z;
    private static float yaw, pitch;
    private static long lastNanos;

    private Freecam() {}

    public static boolean isActive() { return active; }

    public static void toggle() {
        if (active) disable(true);
        else enable();
    }

    private static void enable() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) return;
        Vec3 eye = p.getEyePosition();
        x = eye.x; y = eye.y; z = eye.z;
        yaw = p.getYRot();
        pitch = p.getXRot();
        owner = p;
        savedInput = p.input;
        p.input = new ClientInput() { };
        lastNanos = System.nanoTime();
        active = true;
        mc.gui.setOverlayMessage(Component.literal("Freecam ON"), false);
    }

    public static void disable(boolean notify) {
        Minecraft mc = Minecraft.getInstance();
        if (owner != null && mc.player == owner && savedInput != null) owner.input = savedInput;
        owner = null;
        savedInput = null;
        active = false;
        if (notify && mc.player != null) mc.gui.setOverlayMessage(Component.literal("Freecam OFF"), false);
    }

    /** Called at the start of every client tick. */
    public static void tick(Minecraft mc) {
        if (!active) return;
        if (mc.player == null || mc.player != owner) {
            disable(false); // world changed or player respawned
            return;
        }
        swallow(mc.options.keyAttack);
        swallow(mc.options.keyUse);
        swallow(mc.options.keyPickItem);
        swallow(mc.options.keyDrop);
        swallow(mc.options.keySwapOffhand);
    }

    private static void swallow(KeyMapping km) {
        km.setDown(false);
        while (km.consumeClick()) { /* discard */ }
    }

    /** Mouse look while active (replaces the player's own turn). */
    public static void turn(double dYaw, double dPitch) {
        yaw += (float) (dYaw * 0.15);
        pitch = Math.max(-90f, Math.min(90f, pitch + (float) (dPitch * 0.15)));
    }

    /** Advances the camera for this frame. Called from the camera mixin. */
    public static void step() {
        Minecraft mc = Minecraft.getInstance();
        long now = System.nanoTime();
        double dt = Math.min(0.1, (now - lastNanos) / 1_000_000_000.0);
        lastNanos = now;
        if (mc.screen != null) return;

        double speed = 10.0 * KatanaConfig.get().freecamSpeed * (mc.options.keySprint.isDown() ? 3.0 : 1.0);
        double yr = Math.toRadians(yaw);
        double pr = Math.toRadians(pitch);
        double fx = -Math.sin(yr) * Math.cos(pr), fy = -Math.sin(pr), fz = Math.cos(yr) * Math.cos(pr);
        double rx = -Math.cos(yr), rz = -Math.sin(yr);

        double mx = 0, my = 0, mz = 0;
        if (mc.options.keyUp.isDown()) { mx += fx; my += fy; mz += fz; }
        if (mc.options.keyDown.isDown()) { mx -= fx; my -= fy; mz -= fz; }
        if (mc.options.keyRight.isDown()) { mx += rx; mz += rz; }
        if (mc.options.keyLeft.isDown()) { mx -= rx; mz -= rz; }
        if (mc.options.keyJump.isDown()) my += 1;
        if (mc.options.keyShift.isDown()) my -= 1;
        double len = Math.sqrt(mx * mx + my * my + mz * mz);
        if (len > 1e-6) {
            x += mx / len * speed * dt;
            y += my / len * speed * dt;
            z += mz / len * speed * dt;
        }
    }

    public static double x() { return x; }
    public static double y() { return y; }
    public static double z() { return z; }
    public static float yaw() { return yaw; }
    public static float pitch() { return pitch; }
}
