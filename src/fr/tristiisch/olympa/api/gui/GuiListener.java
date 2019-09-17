package fr.tristiisch.olympa.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;

import fr.tristiisch.olympa.api.gui.OlympaGuiBuild.GuiData;

public class GuiListener implements Listener {

	@EventHandler
	public void onInventoryClose(final InventoryCloseEvent event) {
		final Player player = (Player) event.getPlayer();
		// Bukkit.getPluginManager().callEvent(new GuiCloseEvent(player, event));
		OlympaGuiBuild.removeGui(player);
	}

	@EventHandler
	public void onInventoryOpen(final InventoryOpenEvent event) {
		if (event.getInventory().getType().equals(InventoryType.CRAFTING)) {
			OlympaGuiBuild.setGuiData(new GuiData("container.crafting", event.getPlayer().getUniqueId()));
		}
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		OlympaGuiBuild.removeGui(event.getPlayer());
	}
}
