package fr.olympa.core.bungee.motd;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.UUID;

import com.google.gson.Gson;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MotdListener implements Listener {

	String prefix = "§e-------------§6 Olympa §e-------------";
	String motd_base = "§3⬣ §e§lOlympa §6§n1.9 à 1.15.2+§3 ⬣\n";
	// §6Fun \u2606 Tryhard \u2606 Ranked
	String teamspeak = "§6Teamspeak: §e§nts.olympa.fr";
	String site = "§6Site: §e§nwww.olympa.fr";
	String twitter = "§6Twitter: §e@Olympa_fr";
	String discord = "§6Discord: §e§nwww.discord.olympa.fr";
	String games = "§b§mZTA§c \u00a4§b PvPFaction §c\u00a4 §b§mPvPBox";
	String version = "§cUtilise 1.9 à 1.15+§l✖";
	String reason = "§6Raison de la maintenance :";
	String separator = "§7|";
	String suffix = "§e---------------------------------";

	@EventHandler
	public void onPing(ProxyPingEvent event) {
		// String playerName = event.getConnection().getName();
		// String ip = event.getConnection().getAddress().getAddress().getHostAddress();
		InetSocketAddress virtualHost = event.getConnection().getVirtualHost();
		ServerPing ping = event.getResponse();
		ServerPing.Protocol ver = ping.getVersion();
		ServerPing.Players players = ping.getPlayers();

		// Petit troll pour ceux qui récup des stats
//		if (ip.equals("54.38.31.134")) {
//			players.setOnline(new Random().nextInt(players.getMax()));
//			return;
//		}

		ver.setName(version + " §7" + players.getOnline() + "§8/§7" + players.getMax());
		// ping.setVersion(ver);
		String statusString = BungeeConfigUtils.getConfig("maintenance").getString("settings.status");
		MaintenanceStatus status = MaintenanceStatus.get(statusString);
		if (status == null) {
			status = MaintenanceStatus.DEV;
		}
		if (virtualHost != null) {
			String connectIp = virtualHost.getHostName();
			// System.out.println("ping to " + connectIp + " ping " + new
			// Gson().toJson(ping.getVersion()));
			if (!connectIp.equals("localhost")) {
				String connectDomain = Utils.getAfterFirst(connectIp, ".");
				// Vérifie si l'adresse est correct
				if (!connectDomain.equalsIgnoreCase("olympa.fr") && !connectDomain.equalsIgnoreCase("olympa.net")) {
					ping.setDescriptionComponent(new TextComponent(motd_base + "§4§l⚠ §cUtilise la bonne IP: §4§nplay.olympa.fr"));
					return;
				}
				String connectSubDomain = connectIp.split("\\.")[0];
				if (connectSubDomain.equalsIgnoreCase("buildeur")) {
					ping.setDescriptionComponent(new TextComponent(motd_base + "§aServeur §2Buildeur"));
					return;
				}
			}
		} else {
			System.out.println("Unknow VirtualHost -> what ip the player trie to connect. " + new Gson().toJson(ping.getVersion()));
		}
		switch (status) {
		case OPEN:
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(prefix, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					// new ServerPing.PlayerInfo(this.welcome.replace("%player", playerName) + " " +
					// this.separator2 + " " + this.version, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(games, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(suffix, UUID.randomUUID()),
			});
			if (new Random().nextInt(2) == 0) {
				ping.setDescriptionComponent(new TextComponent(motd_base + games));
			} else {
				StringBuilder sb = new StringBuilder();
				int before = -1;
				for (int i = 0; i < 2; i++) {
					int random;
					do {
						random = new Random().nextInt(4);
					} while (before == random);
					switch (random) {
					case 0:
						sb.append(teamspeak);
						break;
					case 1:
						sb.append(site);
						break;
					case 2:
						sb.append(twitter);
						break;
					case 3:
						sb.append(discord);
						break;
					}
					if (i == 0) {
						sb.append(separator);
					}
					before = random;
				}
				ping.setDescriptionComponent(new TextComponent(motd_base + sb.toString()));
			}
			break;
		case MAINTENANCE:
			String maintenanceMessage = SpigotUtils.color(BungeeConfigUtils.getConfig("maintenance").getString("settings.message"));
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(prefix, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(reason, UUID.randomUUID()),
					new ServerPing.PlayerInfo(maintenanceMessage, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(suffix, UUID.randomUUID()),
			});
			ping.setVersion(new ServerPing.Protocol("§cInfo §nici§7 " + ping.getPlayers().getOnline() + "§8/§7" + ping.getPlayers().getMax(), ping.getVersion().getProtocol() - 1));
			ping.setDescriptionComponent(new TextComponent(motd_base + "§4§l⚠ §cSERVEUR EN MAINTENANCE §4§l⚠"));
			break;
		case DEV:
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(prefix, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§2Serveur en développement depuis", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§2le 18 octobre 2019", UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(suffix, UUID.randomUUID()),
			});
			ping.setVersion(new ServerPing.Protocol("§cInfo §nici§7 " + ping.getPlayers().getOnline() + "§8/§7" + ping.getPlayers().getMax(), ping.getVersion().getProtocol() - 1));
			ping.setDescriptionComponent(new TextComponent(motd_base + "§cServeur en développement"));
			break;
		case SOON:
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(prefix, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§cOuverture très prochainement, suivez-nous", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§csur les réseaux pour plus d'infos.", UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(suffix, UUID.randomUUID()), });
			ping.setVersion(new ServerPing.Protocol("§cInfo §nici§7 " + ping.getPlayers().getOnline() + "§8/§7" + ping.getPlayers().getMax(), ping.getVersion().getProtocol() - 1));
			ping.setDescriptionComponent(new TextComponent(motd_base + "§bOn ouvre bientôt t'inquiète."));
			break;
		case BETA:
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(prefix, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§6Serveur Ouvert en Beta", UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§eInscrit-toi sur notre site pour", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§eêtre pententiellement selectionner", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§eà rejoindre la beta fermer.", UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(suffix, UUID.randomUUID()), });
			ping.setDescriptionComponent(new TextComponent(motd_base + "§c[§6Beta&c] &e-> &binscrit-toi sur www.olympa.fr"));
			break;
		default:
			break;
		}
	}
}
