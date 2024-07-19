package redstonedubstep.mods.vanishmod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

public class VanishUtil {
	public static final Set<UUID> VANISHED_PLAYERS = new HashSet<>();
	public static final MutableComponent VANISHMOD_PREFIX = Component.literal("").append(Component.literal("[").withStyle(ChatFormatting.WHITE)).append(Component.literal("Vanishmod").withStyle(s -> s.applyFormat(ChatFormatting.GRAY).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/vanishmod")))).append(Component.literal("] ").withStyle(ChatFormatting.WHITE));

	public static boolean isVanished(Entity player) {
		return isVanished(player, null);
	}

	public static boolean isVanished(Player player) {
		return isVanished(player, null);
	}

	public static boolean isVanished(Entity potentialPlayer, Entity forPlayer) {
		if (potentialPlayer instanceof Player player)
			return isVanished(player, forPlayer);

		return false;
	}

	public static boolean isVanished(Player player, Entity forPlayer) {
		if (VANISHED_PLAYERS.isEmpty())
			return false;

		if (player != null && !player.level().isClientSide) {
			boolean isVanished = VANISHED_PLAYERS.contains(player.getUUID());

			if (forPlayer != null)
				return !playerAllowedToSeeOther(forPlayer, player, isVanished(forPlayer), isVanished);

			return isVanished;
		}

		return false;
	}

	public static boolean playerAllowedToSeeOther(Entity subject, Entity otherPlayer, boolean isSubjectVanished, boolean isOtherVanished) {
		if (subject.equals(otherPlayer)) //All players should be able to see each other
			return true;

		return !isOtherVanished || canSeeAllVanishedPlayers(subject, isSubjectVanished) || checkTeamVisibility(subject, otherPlayer);
	}

	public static boolean canSeeAllVanishedPlayers(Entity entity, boolean isVanished) {
		if (entity instanceof Player player)
			return (VanishConfig.CONFIG.vanishedPlayersSeeEachOther.get() && isVanished) || (VanishConfig.CONFIG.seeVanishedPermissionLevel.get() >= 0 && player.hasPermissions(VanishConfig.CONFIG.seeVanishedPermissionLevel.get()));

		return false;
	}

	public static boolean checkTeamVisibility(Entity player, Entity otherPlayer) {
		if (!VanishConfig.CONFIG.seeVanishedTeamPlayers.get())
			return false;

		Team team = player.getTeam();

		return team != null && team.canSeeFriendlyInvisibles() && team.getPlayers().contains(otherPlayer.getScoreboardName());
	}

	public static void recheckVanished(ServerPlayer player) {
		boolean isMarkedVanished = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean("Vanished");

		if (isMarkedVanished != isVanished(player))
			VanishingHandler.updateVanishedStatus(player, isMarkedVanished);
	}

	public static List<? extends Entity> removeVanishedFromEntityList(List<? extends Entity> rawList, Entity forPlayer) {
		if (VANISHED_PLAYERS.isEmpty())
			return rawList;

		return rawList.stream().filter(entity -> !(entity instanceof Player player) || !isVanished(player, forPlayer)).collect(Collectors.toList());
	}

	public static <T extends Player> List<T> removeVanishedFromPlayerList(List<T> rawList, Entity forPlayer) {
		if (VANISHED_PLAYERS.isEmpty())
			return rawList;

		return rawList.stream().filter(player -> !isVanished(player, forPlayer)).collect(Collectors.toList());
	}

	public static MutableComponent getVanishedStatusText(ServerPlayer player, boolean isVanished) {
		return Component.translatable(isVanished ? VanishConfig.CONFIG.onVanishQuery.get() : VanishConfig.CONFIG.onUnvanishQuery.get(), player.getDisplayName());
	}

	public static ResourceKey<ChatType> getChatTypeRegistryKey(ChatType.Bound chatType, Player player) {
		return player.level().registryAccess().registryOrThrow(Registries.CHAT_TYPE).getResourceKey(chatType.chatType()).orElse(ChatType.CHAT);
	}
}
