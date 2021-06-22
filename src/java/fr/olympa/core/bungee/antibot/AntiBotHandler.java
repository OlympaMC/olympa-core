package fr.olympa.core.bungee.antibot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.player.CachePlayer;
import fr.olympa.api.bungee.player.DataHandler;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import fr.olympa.core.bungee.vpn.OlympaVpn;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;

public class AntiBotHandler {

	public class AntiBotCase {

		@SuppressWarnings("deprecation")
		public boolean canfirstConnectionCrack(PreLoginEvent event) {
			PendingConnection connection = event.getConnection();
			String ip = connection.getAddress().getAddress().getHostAddress();
			if (!newConnectionCracked.contains(ip))
				newConnectionCracked.add(ip);
			if (!enabled)
				return true;
			event.setCancelReason(BungeeUtils.connectScreen("&eBienvenue %s sur Olympa\n" +
					"&6On dirait que tu nous rejoins au mauvais moment, nous subissons une attaque de bot :(\n" +
					"&ePour vérifier que tu n'es pas un robot, tu dois t'inscrire sur le site : &e&nwww.olympa.fr\n" +
					"&6Tu pourra ensuite te connecter ici.\n\n" +
					"&eTu peux aussi attendre, le temps que l'attaque de bots s'arrête.", connection.getName()));
			event.setCancelled(true);
			return false;
		}

		public void queueTooBig(boolean isTooBig) {
			if (isTooBig) {
				queueTooLarge = true;
				if (!enabled)
					setEnable(true, "La pile est trop grande");
			} else if (queueTooLarge)
				queueTooLarge = false;
		}

		public void checkVpn(LoginEvent event, PendingConnection connection, OlympaVpn olympaVpn) {
			CachePlayer cache = DataHandler.get(connection.getName());
			String ip = connection.getAddress().getAddress().getHostAddress();
			boolean isWhitelist = olympaVpn.getWhitelistUsers() != null && olympaVpn.getWhitelistUsers().contains(connection.getName());
			if (isWhitelist)
				return;
			// 1 semaine
			if (enabled && Utils.getCurrentTimeInSeconds() - cache.getOlympaPlayer().getFirstConnection() < 604800 && !"France".equals(olympaVpn.getCountry()))
				event.setCancelReason(BungeeUtils.connectScreen("&eBienvenue %s sur Olympa\n" +
						"&6On dirait que tu nous rejoins au mauvais moment, nous subissons une attaque de bot\n" +
						"&6Tu es l'un des rares joueurs à ne pas pouvoir te connecter, contacte un staff haut gradé\n" +
						"&6(avec ce screen) pour qu'il t'ajoute à la whitelist des VPN manuellement.\n\n" +
						"&eTu peux aussi attendre, le temps que l'attaque de bots s'arrête.", connection.getName()));
			if (olympaVpn.isProxy() || olympaVpn.isHosting()) {
				if (!connectionWithVpn.contains(ip))
					connectionWithVpn.add(ip);
				if (connectionWithVpn.size() > 3)
					setEnable(true, null);
				LinkSpigotBungee.getInstance().sendMessage("&cVPN Détecté en %s Pseudo %s IP %s", olympaVpn.getCountry(), connection.getName(), ip);
				event.setCancelReason(BungeeUtils.connectScreen("&cImpossible d'utiliser un VPN.\n\n&e&lSi tu penses qu'il y a une erreur, contacte un membre du staff."));
				event.setCancelled(true);
			}
		}

	}

	private boolean enabled = false;
	private boolean manualModification = false;
	private List<String> connectionWithVpn = new ArrayList<>();
	private List<String> newConnectionCracked = new ArrayList<>();
	private boolean queueTooLarge = false;
	private int wantToDisable = 0;
	private AntiBotCase cases = new AntiBotCase();

	public AntiBotHandler() {
		OlympaBungee.getInstance().getTask().scheduleSyncRepeatingTask("antibot_reset", this::resetStats, 15, 15, TimeUnit.SECONDS);
	}

	public AntiBotCase getCase() {
		return cases;
	}

	public String getStatus() {
		return enabled ? "&cActivé" : "&2Désactiver";
	}

	public boolean isEnable() {
		return enabled;
	}

	public boolean isNeedToBeToggle() {
		if (enabled) {
			if (!queueTooLarge && connectionWithVpn.isEmpty() && newConnectionCracked.size() < 5)
				if (wantToDisable < 2)
					wantToDisable++;
				else
					return true;
		} else if (!manualModification && (queueTooLarge || connectionWithVpn.size() > 2 || newConnectionCracked.size() > 5))
			return true;
		return false;
	}

	public void enable() {
		if (enabled)
			return;
		enabled = true;
		printInfo(null);
	}

	public void resetStats() {
		if (isNeedToBeToggle()) {
			manualModification = false;
			toggleEnable(null);
		}
		connectionWithVpn.clear();
		newConnectionCracked.clear();
	}

	public void disable() {
		if (!enabled)
			return;
		enabled = false;
		printInfo(null);
	}

	public void setStatus(ProxiedPlayer player, Boolean enable) {
		String playerName = player.getName();
		if (enable != null) {
			setEnable(enable, playerName);
			if (!enable)
				manualModification = true;
			else
				manualModification = false;
		} else {
			toggleEnable(playerName);
			if (!enabled)
				manualModification = true;
			else
				manualModification = false;
		}
		player.sendMessage(Prefix.DEFAULT.formatMessageB("&eL'antibot est désormais %s&7.", getStatus()));
	}

	public void showStatus(ProxiedPlayer player) {
		player.sendMessage(Prefix.DEFAULT.formatMessageB("&eL'antibot est actuellement %s&7.", getStatus()));
	}

	private void toggleEnable(String source) {
		enabled = !enabled;
		printInfo(source);
	}

	private void setEnable(boolean enable, String source) {
		if (enabled == enable)
			return;
		enabled = enable;
		printInfo(source);
	}

	private void printInfo(String source) {
		LinkSpigotBungee link = LinkSpigotBungee.Provider.link;
		String msg = "&cANTIBOT > L'anti bot est maintenant &4%s&c par &4%s&c.";
		Object[] arg = new String[] { enabled ? "activé" : "désactiver", source != null ? source : "automatisme" };
		if (link.isSpigot())
			OlympaCorePermissionsSpigot.SPIGOT_COMMAND_ANTIBOT.sendMessage(msg, arg);
		else
			OlympaCorePermissionsBungee.BUNGEE_COMMAND_ANTIBOT.sendMessage(msg, arg);
		link.sendMessage(msg, arg);
	}
}
