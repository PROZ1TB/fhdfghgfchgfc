package com.katanaclient.mixin;

import com.katanaclient.freecam.Freecam;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    /** While freecam is on, mouse look turns the camera instead of the player. */
    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void katana$turn(double yRot, double xRot, CallbackInfo ci) {
        if (Freecam.isActive() && (Object) this instanceof LocalPlayer) {
            Freecam.turn(yRot, xRot);
            ci.cancel();
        }
    }
}
