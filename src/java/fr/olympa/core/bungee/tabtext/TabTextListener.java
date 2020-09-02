package fr.olympa.core.bungee.tabtext;

import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TabTextListener implements Listener {

	//	private static long time = Utils.getCurrentTimeInSeconds();

	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		//		long t = Utils.getCurrentTimeInSeconds();
		//		if (t - time < 10)
		//			return;
		//		time = t;
		OlympaBungee.getInstance().getTask().runTask(() -> TabText.sendAll());
	}

	@EventHandler
	public void onPostLogin(PostLoginEvent event) {
		//		long t = Utils.getCurrentTimeInSeconds();
		//		if (t - time < 10) {
		//		event.getPlayer().setTabHeader(header, footer);
		//		return;
		//		}
		//		time = t;
		TabText.sendAll();
		TabText.send(event.getPlayer());
	}

}
