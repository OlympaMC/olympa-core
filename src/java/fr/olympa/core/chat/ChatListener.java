package fr.olympa.core.chat;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.olympa.OlympaCore;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.objects.OlympaServerSettings;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.chat.Chat.OlympaChat;

public class ChatListener implements Listener {

	/*
	 * Dev: Tristiisch74 Permet d'enlever les IP, les liens, les insultes, le flood,
	 * les full maj, les doubles messages...
	 */
	private List<Pattern> regex_swear = new ArrayList<>();
	// private Pattern matchIpv6 = Pattern.compile(
	// "^(?>(?>([a-f0-9]{1,4})(?>:(?1)){7}|(?!(?:.*[a-f0-9](?>:|$)){8,})((?1)(?>:(?1)){0,6})?::(?2)?)|(?>(?>(?1)(?>:(?1)){5}:|(?!(?:.*[a-f0-9]:){6,})(?3)?::(?>((?1)(?>:(?1)){0,4}):)?)?(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])(?>.(?4)){3}))$");
	private Pattern matchIpv4 = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
	private Pattern matchLink = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");
	private Pattern matchFlood = Pattern.compile("(.)\\1{2,}");

	public ChatListener() {

		// Récupère la config et convertie String => Regex
		List<String> swear_list = OlympaCore.getInstance().getConfig().getStringList("chat.insult");
		for (String swear : swear_list) {
			String swears = "";
			String b = "\\b";
			if (swear.startsWith("|")) {
				b = "";
				swear = swear.substring(1);
			}
			for (char s : swear.toCharArray()) {
				swears += s + "+(\\W|\\d|_)*";
			}
			this.regex_swear.add(Pattern.compile("(?iu)" + b + "(" + swears + ")" + b));
		}
	}

	@EventHandler
	public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		OlympaServerSettings serverSettings = OlympaServerSettings.getInstance();

		OlympaPlayer olympaPlayer = new AccountProvider(player.getUniqueId()).getFromCache();

		OlympaChat olympaTchat = Chat.getPlayer(player.getUniqueId());

		// Si le chat est mute, cancel message
		if (serverSettings.isChatMute()) {
			if (OlympaCorePermissions.CHAT_MUTEDBYPASS.hasPermission(olympaPlayer)) {
				player.sendMessage(SpigotUtils.color(Prefix.INFO + "Le chat est désactivé pour les autres joueurs."));
				return;
			}
			player.sendMessage(SpigotUtils.color(Prefix.BAD + "Le chat est désactivé."));
			event.setCancelled(true);
			return;
		}

		if (OlympaCorePermissions.CHAT_BYPASS.hasPermission(olympaPlayer)) {
			return;
		}

		String message = event.getMessage();
		String msgNFD = Normalizer.normalize(message, Form.NFD);
		long currentTime = Utils.getCurrentTimeinSeconds();
		long time = currentTime - olympaTchat.getLastMsgTime();
		if (olympaTchat.isLastMsg(msgNFD) && time < 10) {
			event.setCancelled(true);
			player.sendMessage(SpigotUtils.color(Prefix.BAD + "Merci de ne pas répéter le même message."));
			return;
		}

		// Si le chat est slow, met un cooldown entre chaque message
		if (time < serverSettings.getTimeCooldown() && serverSettings.isChatSlow()) {
			event.setCancelled(true);
			player.sendMessage(SpigotUtils.color(Prefix.BAD + "Merci de patienter %second secondes entre chaque messages."
					.replaceFirst("%second", String.valueOf(serverSettings.getTimeCooldown()))));
			olympaTchat.setLastMsgTime(currentTime);
			return;
		}
		olympaTchat.setLastMsgTime(currentTime);

		// Si le message contient des liens, cancel message
		Matcher matcher = this.matchLink.matcher(msgNFD);
		if (matcher.find()) {
			String link = matcher.group();
			Set<String> linkWhitelist = new HashSet<>(OlympaCore.getInstance().getConfig().getStringList("chat.linkwhitelist"));
			if (!linkWhitelist.stream().filter(l -> link.contains(l)).findFirst().isPresent()) {
				event.setCancelled(true);
				player.sendMessage(SpigotUtils.color(Prefix.BAD + "Les liens sont interdits."));
				Chat.sendToStaff("Lien", player, message);
				return;
			}
		}

		// Si le message contient des ips, cancel message
		matcher = this.matchIpv4.matcher(msgNFD);
		if (matcher.find()) {
			event.setCancelled(true);
			player.sendMessage(SpigotUtils.color(Prefix.BAD + "Les adresses IPs sont interdites."));
			Chat.sendToStaff("IP", player, message);
			return;
		}

		/**matcher = this.matchIpv6.matcher(msgNFD);
		if (matcher.find()) {
			event.setCancelled(true);
			player.sendMessage(SpigotUtils.color(Prefix.BAD + "Les adresses IPv6 sont interdites."));
			Chat.sendToStaff("IP", player, message);
			return;
		}**/

		// Si le message contient des insultes, cancel message
		for (Pattern regex : this.regex_swear) {
			matcher = regex.matcher(msgNFD);
			if (matcher.find() && Bukkit.getPlayer(matcher.group()) == null) {
				event.setCancelled(true);
				player.sendMessage(SpigotUtils.color(Prefix.BAD + "Merci de rester correct"));
				Chat.sendToStaff("Insulte", player, message);
				return;
			}
		}

		// Si le message contient des liens, cancel message
		/*
		 * matcher = this.symbole.matcher(msgNFD); if(matcher.find()) {
		 * event.setCancelled(true); String find = matcher.group();
		 * player.sendMessage(Utils.
		 * color("&c➤ Merci de ne pas utiliser de caratères spéciaux tel que &r") + find
		 * + Utils.color("&c.")); Tchat.sendToStaff("Symboles " + find, player,
		 * message); return; }
		 */

		// Si le message contient trop de majuscules, les enlever
		if (msgNFD.length() >= serverSettings.getBlockCaps()) {
			int uppers = (int) msgNFD.chars().filter(c -> Character.isUpperCase(c)).count();

			if (Math.round(uppers * 1.0 / (msgNFD.length() * 1.0) * 100.0) > serverSettings.getMaxCaps()) {
				event.setMessage(message.toLowerCase().replaceFirst(".", String.valueOf(message.toLowerCase().charAt(0)).toUpperCase()));
				player.sendMessage(SpigotUtils.color(Prefix.BAD + "Merci d'éviter de mettre trop de majuscules."));
			}
		}

		// Si le message contient trop du flood, les enlever [EN TEST]
		// TODO check players names
		try {
			matcher = this.matchFlood.matcher(message);

			if (matcher.find() && Bukkit.getPlayer(matcher.group(0)) == null) {
				do {
					String isNotLetter = "";
					if (!matcher.group(1).matches("[a-zA-Z0-9]")) {
						isNotLetter = "\\";
					}
					message = message.replaceFirst("(" + isNotLetter + matcher.group(0) + ")\\1{2,}", "");
					matcher = this.matchFlood.matcher(message);
				} while (matcher.find() && Bukkit.getPlayer(matcher.group(1)) == null);

				event.setMessage(message);
				player.sendMessage(SpigotUtils.color(Prefix.BAD + "Merci d'éviter le flood."));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		olympaTchat.setLastMsg(msgNFD);
	}
}
