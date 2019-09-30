package fr.tristiisch.olympa.api.permission;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.tristiisch.olympa.api.objects.OlympaGroup;
import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.core.datamanagment.redis.access.OlympaAccountProvider;
import net.md_5.bungee.api.chat.BaseComponent;

public enum OlympaPermission {

	// TODO change to OlympaGroup.ADMIN
	GROUP_COMMAND(OlympaGroup.DEV),

	CHAT_COMMAND(OlympaGroup.MOD),
	CHAT_SEEINSULTS(OlympaGroup.MOD),
	CHAT_BYPASS(OlympaGroup.MODP),
	CHAT_MUTEDBYPASS(OlympaGroup.MOD),

	BAN_COMMAND(OlympaGroup.MOD),
	BAN_BYPASS_BAN(OlympaGroup.BUILDER),
	BAN_BYPASS_MAXTIME(OlympaGroup.DEV),
	BAN_BYPASS_MINTIME(OlympaGroup.DEV),

	CHAT_COLOR(OlympaGroup.MODP);

	OlympaGroup group;

	private OlympaPermission(OlympaGroup group) {
		this.group = group;
	}

	public OlympaGroup getGroup() {
		return this.group;
	}

	public void getPlayers(Consumer<? super Set<Player>> success) {
		Set<Player> players = Bukkit.getOnlinePlayers().stream().filter(player -> this.hasPermission(new OlympaAccountProvider(player.getUniqueId()).getFromCache())).collect(Collectors.toSet());
		if (!players.isEmpty()) {
			success.accept(players);
		}
	}

	public boolean hasPermission(CommandSender sender) {
		if (sender instanceof Player) {
			return this.hasPermission((Player) sender);
		}
		return true;
	}

	public boolean hasPermission(OlympaGroup group) {
		return group.getPower() >= this.group.getPower();
	}

	public boolean hasPermission(OlympaPlayer olympaPlayer) {
		return olympaPlayer != null && olympaPlayer.hasPower(this.group);
	}

	public boolean hasPermission(Player player) {
		return this.hasPermission(new OlympaAccountProvider(player.getUniqueId()).getFromCache());
	}

	public void sendMessage(BaseComponent baseComponent) {
		this.getPlayers(players -> players.forEach(player -> player.spigot().sendMessage(baseComponent)));
	}

	public void sendMessage(String message) {
		this.getPlayers(players -> players.forEach(player -> player.sendMessage(message)));
	}
}
