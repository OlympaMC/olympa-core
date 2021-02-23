package fr.olympa.core.spigot.chat;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.server.OlympaServerSettings;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.chat.Chat.OlympaChat;

public class CancerListener implements Listener {

	// private Pattern matchIpv6 = Pattern.compile(
	// "^(?>(?>([a-f0-9]{1,4})(?>:(?1)){7}|(?!(?:.*[a-f0-9](?>:|$)){8,})((?1)(?>:(?1)){0,6})?::(?2)?)|(?>(?>(?1)(?>:(?1)){5}:|(?!(?:.*[a-f0-9]:){6,})(?3)?::(?>((?1)(?>:(?1)){0,4}):)?)?(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])(?>.(?4)){3}))$");
	private Pattern matchIpv4 = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
	private Pattern matchLink = Pattern.compile("(https?:\\/\\/(www\\.)?)?([-\\w]+(\\.|[0-9]))+(fr|org|net|com|xxx|name|xyr|gg|ly|be|lu)\\S*");
	private Pattern matchLink2 = Pattern.compile("^(https?:\\/\\/)?(www|play|pvp)\\.([-\\w]+(\\.|[0-9]))+(fr|org|net|com|xxx|name|xyr|gg|ly|be|lu|shop)$");
	private Pattern matchFlood = Pattern.compile("(.)\\1{3,}");
	private Pattern matchNoWord = Pattern.compile("[^\\w\\sàáâãäåçèéêëìíîïðòóôõöùúûüýÿÀÁÂÃÄÅÇÈÉÊËÌÍÎÏÐÒÓÔÕÖÙÚÛÜÝŸ\\-=+÷²!@#%^&§*();,.?\\\"':{}|\\[\\]<>~\\\\€$£µ\\/°]+");

	@EventHandler
	public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
		if (event.isCancelled())
			return;

		Player player = event.getPlayer();
		OlympaServerSettings serverSettings = OlympaServerSettings.getInstance();

		OlympaPlayer olympaPlayer = new AccountProvider(player.getUniqueId()).getFromCache();

		OlympaChat olympaTchat = Chat.getPlayer(player.getUniqueId());

		// Si le chat est mute, cancel message
		if (serverSettings.isChatMute()) {
			if (OlympaCorePermissions.CHAT_MUTEDBYPASS.hasPermission(olympaPlayer)) {
				player.sendMessage(ColorUtils.color(Prefix.INFO + "Le chat est désactivé pour les autres joueurs."));
				return;
			}
			player.sendMessage(ColorUtils.color(Prefix.BAD + "Le chat est désactivé."));
			event.setCancelled(true);
			return;
		}

		if (OlympaCorePermissions.CHAT_BYPASS.hasPermission(olympaPlayer))
			return;

		String message = event.getMessage();
		String msgNFD = Normalizer.normalize(message, Form.NFD);
		long currentTime = Utils.getCurrentTimeInSeconds();
		long time = currentTime - olympaTchat.getLastMsgTime();
		if (olympaTchat.isLastMsg(msgNFD) && time < 10) {
			event.setCancelled(true);
			player.sendMessage(ColorUtils.color(Prefix.BAD + "Merci de ne pas répéter le même message."));
			return;
		}

		// Si le chat est slow, met un cooldown entre chaque message
		if (serverSettings.isChatSlow() && time < serverSettings.getTimeCooldown()) {
			event.setCancelled(true);
			player.sendMessage(ColorUtils.join(Prefix.BAD + "Merci de patienter %s secondes entre chaque messages.", String.valueOf(serverSettings.getTimeCooldown())));
			olympaTchat.setLastMsgTime(currentTime);
			return;
		}
		olympaTchat.setLastMsgTime(currentTime);

		// Si le message contient des liens, cancel message
		Matcher matcher = matchLink.matcher(msgNFD);
		Matcher matcher2 = matchLink2.matcher(msgNFD);
		if (matcher.find() || matcher2.find()) {
			String link;
			if (matcher.find())
				link = matcher.group();
			else
				link = matcher2.group();
			Set<String> linkWhitelist = new HashSet<>(OlympaCore.getInstance().getConfig().getStringList("chat.linkwhitelist"));
			if (!linkWhitelist.stream().anyMatch(l -> link.contains(l))) {
				event.setCancelled(true);
				player.sendMessage(ColorUtils.color(Prefix.BAD + "Les liens sont interdits."));
				Chat.sendToStaff("Lien", player, message, link);
				return;
			}
		}

		// Si le message contient des ips, cancel message
		matcher = matchIpv4.matcher(msgNFD);
		if (matcher.find()) {
			String ip = matcher.group();
			event.setCancelled(true);
			player.sendMessage(ColorUtils.color(Prefix.BAD + "Les adresses IPs sont interdites."));
			Chat.sendToStaff("IP", player, message, ip);
			return;
		}

		/**
		 * matcher = this.matchIpv6.matcher(msgNFD); if (matcher.find()) {
		 * event.setCancelled(true); player.sendMessage(ColorUtils.color(Prefix.BAD +
		 * "Les adresses IPv6 sont interdites.")); Chat.sendToStaff("IP", player,
		 * message); return; }
		 **/

		// Si le message contient des insultes, cancel message
		for (Pattern regex : OlympaCore.getInstance().getSwearHandler().getRegexSwear()) {
			matcher = regex.matcher(msgNFD);
			if (matcher.find() && Bukkit.getPlayer(matcher.group()) == null) {
				event.setCancelled(true);
				player.sendMessage(ColorUtils.color(Prefix.BAD + "Merci de rester correct."));
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
				//				message = message.toLowerCase().replaceFirst(".", String.valueOf(message.toLowerCase().charAt(0)).toUpperCase());
				message = String.valueOf(message.toLowerCase().charAt(0)).toUpperCase() + message.substring(1);
				event.setMessage(message);
				player.sendMessage(ColorUtils.color(Prefix.BAD + "Merci d'éviter de mettre trop de majuscules."));
			}
		}

		// Si le message contient trop du flood, les enlever [EN TEST]
		// TODO check players names
		try {
			matcher = matchFlood.matcher(message);
			int i = 0;
			boolean find = false;
			if (matcher.find())
				for (String wordFlooded : message.split(" ")) {
					if (Bukkit.getPlayer(wordFlooded) != null)
						continue;
					matcher = matchFlood.matcher(wordFlooded);
					i = 0;
					while (matcher.find()) {
						String charsFlooded = matcher.group();
						String charFlooded = matcher.group(1);
						String wordWithoutFlood = wordFlooded.replace(charsFlooded, charFlooded);
						message = message.replace(wordFlooded, wordWithoutFlood);
						find = true;
						if (++i > 50) {
							OlympaCore.getInstance().sendMessage("&4ERROR &cBoucle infini dans la gestion de chat pour le flood.");
							break;
						}
					}
					if (find) {
						event.setMessage(message);
						player.sendMessage(ColorUtils.color(Prefix.BAD + "Merci d'éviter le flood."));
					}
				}
			i = 0;
			find = false;
			// Si le message contient des charatères non autorisé
			matcher = matchNoWord.matcher(message);
			while (matcher.find()) {
				String chars = matcher.group();
				message = message.replace(chars, "");
				if (++i > 200) {
					OlympaCore.getInstance().sendMessage("&4ERROR &cBoucle infini dans la gestion de chat pour les symboles interdits.");
					break;
				}
				find = true;
			}
			if (find) {
				event.setMessage(message);
				player.sendMessage(ColorUtils.color(Prefix.BAD + "Les symboles chelou sont interdit."));
			}
		} catch (Exception | NoClassDefFoundError e) {
			e.printStackTrace();
		}
		olympaTchat.setLastMsg(msgNFD);
		if (event.getMessage().isBlank())
			event.setCancelled(true);
	}
}
