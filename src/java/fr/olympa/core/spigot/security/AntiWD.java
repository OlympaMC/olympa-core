package fr.olympa.core.spigot.security;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import fr.olympa.api.utils.SpigotUtils;

public class AntiWD implements Listener, PluginMessageListener {

	public AntiWD(Plugin plugin) {
		Server server = plugin.getServer();
		Messenger messenger = server.getMessenger();
		server.getPluginManager().registerEvents((Listener) this, (Plugin) this);
		messenger.registerIncomingPluginChannel((Plugin) this, "WDL|INIT", (PluginMessageListener) this);
		messenger.registerOutgoingPluginChannel((Plugin) this, "WDL|CONTROL");
	}
	
	@Override
	public void onPluginMessageReceived(final String channel, final Player player, final byte[] data) {
		if (channel.equals("WDL|INIT")) {
			player.kickPlayer(SpigotUtils.connectScreen("Les mods de téléchargement de maps sont interdits"));
		}
	}
}
