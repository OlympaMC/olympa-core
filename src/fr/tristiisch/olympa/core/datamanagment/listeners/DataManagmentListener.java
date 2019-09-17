package fr.tristiisch.olympa.core.datamanagment.listeners;

import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.tristiisch.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaAccount;
import fr.tristiisch.olympa.api.task.TaskManager;
import fr.tristiisch.olympa.api.utils.SpigotUtils;
import fr.tristiisch.olympa.api.utils.Utils;
import fr.tristiisch.olympa.core.datamanagment.redis.access.OlympaAccountObject;

public class DataManagmentListener implements Listener {

	static {
		TaskManager.runTaskAsynchronously(() -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				OlympaAccountObject olympaAccountObject = new OlympaAccountObject(player.getUniqueId());
				OlympaPlayer olympaPlayer;
				try {
					olympaPlayer = olympaAccountObject.get();
				} catch (SQLException e) {
					player.kickPlayer("§cImpossible de récupérer vos données, merci de réessayer.\n\n§cSi le problème persiste, signalez-le nous.");
					e.printStackTrace();
					continue;
				}
				OlympaPlayerLoadEvent loginevent = new OlympaPlayerLoadEvent(player, olympaPlayer);
				Bukkit.getPluginManager().callEvent(loginevent);
				olympaAccountObject.saveToRedis(olympaPlayer);
				olympaAccountObject.saveToCache(olympaPlayer);
			}
		});
	}

	@EventHandler
	public void onPlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		OlympaAccount olympaAccount = new OlympaAccountObject(player.getUniqueId());
		OlympaPlayer olympaPlayer = olympaAccount.getFromCache();

		if (olympaPlayer == null) {
			return;
		}

		olympaPlayer.setIp(player.getAddress().getAddress().getHostAddress());
		olympaPlayer.setLastConnection(Utils.getCurrentTimeinSeconds());

		OlympaPlayer oldOlympaPlayer = olympaPlayer.clone();
		OlympaPlayerLoadEvent loginevent = new OlympaPlayerLoadEvent(player, olympaPlayer);
		loginevent.setJoinMessage("&7[&a+&7] %prefix%name");
		Bukkit.getPluginManager().callEvent(loginevent);

		if (loginevent.getJoinMessage() != null && !loginevent.getJoinMessage().isEmpty()) {
			Bukkit.broadcastMessage(loginevent.getJoinMessage());
		}

		if (!oldOlympaPlayer.equals(olympaPlayer)) {
			olympaAccount.saveToRedis(olympaPlayer);
		}
		event.setJoinMessage(null);
	}

	@EventHandler
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		UUID uuid = event.getUniqueId();

		OlympaAccount olympaAccount = new OlympaAccountObject(uuid);
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = olympaAccount.get();
		} catch (Exception e) {
			event.disallow(Result.KICK_OTHER, "§cImpossible de récupérer vos données, merci de réessayer.\n\n§cSi le problème persiste, signalez-le nous.");
			e.printStackTrace();
			return;
		}
		if (olympaPlayer == null) {
			boolean isWork = olympaAccount.createNew(olympaPlayer, event.getName(), event.getAddress().getHostAddress());
			if (!isWork) {
				event.disallow(Result.KICK_OTHER, "§cUne erreur de données est survenu, merci de réessayer.");
				return;
			}
		}
		olympaAccount.saveToCache(olympaPlayer);
		olympaAccount.saveToRedis(olympaPlayer);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuitHighest(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		new OlympaAccountObject(player.getUniqueId()).removeFromCache();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuitLow(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = new OlympaAccountObject(player.getUniqueId()).getFromCache();
		if (olympaPlayer != null) {
			event.setQuitMessage(SpigotUtils.color("&7[&c-&7] %prefix%name"
					.replaceAll("%group", olympaPlayer.getGroup().getName())
					.replaceAll("%prefix", olympaPlayer.getGroup().getPrefix())
					.replaceAll("%name", player.getName())));
		} else {
			event.setQuitMessage(null);
		}
	}
}
