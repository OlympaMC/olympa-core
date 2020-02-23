package fr.olympa.core.bungee.login.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.login.HandlerHideLogin;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.servers.ServersConnection;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginRegisterListener implements Listener {

	public static List<ProxiedPlayer> logged = new ArrayList<>(); // temp

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChat(ChatEvent event) {
		if (!(event.getSender() instanceof ProxiedPlayer)) {
			return;
		}
		String[] args = event.getMessage().split(" ");
		String command = args[0].substring(1);
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		if (!logged.contains(player) && ServersConnection.isAuth(player) && (!event.getMessage().startsWith("/") || !HandlerHideLogin.command.contains(command))) {
			OlympaPlayer olympaPlayer = new AccountProvider(player.getUniqueId()).getFromRedis();
			if (olympaPlayer == null || olympaPlayer.getPassword() == null || olympaPlayer.getPassword().isEmpty()) {
				player.sendMessage(Prefix.DEFAULT_BAD + SpigotUtils.color("Tu dois t'enregistrer. Fait &4/register <mdp>&c."));
			} else {
				player.sendMessage(Prefix.DEFAULT_BAD + SpigotUtils.color("Tu dois être connecté. Fait &4/login <mdp>&c."));
			}
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onOlympaPlayerLogin(OlympaPlayerLoginEvent event) {
		ProxyServer.getInstance().getScheduler().schedule(OlympaBungee.getInstance(), () -> {
			OlympaPlayer olympaPlayer = event.getOlympaPlayer();
			new AccountProvider(olympaPlayer.getUniqueId()).removeFromCache();
		}, 1, TimeUnit.SECONDS);
		ProxiedPlayer player = event.getPlayer();
		if (ServersConnection.isAuth(player)) {
			ServersConnection.tryConnectToLobby(event.getPlayer());
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		player.sendMessage(BungeeUtils.color("&e--------- &6Olympa -----------"));
		player.sendMessage(BungeeUtils.color(""));
		player.sendMessage(BungeeUtils.color("&aBienvenue " + player.getName() + " sur &2Olympa&a."));
		player.sendMessage(BungeeUtils.color(""));
		if (!olympaPlayer.isPremium()) {
			if (olympaPlayer.getPassword() != null) {
				player.sendMessage(BungeeUtils.color("&aUtilise &2/login <mot de passe>"));
			} else {
				player.sendMessage(BungeeUtils.color("&aUtilise &2/register <mot de passe>&a pour te crée un compte."));
			}
		}
		player.sendMessage(BungeeUtils.color(""));
		player.sendMessage(BungeeUtils.color("&7Ajoute ton email: &8/email"));
		player.sendMessage(BungeeUtils.color("&7Change ton mot de passe: &8/passwd"));

	}

}
