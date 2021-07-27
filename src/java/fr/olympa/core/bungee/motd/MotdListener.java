package fr.olympa.core.bungee.motd;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;

import javax.imageio.ImageIO;

import fr.olympa.api.common.chat.Chat;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

public class MotdListener implements Listener {

	public static final String MOTD_BASE = Chat.centerMotD("§3⬣ §e§lOlympa §6" + ProtocolAPI.getFirstVersion().getName() + " à " + ProtocolAPI.getLastVersion().getName() + "§3 ⬣") + "§r\n";
	String teamspeak = "§6Teamspeak: §e§nts.olympa.fr§r";
	String site = "§6Site: §e§nwww.olympa.fr§r";
	String twitter = "§6Twitter: §e@Olympa_fr";
	String discord = "§6Discord: §e§ndiscord.olympa.fr§r";
	String games = "§bPvP-Kits §c¤§3 ZTA §c¤ §bCréatif §c¤ §bPvP-Factions§r";
	String pvp = "§7Le PvP fonctionne comme en 1.8";
	String version = "§cUtilise la " + ProtocolAPI.getLastVersion().getName() + "§l✖§r";
	String reason = "§6Raison de la maintenance :";
	String separator = " §7| ";

	@EventHandler
	public void onPing(ProxyPingEvent event) {
		InetSocketAddress virtualHost = event.getConnection().getVirtualHost();
		ServerPing ping = event.getResponse();
		ServerPing.Protocol ver = ping.getVersion();
		ServerPing.Players players = ping.getPlayers();

		// Petit troll pour ceux qui récup des stats
		//		if (ip.equals("54.38.31.134")) {
		//			players.setOnline(new Random().nextInt(10000));
		//			return;
		//		}

		ver.setName(version + " §7" + players.getOnline() + "§8/§7" + players.getMax());
		// ping.setVersion(ver);
		Configuration config = OlympaBungee.getInstance().getMaintConfig();
		String statusString = config.getString("settings.status");
		ServerStatus status = ServerStatus.get(statusString);
		if (status == null)
			status = ServerStatus.DEV;
		String connectIp = null;
		String connectDomain = null;
		if (virtualHost != null) {
			connectIp = virtualHost.getHostName();
			connectDomain = Utils.getAfterFirst(connectIp, ".");
		}
		if (connectIp == null || !connectIp.equals("localhost")) {
			// Vérifie si l'adresse est correct
			if (connectIp == null || !connectDomain.equalsIgnoreCase("olympa.fr") && !connectDomain.equalsIgnoreCase("olympa.net")) {
				ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§4§l⚠ §cUtilise la bonne IP: §4§nplay.olympa.fr§r")));
				return;
			}
			String connectSubDomain = connectIp.split("\\.")[0];
			if (connectSubDomain.equalsIgnoreCase("buildeur")) {
				ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§aServeur §2Buildeur§r")));
				return;
			} else if (connectSubDomain.equalsIgnoreCase("dev")) {
				ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§aServeur §2Développeur§r")));
				return;
			}
		}

		switch (status) {
		case OPEN:
			players.setSample(new PlayerInfoBuilder().append("").append(games).append(pvp).append("").append(discord).append(teamspeak).append(twitter).append(site).append("").build());
			int random = new Random().nextInt(4);
			if (random == 0)
				ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD(games)));
			else if (random == 1)
				ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD(pvp)));
			else {
				StringBuilder sb = new StringBuilder();
				int before = -1;
				for (int i = 0; i < 2; i++) {
					do
						random = new Random().nextInt(5);
					while (before == random);
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
					case 4:
						sb.append(pvp);
						break;
					}
					if (i == 0)
						sb.append(separator);
					before = random;
				}
				ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD(sb.toString())));
			}
			break;
		case MAINTENANCE:
			try {
				File file = new File("maintenance.png");
				if (!file.exists())
					return;
				BufferedImage in = ImageIO.read(new File("maintenance.png"));
				ping.setFavicon(Favicon.create(in));
			} catch (IOException e) {
				e.printStackTrace();
			}
			String maintenanceMessage = ColorUtils.color(config.getString("settings.message"));
			players.setSample(new PlayerInfoBuilder().append("").append(games).append(pvp).append("").append(reason)
					.append(maintenanceMessage).append("").append(discord).append(teamspeak).append(twitter).append(site).append("").build());
			ping.setVersion(new ServerPing.Protocol("§cInfo §nici§8 - §7" + ping.getPlayers().getOnline() + "§8/§7" + ping.getPlayers().getMax(), ping.getVersion().getProtocol() - 1));
			ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§4§l⚠ §cSERVEUR EN MAINTENANCE §4§l⚠")));
			break;
		case DEV:
			try {
				File file = new File("maintenance.png");
				if (!file.exists())
					return;
				BufferedImage in = ImageIO.read(new File("maintenance.png"));
				ping.setFavicon(Favicon.create(in));
			} catch (IOException e) {
				e.printStackTrace();
			}
			players.setSample(new PlayerInfoBuilder().append("").append(games).append(pvp).append("")
					.append("§2Serveur en développement depuis")
					.append("§2le 18 octobre 2019")
					.append("").append(discord).append(teamspeak).append(twitter).append(site).append("").build());
			ping.setVersion(new ServerPing.Protocol(ColorUtils.randomColor() + "Info §nici§8 - §7" + ping.getPlayers().getOnline() + "§8/§7" + ping.getPlayers().getMax(), ping.getVersion().getProtocol() - 1));
			ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§cServeur en développement")));
			break;
		case SOON:
			players.setSample(new PlayerInfoBuilder().append("").append(games).append(pvp).append("")
					.append("§cOuverture très prochainement, suivez-nous")
					.append("§csur les réseaux pour plus d'informations.")
					.append("").append(discord).append(teamspeak).append(twitter).append(site).append("").build());
			ping.setVersion(new ServerPing.Protocol("§cInfo §nici§7 " + ping.getPlayers().getOnline() + "§8/§7" + ping.getPlayers().getMax(), ping.getVersion().getProtocol() - 1));
			ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§dOuverture prochaine!")));
			break;
		case BETA:
			players.setSample(new PlayerInfoBuilder().append("").append(games).append(pvp).append("")
					.append("§6Serveur Ouvert en Bêta")
					.append("§eInscrit-toi sur notre site pour")
					.append("§eêtre pententiellement selectionner")
					.append("§eà rejoindre la bêta.")
					.append("§cIl y a de place, tu as toutes tes chances...")
					.append("").append(discord).append(teamspeak).append(twitter).append(site).append("").build());
			ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§c[§6Bêta&c] &e-> &binscrit-toi sur www.olympa.fr")));
			break;
		case CLOSE_BETA:
			players.setSample(new PlayerInfoBuilder()
					.append("")
					.append(games).append(pvp)
					.append("")
					.append("&cBêta Fermée")
					.append("")
					.append("&eSeul le staff et quelques amis")
					.append("&eont accès au serveur.")
					.append("")
					.append(discord)
					.append(teamspeak)
					.append(twitter)
					.append(site)
					.append("")
					.build());
			ping.setVersion(new ServerPing.Protocol("§cInfo §nici§7 " + ping.getPlayers().getOnline() + "§8/§7" + ping.getPlayers().getMax(), ping.getVersion().getProtocol() - 1));
			ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§6Bêta fermée")));
			break;
		default:
			break;
		}
	}
}
