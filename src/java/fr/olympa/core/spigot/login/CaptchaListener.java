package fr.olympa.core.spigot.login;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
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
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;
		Player player = (Player) event.getWhoClicked();
		if (PlayerLogin.isIn(player))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (PlayerLogin.isIn(player))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (PlayerLogin.isIn(player))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (PlayerLogin.isIn(player))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityPickupItem(EntityPickupItemEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		Player player = (Player) event.getEntity();
		if (PlayerLogin.isIn(player))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (PlayerLogin.isIn(player) && !event.getCause().equals(TeleportCause.PLUGIN)) {
			Prefix.DEFAULT_BAD.sendMessage(player, "Réponds au captcha dans le chat pour continuer.");
			event.setTo(event.getFrom());
		}
	}
	//
	//	@EventHandler(priority = EventPriority.LOWEST)
	//	public void onPlayerMove(PlayerMoveEvent event) {
	//		Player player = event.getPlayer();
	//		if (PlayerLogin.isIn(player)) {
	//			Prefix.DEFAULT_BAD.sendMessage(player, "Réponds au captcha dans le chat pour continuer.");
	//			event.setTo(event.getFrom());
	//		}
	//	}
}
