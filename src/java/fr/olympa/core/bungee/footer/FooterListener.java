package fr.olympa.core.bungee.footer;

import fr.olympa.api.utils.ColorUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class FooterListener implements Listener {

	@EventHandler
	public void onPostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		TextComponent top = new TextComponent(TextComponent.fromLegacyText(ColorUtils.color("&eOlympa &lΩ")));
		TextComponent footer = new TextComponent(TextComponent.fromLegacyText(ColorUtils.color("&bVersions 1.9 à 1.15\n&3Discord &n&ldiscord.olympa.fr&7 &l&m|&3 Twitter &n&l@Olympa_fr&7 &l&m|&3 Site &n&lwww.olympa.fr")));
		player.setTabHeader(top, footer);
	}
}
