package com.katanaclient;

import com.katanaclient.config.KatanaConfig;
import com.katanaclient.freecam.Freecam;
import com.katanaclient.hud.Hud;
import com.katanaclient.ui.KatanaMenuScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KatanaClient implements ClientModInitializer {
    private static KeyMapping menuKey;
    private static KeyMapping freecamKey;

    @Override
    @SuppressWarnings("deprecation")
    public void onInitializeClient() {
        KatanaConfig.load();

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.katanaclient.menu", GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC));
        freecamKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.katanaclient.freecam", GLFW.GLFW_KEY_F7, KeyMapping.Category.MISC));

        HudRenderCallback.EVENT.register((graphics, tickCounter) -> Hud.render(graphics));

        // START tick runs before vanilla handles key presses, so clicks can be swallowed in time.
        ClientTickEvents.START_CLIENT_TICK.register(Freecam::tick);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.consumeClick()) {
                if (client.screen == null) client.setScreen(new KatanaMenuScreen(null));
            }
            while (freecamKey.consumeClick()) {
                if (client.screen == null) Freecam.toggle();
            }
        });
    }
}
