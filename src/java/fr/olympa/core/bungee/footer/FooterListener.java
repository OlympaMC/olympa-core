package fr.olympa.core.bungee.footer;

import java.util.StringJoiner;

import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class FooterListener implements Listener {
	
	private static long time = Utils.getCurrentTimeInSeconds();
	
	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event) {
		long t = Utils.getCurrentTimeInSeconds();
		if (t - time < 10)
			return;
		time = t;
		TextComponent header = getHeader();
		TextComponent footer = getFooter();
		OlympaBungee.getInstance().getProxy().getPlayers().forEach(p -> p.setTabHeader(header, footer));
	}

	@EventHandler
	public void onPostLogin(PostLoginEvent event) {
		long t = Utils.getCurrentTimeInSeconds();
		TextComponent header = getHeader();
		TextComponent footer = getFooter();
		if (t - time < 10) {
			event.getPlayer().setTabHeader(header, footer);
			return;
		}
		time = t;
		OlympaBungee.getInstance().getProxy().getPlayers().forEach(p -> p.setTabHeader(header, footer));
	}
	
	private TextComponent getHeader() {
		return new TextComponent(TextComponent.fromLegacyText(ColorUtils.color("&eOlympa Ω")));

	}
	
	private TextComponent getFooter() {
		StringJoiner sj = new StringJoiner("\n");
		sj.add("&bVersions 1.9 à 1.15");
		int onlineCount = OlympaBungee.getInstance().getProxy().getOnlineCount();
		sj.add("&b" + onlineCount + "connecté" + Utils.withOrWithoutS(onlineCount));
		sj.add("&3Discord &n&ldiscord.olympa.fr&7 &l&m|&3 Twitter &n&l@Olympa_fr&7 &l&m|&3 Site &n&lwww.olympa.fr");
		return new TextComponent(TextComponent.fromLegacyText(ColorUtils.color(sj.toString())));
	}
}
