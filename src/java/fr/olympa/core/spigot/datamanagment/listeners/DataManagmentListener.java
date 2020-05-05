package fr.olympa.core.spigot.datamanagment.listeners;

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

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class DataManagmentListener implements Listener {

	static {
		OlympaTask task = OlympaCore.getInstance().getTask();
		task.runTaskAsynchronously(() -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				AccountProvider olympaAccountObject = new AccountProvider(player.getUniqueId());
				OlympaPlayer olympaPlayer;
				try {
					olympaPlayer = olympaAccountObject.get();
					MySQL.loadPlayerPluginDatas(olympaPlayer);
				} catch (SQLException e) {
					player.kickPlayer("§cUne erreur est survenue. Merci de réessayer\n\n§e§lSi le problème persiste, signale-le au staff.\n§eCode d'erreur: §l#SQLSpigotReload");
					e.printStackTrace();
					continue;
				}
				task.runTask(() -> {
					OlympaPlayerLoadEvent loginevent = new OlympaPlayerLoadEvent(player, olympaPlayer, false);
					Bukkit.getPluginManager().callEvent(loginevent);
					olympaAccountObject.saveToRedis(olympaPlayer);
					olympaAccountObject.saveToCache(olympaPlayer);
				});
			}
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void on1PlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		UUID uuid = event.getUniqueId();

		AccountProvider olympaAccount = new AccountProvider(uuid);
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = olympaAccount.get();
		} catch (Exception e) {
			event.disallow(Result.KICK_OTHER, SpigotUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SQLSpigotPreLogin"));
			e.printStackTrace();
			return;
		}
		if (olympaPlayer == null) {
			event.disallow(Result.KICK_OTHER, SpigotUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SQLSpigotNoData"));
			return;
		}

		olympaAccount.saveToCache(olympaPlayer);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void on2PlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());

		if (olympaPlayer == null) {
			player.kickPlayer(SpigotUtils.connectScreen("§cCette erreur est impossible, contacte-vite le staff. \n§eCode d'erreur: §l#Nucléaire"));
			event.setJoinMessage(null);
			return;
		}
		event.setJoinMessage(ColorUtils.color("&7[&a+&7] %prefix%name".replace("%group", olympaPlayer.getGroupName()).replace("%prefix", olympaPlayer.getGroupPrefix()).replace("%name", player.getDisplayName())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on3PlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		AccountProvider olympaAccount = new AccountProvider(player.getUniqueId());
		OlympaPlayer olympaPlayer = olympaAccount.getFromCache();

		if (olympaPlayer == null) {
			player.kickPlayer(SpigotUtils.connectScreen("§cCette erreur est impossible, contacte-vite le staff. \n§eCode d'erreur: §l#Nucléaire"));
			return;
		}

		event.setJoinMessage(null);

		OlympaCore.getInstance().launchAsync(() -> {
			try {
				MySQL.loadPlayerPluginDatas(olympaPlayer);

				OlympaPlayerLoadEvent loginevent = new OlympaPlayerLoadEvent(player, olympaPlayer, true);
				Bukkit.getPluginManager().callEvent(loginevent);
			} catch (SQLException e) {
				e.printStackTrace();
				OlympaCore.getInstance().getTask().runTask(() -> {
					player.kickPlayer(SpigotUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SpigotJoin"));
				});
				return;
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuitHigh(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		AccountProvider account = new AccountProvider(player.getUniqueId());
		OlympaPlayer olympaPlayer = account.getFromCache();
		if (olympaPlayer != null) {
			event.setQuitMessage(ColorUtils.color("&7[&c-&7] %prefix%name".replace("%group", olympaPlayer.getGroupName()).replace("%prefix", olympaPlayer.getGroupPrefix()).replace("%name", player.getDisplayName())));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuitHighest(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		AccountProvider account = new AccountProvider(player.getUniqueId());
		OlympaPlayer olympaPlayer = account.getFromCache();
		if (olympaPlayer != null) {
			try {
				MySQL.savePlayerPluginDatas(olympaPlayer);
				account.saveToRedis(olympaPlayer);
				account.removeFromCache();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
