package fr.olympa.core.bungee.motd;

import java.util.Random;
import java.util.UUID;

import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MotdListener implements Listener {

	String motd_base = "§3⬣ §e§lOlympa §6§n1.15.1§3 ⬣\n";
	// §6Fun \u2606 Tryhard \u2606 Ranked
	String teamspeak = "§7Teamspeak: §8§nts.olympa.fr";
	String site = "§eSite: §6§nwww.olympa.fr";
	String twitter = "§bTwitter: §3@Olympa_fr";
	String discord = "§dDiscord: §5§nwww.olympa.fr/discord";
	String games = "§b§mZTA§c \u00a4§b PvPFaction §c\u00a4 §b§mPvP Box";
	String welcome = "§eBienvenue sur Olympa §6%player";
	String version = "§cVersion 1.15.1§l✖";
	String reason = "§6Raison de la maintenance :";
	String separator2 = "§7|";
	String separator = "§e--------------------------------";

	@EventHandler
	public void onPing(ProxyPingEvent event) {
		String playerName = event.getConnection().getName();
		ServerPing ping = event.getResponse();
		ServerPing.Protocol ver = ping.getVersion();
		ver.setName(this.version);
		ping.setVersion(ver);
		ServerPing.Players players = ping.getPlayers();
		Byte status = BungeeConfigUtils.getConfig("maintenance").getByte("settings.status");
		if (status == 0) {
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(this.separator, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.welcome.replace("%player", playerName) + " " + this.separator2 + " " + this.version, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.games, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.separator, UUID.randomUUID()),
			});

			if (new Random().nextInt(2) == 0) {
				ping.setDescriptionComponent(new TextComponent(this.motd_base + this.games));
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
						sb.append(this.teamspeak);
						break;

					case 1:
						sb.append(this.site);
						break;

					case 2:
						sb.append(this.twitter);
						break;

					case 3:
						sb.append(this.discord);
						break;
					}
					if (i == 0) {
						sb.append(this.separator2);
					}
					before = random;
				}

				ping.setDescriptionComponent(new TextComponent(this.motd_base + sb.toString()));
			}

		} else if (status == 1) {
			String maintenanceMessage = SpigotUtils.color(BungeeConfigUtils.getConfig("maintenance").getString("settings.message"));
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(this.separator, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.reason, UUID.randomUUID()),
					new ServerPing.PlayerInfo(maintenanceMessage, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.separator, UUID.randomUUID()),
			});
			ping.setVersion(new ServerPing.Protocol("§c§lINFOS ICI §4§l➤", ping.getVersion().getProtocol() - 1));
			ping.setDescriptionComponent(new TextComponent(this.motd_base + "§4§l⚠ §cSERVEUR EN MAINTENANCE §4§l⚠"));
		} else if (status == 2) {
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(this.separator, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.welcome.replace("%player", playerName) + " " + this.separator2 + " " + this.version, UUID.randomUUID()),
					new ServerPing.PlayerInfo("§2Serveur en développement depuis", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§2le 1 juillet 2019", UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.separator, UUID.randomUUID()),
			});
			ping.setDescriptionComponent(new TextComponent(this.motd_base + "§cServeur en développement"));
		} else if (status == 3) {
			players.setSample(new ServerPing.PlayerInfo[] {
					new ServerPing.PlayerInfo(this.separator, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§cOuverture très prochainement, suivez-nous", UUID.randomUUID()),
					new ServerPing.PlayerInfo("§csur les réseaux pour plus d'infos.", UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.teamspeak, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.twitter, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.discord, UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.site, UUID.randomUUID()),
					new ServerPing.PlayerInfo("", UUID.randomUUID()),
					new ServerPing.PlayerInfo(this.separator, UUID.randomUUID()), });
			ping.setVersion(new ServerPing.Protocol("§c§lINFOS ICI §4§l➤", ping.getVersion().getProtocol() - 1));
			ping.setDescriptionComponent(new TextComponent(this.motd_base + "§bHé " + playerName + ", on ouvre bientôt t'inquiète."));
		}
	}
}
