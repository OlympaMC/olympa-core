package fr.olympa.core.bungee.permission;

import java.util.Objects;

import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class PermissionCheckListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerPermissionCheck(PermissionCheckEvent event) {
		if (!(event.getSender() instanceof ProxiedPlayer))
			return;
		Objects.requireNonNull(event.getPermission(), "permission");
		Objects.requireNonNull(event.getSender(), "sender");
		String permission = event.getPermission();
		BungeeVanillaPermission bungeeVanillaPermission = BungeeVanillaPermission.get(permission);
		OlympaBungeePermission olympaBungeePermission = bungeeVanillaPermission.getPermission();
		event.setHasPermission(olympaBungeePermission.hasSenderPermissionBungee(event.getSender()));
	}

}
