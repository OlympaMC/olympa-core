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

import fr.tristiisch.olympa.api.bossbar.Witherbar;
import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.plugin.OlympaPlugin;
import fr.tristiisch.olympa.api.task.TaskManager;
import fr.tristiisch.olympa.api.utils.SpigotUtils;
import fr.tristiisch.olympa.core.datamanagment.customevent.AsyncOlympaPlayerLoadEvent;
import fr.tristiisch.olympa.core.datamanagment.redis.access.Account;

public class DataManagmentListener implements Listener {

	static {
		TaskManager.runTaskAsynchronously(() -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				Account account = new Account(player.getUniqueId());
				OlympaPlayer olympaPlayer;
				try {
					olympaPlayer = account.get();
				} catch (SQLException e) {
					player.kickPlayer("§cImpossible de récupérer vos données, merci de réessayer.\n\n§cSi le problème persiste, signalez-le nous.");
					e.printStackTrace();
					continue;
				}
				AsyncOlympaPlayerLoadEvent loginevent = new AsyncOlympaPlayerLoadEvent(player, olympaPlayer);
				Bukkit.getPluginManager().callEvent(loginevent);
				account.saveToRedis(olympaPlayer);
				account.saveToCache(olympaPlayer);
			}
		});
	}

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		Account account = new Account(player.getUniqueId());
		OlympaPlayer olympaPlayer = account.getFromCache();

		if (olympaPlayer == null) {
			return;
		}

		OlympaPlayer oldOlympaPlayer = olympaPlayer.clone();
		AsyncOlympaPlayerLoadEvent loginevent = new AsyncOlympaPlayerLoadEvent(player, olympaPlayer);
		Bukkit.getPluginManager().callEvent(loginevent);
		if (!oldOlympaPlayer.equals(olympaPlayer)) {
			account.saveToRedis(olympaPlayer);
		}

		// Witherbar.setProgress(100);
		new Witherbar(OlympaPlugin.getInstance(), SpigotUtils.color("&eBienvenue sur &6Olympa&e !"));
		Witherbar.addPlayer(player);
	}

	@EventHandler
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		UUID uuid = event.getUniqueId();

		Account account = new Account(uuid);
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = account.get();
		} catch (SQLException e) {
			event.disallow(Result.KICK_OTHER, "§cImpossible de récupérer vos données, merci de réessayer.\n\n§cSi le problème persiste, signalez-le nous.");
			e.printStackTrace();
			return;
		}
		if (olympaPlayer == null) {
			boolean isWork = account.createNew(olympaPlayer, event.getName(), event.getAddress().getHostAddress());
			if (!isWork) {
				event.disallow(Result.KICK_OTHER, "§cUne erreur de données est survenu, merci de réessayer.");
				return;
			}
		}
		account.saveToCache(olympaPlayer);
		account.saveToRedis(olympaPlayer);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		new Account(player.getUniqueId()).removeFromCache();
	}
}
