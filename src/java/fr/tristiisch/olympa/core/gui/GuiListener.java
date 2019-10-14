package fr.tristiisch.olympa.core.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.tristiisch.olympa.OlympaCore;
import fr.tristiisch.olympa.api.customevents.GuiClickEvent;
import fr.tristiisch.olympa.api.gui.GuiHandler;
import fr.tristiisch.olympa.api.gui.OlympaGuiBuild;

public class GuiListener implements Listener {

	@EventHandler
	public void onInventoryClick(final InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		final Player player = (Player) event.getWhoClicked();
		OlympaGuiBuild gui = GuiHandler.getGui(player);

		if (gui == null) {
			return;
		}

		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new GuiClickEvent(player, event, gui));

		if (!gui.canClick()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent event) {
		final Player player = (Player) event.getPlayer();
		// Bukkit.getPluginManager().callEvent(new GuiCloseEvent(player, event));
		GuiHandler.removeGui(player);
	}

	@EventHandler
	public void onInventoryOpen(final InventoryOpenEvent event) {
		if (event.getInventory().getType().equals(InventoryType.CRAFTING)) {
			// GuiHandler.setGui(new GuiData("container.crafting",
			// event.getPlayer().getUniqueId()));
		}
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		GuiHandler.removeGui(event.getPlayer());
	}
}
