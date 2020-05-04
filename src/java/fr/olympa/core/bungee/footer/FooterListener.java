package fr.olympa.core.bungee.footer;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class FooterListener implements Listener {

	@EventHandler
	public void onPostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();

		TextComponent top = new TextComponent();
		top.setColor(ChatColor.YELLOW);
		top.setText("Olympa");
		TextComponent footer = new TextComponent();
		footer.setColor(ChatColor.GOLD);
		footer.setText("Versions 1.9 à 1.15\nDiscord www.discord.olympa.fr | Twitter @Olympa_fr | Site www.olympa.fr");
		player.setTabHeader(top, footer);
	}
}
