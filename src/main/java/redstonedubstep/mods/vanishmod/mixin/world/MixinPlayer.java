package redstonedubstep.mods.vanishmod.mixin.world;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import redstonedubstep.mods.vanishmod.VanishConfig;
import redstonedubstep.mods.vanishmod.VanishUtil;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity {
	//LivingEntity needs a constructor, so here we go
	public MixinPlayer(Level world) {
		super(EntityType.PLAYER, world);
	}

	//Suppress arm swing sound when hitting the player
	@Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
	public void redirectPlaySound(Level world, @Nullable Player player, double x, double y, double z, SoundEvent soundIn, SoundSource category, float volume, float pitch) {
		if (!VanishConfig.CONFIG.hidePlayersFromWorld.get() || !VanishUtil.isVanished(getCommandSenderWorld().getPlayerByUUID(getUUID())))
			world.playSound(player, x, y, z, soundIn, category, volume, pitch);
	}

	//Fixes that the night can be skipped in some instances when a vanished player is sleeping
	@Inject(method = "isSleepingLongEnough", at = @At("HEAD"), cancellable = true)
	private void onIsSleepingLongEnough(CallbackInfoReturnable<Boolean> callbackInfo) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(getCommandSenderWorld().getPlayerByUUID(getUUID())))
			callbackInfo.setReturnValue(false);
	}

	//Suppress burping sound when a vanished player finishes eating
	@ModifyArg(method = "eat", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
	private SoundEvent removeBurpSound(SoundEvent soundEvent) {
		if (VanishConfig.CONFIG.hidePlayersFromWorld.get() && VanishUtil.isVanished(getCommandSenderWorld().getPlayerByUUID(getUUID())))
			soundEvent = null;

		return soundEvent;
	}
}