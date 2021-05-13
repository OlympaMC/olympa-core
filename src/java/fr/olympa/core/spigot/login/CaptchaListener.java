package fr.olympa.core.spigot.login;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import fr.olympa.api.utils.Prefix;

public class CaptchaListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String msg = event.getMessage();
		if (PlayerLogin.isIn(player)) {
			event.setCancelled(true);
			PlayerLogin.captchaGood(player, msg);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if (PlayerLogin.isIn(player)) {
			Prefix.DEFAULT_BAD.sendMessage(player, "Réponds au captcha dans le chat pour continuer.");
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerMove(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (PlayerLogin.isIn(player) && !event.getCause().equals(TeleportCause.PLUGIN)) {
			Prefix.DEFAULT_BAD.sendMessage(player, "Réponds au captcha dans le chat pour continuer.");
			event.setTo(event.getFrom());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location playerLoc = player.getLocation();
		Location toLoc = event.getTo();
		if (PlayerLogin.isIn(player) && (playerLoc.getX() != toLoc.getX() || playerLoc.getZ() != toLoc.getZ())) {
			Prefix.DEFAULT_BAD.sendMessage(player, "Réponds au captcha dans le chat pour continuer.");
			event.setTo(event.getFrom());
		}
	}
}
