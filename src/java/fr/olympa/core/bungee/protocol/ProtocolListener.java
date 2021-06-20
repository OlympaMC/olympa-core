package fr.olympa.core.bungee.protocol;

import fr.olympa.api.bungee.customevent.OlympaPlayerLoginEvent;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ProtocolListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PreLoginEvent event) {
		if (event.isCancelled())
			return;
		PendingConnection connection = event.getConnection();
		ProtocolAPI playerVersion = ProtocolAPI.get(connection.getVersion());
		if (playerVersion == null || !playerVersion.isAllowed()) {
			ProtocolAPI lastVersion = ProtocolAPI.getLastVersion();
			event.setCancelReason(
					BungeeUtils.connectScreen("&cLa version que tu utilise n'est pas compatible avec le serveur.\n&4Utilise une version entre &c&l%s&4 et &c&l%s&4."
							+ "\n\n&4&nLa version recommandée est &c&n%s&4.", ProtocolAPI.getFirstVersion().getName(), lastVersion.getName(), lastVersion.getName()));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onOlympaPlayerLogin(OlympaPlayerLoginEvent event) {
		if (event.isCancelled())
			return;
		ProxiedPlayer player = event.getPlayer();
		ProtocolAPI recommandedVersion = ProtocolAPI.getLastVersion();
		ProtocolAPI playerVersion = ProtocolAPI.get(player.getPendingConnection().getVersion());
		if (playerVersion == null)
			player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_BAD, "Pour une meilleur expérience, il est préférable d'utiliser la version &2&n&l%s&c", recommandedVersion));
		else if (!recommandedVersion.equals(playerVersion))
			player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_BAD, "Pour une meilleur expérience, il est préférable d'utiliser la version &2&n&l%s&c. "
					+ "Tu utilise actuellement la version &4%s&c.", (Object) recommandedVersion.getName(), (Object) playerVersion.getName()));
	}
}
