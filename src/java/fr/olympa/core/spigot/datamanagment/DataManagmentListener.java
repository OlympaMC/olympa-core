package fr.olympa.core.spigot.datamanagment;

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

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.common.task.OlympaTask;
import fr.olympa.api.spigot.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.spigot.utils.SpigotUtils;
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
					AccountProvider.loadPlayerDatas(olympaPlayer);
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

	@EventHandler
	public void on1PlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		UUID uuid = event.getUniqueId();
		AccountProvider olympaAccount = new AccountProvider(uuid);
		OlympaPlayer olympaPlayer = olympaAccount.getFromCache();
		OlympaCore core = OlympaCore.getInstance();
		ServerStatus status = core.getStatus();
		if (status == ServerStatus.CLOSE) {
			event.disallow(Result.KICK_OTHER, ColorUtils.color("&cLe serveur est fermé, réessaye dans quelques instants..."));
			return;
		}
		if (status == ServerStatus.UNKNOWN) {
			event.disallow(Result.KICK_OTHER, ColorUtils.color("&cImpossible de se connecter au serveur, réessaye dans quelques instants..."));
			return;
		}
		int i = 0;
		while (olympaPlayer == null && i < 10) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (i == 5 || i == 8) {
				olympaPlayer = olympaAccount.getFromRedis();
				LinkSpigotBungee.Provider.link.sendMessage("&4OlympaPlayer de %s pas encore trouvé, tentative de le récupérer via redis n°%d après %s secondes d'attente. Résultat: %s",
						uuid, i == 5 ? 1 : 2, i, olympaPlayer != null);
			} else if (i == 9)
				try {
					olympaPlayer = olympaAccount.fromDb();
					LinkSpigotBungee.Provider.link.sendMessage("&4OlympaPlayer de %s pas encore trouvé, tentative de le récupérer via BDD (risque de perte de données majeure) n°%d. Résultat: %s", uuid, i + 1, olympaPlayer != null);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			else
				olympaPlayer = olympaAccount.getFromCache();
			i++;
		}
		if (olympaPlayer == null) {
			event.disallow(Result.KICK_OTHER, SpigotUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SQLSpigotNoData"));
			return;
		}
		//		if (status.getPermission() != null && !status.getPermission().hasPermission(olympaPlayer)) {
		//			event.disallow(Result.KICK_OTHER, ColorUtils.color("&cLe serveur &4" + core.getServerName() + "&c est actuellement en mode " + status.getNameColored() + "&c."));
		//			return;
		//		}
		olympaAccount.saveToCache(olympaPlayer);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void on2PlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());

		if (olympaPlayer == null) {
			player.kickPlayer(SpigotUtils.connectScreen("§cCette erreur ne peut normalement pas se produire, contacte vite le staff. \n§eCode d'erreur: §l#NucléaireHigh"));
			event.setJoinMessage(null);
			return;
		}
		event.setJoinMessage(ColorUtils.color(String.format("&7[&a+&7] %s", olympaPlayer.getNameWithPrefix())));
		//OlympaCore instance = OlympaCore.getInstance();
		//instance.sendMessage("Version de §6%s§e : §6%s.", player.getName(), instance.getVersionHandler().getVersion(player).getName());
		//		 new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, Reflection.getPlayerConnection(player)));

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void on3PlayerLogin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());

		if (olympaPlayer == null) {
			player.kickPlayer(SpigotUtils.connectScreen("§cCette erreur ne peut normalement pas se produire, contacte vite le staff. \n§eCode d'erreur: §l#NucléaireHighest"));
			event.setJoinMessage(null);
			return;
		}

		OlympaCore.getInstance().launchAsync(() -> {
			try {
				if (player.isOnline()) {
					if (AccountProvider.loadPlayerDatas(olympaPlayer))
						Bukkit.broadcastMessage("§d§k##§6 Bienvenue au joueur " + olympaPlayer.getGroup().getColor() + "§l" + player.getName() + "§6 qui rejoint le serveur ! §d§k##");

					if (player.isOnline()) {
						OlympaPlayerLoadEvent loginevent = new OlympaPlayerLoadEvent(player, olympaPlayer, true);
						Bukkit.getPluginManager().callEvent(loginevent);
					} else
						OlympaCore.getInstance().sendMessage("§c⚠ Le joueur %s s'est déconnecté avant que son OlympaPlayer ne soit complètement chargé.", player.getName());
				} else
					OlympaCore.getInstance().sendMessage("§c⚠ Le joueur %s s'est déconnecté avant que son OlympaPlayer n'ait tenté de se charger.", player.getName());

			} catch (SQLException e) {
				e.printStackTrace();
				OlympaCore.getInstance().getTask().runTask(() -> player.kickPlayer(SpigotUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SQLSpigotJoin")));
				return;
			} catch (Exception e) {
				e.printStackTrace();
				OlympaCore.getInstance().getTask().runTask(() -> player.kickPlayer(SpigotUtils.connectScreen("§cUne erreur est survenue. \n\n§e§lMerci de la signaler au staff.\n§eCode d'erreur: §l#SpigotJoin")));
				return;
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuitHigh(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		AccountProvider account = new AccountProvider(player.getUniqueId());
		OlympaPlayer olympaPlayer = account.getFromCache();
		if (olympaPlayer != null)
			event.setQuitMessage(ColorUtils.format("&7[&c-&7] %s", olympaPlayer.getNameWithPrefix()));
		else
			event.setQuitMessage(null);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuitHighest(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		AccountProvider account = new AccountProvider(player.getUniqueId());
		OlympaPlayer olympaPlayer = account.getFromCache();
		if (olympaPlayer != null) {
			olympaPlayer.unloaded();
			account.saveToRedis(olympaPlayer);
			account.removeFromCache();
		}
	}
}
