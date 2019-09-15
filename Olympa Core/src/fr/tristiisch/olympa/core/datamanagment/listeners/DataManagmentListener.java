package fr.tristiisch.olympa.core.datamanagment.listeners;

import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.tristiisch.olympa.api.customevents.AsyncOlympaPlayerLoadEvent;
import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaAccount;
import fr.tristiisch.olympa.api.task.TaskManager;
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
				AsyncOlympaPlayerLoadEvent loginevent = new AsyncOlympaPlayerLoadEvent(player, olympaPlayer);
				Bukkit.getPluginManager().callEvent(loginevent);
				olympaAccountObject.saveToRedis(olympaPlayer);
				olympaAccountObject.saveToCache(olympaPlayer);
			}
		});
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		OlympaAccount olympaAccount = new OlympaAccountObject(player.getUniqueId());
		OlympaPlayer olympaPlayer = olympaAccount.getFromCache();

		if (olympaPlayer == null) {
			return;
		}

		OlympaPlayer oldOlympaPlayer = olympaPlayer.clone();
		AsyncOlympaPlayerLoadEvent loginevent = new AsyncOlympaPlayerLoadEvent(player, olympaPlayer);
		Bukkit.getPluginManager().callEvent(loginevent);
		if (!oldOlympaPlayer.equals(olympaPlayer)) {
			olympaAccount.saveToRedis(olympaPlayer);
		}
	}

	@EventHandler
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		UUID uuid = event.getUniqueId();

		OlympaAccount olympaAccount = new OlympaAccountObject(uuid);
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = olympaAccount.get();
		} catch (SQLException e) {
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

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		new OlympaAccountObject(player.getUniqueId()).removeFromCache();
	}
}
