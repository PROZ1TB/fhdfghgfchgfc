package com.katanaclient.mixin;

import com.katanaclient.freecam.Freecam;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow protected abstract void setPosition(double x, double y, double z);

    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Shadow private boolean detached;

    /** Runs after vanilla positions the camera each frame; overrides it while freecam is on. */
    @Inject(method = {"setup", "update"}, at = @At("TAIL"), require = 1)
    private void katana$freecam(CallbackInfo ci) {
        if (!Freecam.isActive()) return;
        Freecam.step();
        this.setRotation(Freecam.yaw(), Freecam.pitch());
        this.setPosition(Freecam.x(), Freecam.y(), Freecam.z());
        this.detached = true; // draws the player model, hides the first-person hand
    }
}
