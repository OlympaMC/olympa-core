package fr.olympa.core.bungee.redis.receiver.site;

import java.sql.SQLException;
import java.util.UUID;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.customevent.OlympaGroupChangeEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.JedisPubSub;

public class SiteGroupChangeReceiver extends JedisPubSub {
	@Override
	public void onMessage(String channel, String message) {
		String[] args = message.split(";");
		UUID uuid = (UUID) RegexMatcher.UUID.parse(args[2]);
		String[] infoGroup = args[1].split(":");
		OlympaGroup groupChanged = OlympaGroup.getById(Integer.parseInt(infoGroup[0]));
		long timestamp = Integer.parseInt(infoGroup[1]);
		ChangeType state = ChangeType.get(Integer.parseInt(args[2]));
		switch (state) {

		case ADD:
			break;
		case REMOVE:
			break;
		case SET:
			break;
		default:
			break;
		}
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = new AccountProvider(uuid).get();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		OlympaBungee.getInstance().sendMessage("&a[DEBUG] PLAYER CHANGE GROUPE from Redis for " + olympaPlayer.getName() + " from site");
		ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uuid);
		ProxyServer.getInstance().getPluginManager().callEvent(new OlympaGroupChangeEvent(player, olympaPlayer, groupChanged, timestamp, state));
	}
}
