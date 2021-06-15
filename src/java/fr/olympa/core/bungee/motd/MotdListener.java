package fr.olympa.core.bungee.motd;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.UUID;

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

	String prefix = "§e-------------§6 Olympa §e-------------";
	public static String MOTD_BASE = Chat.centerMotD("§3⬣ §e§lOlympa §6" + ProtocolAPI.getFirstVersion().getName() + " à " + ProtocolAPI.getLastVersion().getName() + "§3 ⬣") + "\n";
	// §6Fun \u2606 Tryhard \u2606 Ranked
	String teamspeak = "§6Teamspeak: §e§nts.olympa.fr";
	String site = "§6Site: §e§nwww.olympa.fr";
	String twitter = "§6Twitter: §e@Olympa_fr";
	String discord = "§6Discord: §e§ndiscord.olympa.fr";
	String games = "§bPvPFaction§c ¤§3 ZTA §c¤ §b§mCréatif";
	String version = "§cUtilise la " + ProtocolAPI.getLastVersion().getName() + "§l✖";
	String reason = "§6Raison de la maintenance :";
	String separator = " §7| ";
	String suffix = "§e---------------------------------";

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPing(ProxyPingEvent event) {
		// String playerName = event.getConnection().getName();
		String ip = event.getConnection().getAddress().getAddress().getHostAddress();
		InetSocketAddress virtualHost = event.getConnection().getVirtualHost();
		ServerPing ping = event.getResponse();
		ServerPing.Protocol ver = ping.getVersion();
		ServerPing.Players players = ping.getPlayers();

		// Petit troll pour ceux qui récup des stats
		if (ip.equals("54.38.31.134")) {
			players.setOnline(new Random().nextInt(10000));
			return;
		}

		ver.setName(version + " §7" + players.getOnline() + "§8/§7" + players.getMax());
		// ping.setVersion(ver);
		Configuration config = OlympaBungee.getInstance().getMaintConfig();
		String statusString = config.getString("settings.status");
		ServerStatus status = ServerStatus.get(statusString);
		if (status == null)
			status = ServerStatus.DEV;
		if (virtualHost != null) {
			String connectIp = virtualHost.getHostName();
			// System.out.println("ping to " + connectIp + " ping " + new
			// Gson().toJson(ping.getVersion()));
			if (!connectIp.equals("localhost")) {
				String connectDomain = Utils.getAfterFirst(connectIp, ".");
				// Vérifie si l'adresse est correct
				if (!connectDomain.equalsIgnoreCase("olympa.fr") && !connectDomain.equalsIgnoreCase("olympa.net")) {
					ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§4§l⚠ §cUtilise la bonne IP: §4§nplay.olympa.fr")));
					return;
				}
				String connectSubDomain = connectIp.split("\\.")[0];
				if (connectSubDomain.equalsIgnoreCase("buildeur")) {
					ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§aServeur §2Buildeur")));
					return;
				}
			}
		}
		switch (status) {
		case OPEN:
			//			players.setSample(new ServerPing.PlayerInfo[] {
			//					new ServerPing.PlayerInfo(prefix, UUID.randomUUID()),
			//					new ServerPing.PlayerInfo("", UUID.randomUUID()),
			//					// new ServerPing.PlayerInfo(this.welcome.replace("%player", playerName) + " " +
			//					// this.separator2 + " " + this.version, UUID.randomUUID()),
			//					new ServerPing.PlayerInfo("", UUID.randomUUID()),
			//					new ServerPing.PlayerInfo(games, UUID.randomUUID()),
			//					new ServerPing.PlayerInfo("", UUID.randomUUID()),
			//					new ServerPing.PlayerInfo(teamspeak, UUID.randomUUID()),
			//					new ServerPing.PlayerInfo(twitter, UUID.randomUUID()),
			//					new ServerPing.PlayerInfo(discord, UUID.randomUUID()),
			//					new ServerPing.PlayerInfo(site, UUID.randomUUID()),
			//					new ServerPing.PlayerInfo("", UUID.randomUUID()),
			//					new ServerPing.PlayerInfo(suffix, UUID.randomUUID()),
			//			});
			players.setSample(new PlayerInfoBuilder().append("").append("").append(games).append("").append(discord).append(teamspeak).append(twitter).append(site).append("").build());
			if (new Random().nextInt(2) == 0)
				ping.setDescriptionComponent(new TextComponent(MOTD_BASE + games));
			else {
				StringBuilder sb = new StringBuilder();
				int before = -1;
				for (int i = 0; i < 2; i++) {
					int random;
					do
						random = new Random().nextInt(4);
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
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(prefix, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(reason, UUID.randomUUID()),
					new ServerPing.PlayerInfo(maintenanceMessage, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(suffix, UUID.randomUUID()),
			});
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
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(prefix, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§2Serveur en développement depuis", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§2le 18 octobre 2019", UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§22 Bêta privées ont déjà été faites", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§2Une prochaine bêta en 2021 ?", UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(suffix, UUID.randomUUID()),
			});
			ping.setVersion(new ServerPing.Protocol(ColorUtils.randomColor() + "Info §nici§8 - §7" + ping.getPlayers().getOnline() + "§8/§7" + ping.getPlayers().getMax(), ping.getVersion().getProtocol() - 1));
			ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§cServeur en développement")));
			break;
		case SOON:
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(prefix, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§cOuverture très prochainement, suivez-nous", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§csur les réseaux pour plus d'infos.", UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(suffix, UUID.randomUUID()), });
			ping.setVersion(new ServerPing.Protocol("§cInfo §nici§7 " + ping.getPlayers().getOnline() + "§8/§7" + ping.getPlayers().getMax(), ping.getVersion().getProtocol() - 1));
			ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§bOn ouvre bientôt t'inquiète.")));
			break;
		case BETA:
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(prefix, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§6Serveur Ouvert en Bêta", UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§eInscrit-toi sur notre site pour", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§eêtre pententiellement selectionner", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§eà rejoindre la beta.", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§cIl y a de place, tu as toutes tes chances..", UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(suffix, UUID.randomUUID()), });
			ping.setDescriptionComponent(new TextComponent(MOTD_BASE + Chat.centerMotD("§c[§6Beta&c] &e-> &binscrit-toi sur www.olympa.fr")));
			break;
		case CLOSE_BETA:
			players.setSample(new PlayerInfoBuilder()
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
