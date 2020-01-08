package fr.olympa.core.bungee.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerHandshakeEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class AuthListener implements Listener {

	Cache<String, String> cache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

	public UUID getUuid(PendingConnection con) {
		try {
			URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + con.getName());
			URLConnection connection = url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(con.getAddress().getAddress().getHostAddress(), con.getAddress().getPort())));
			connection.setUseCaches(false);
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getURL().openStream(), Charset.forName("UTF-8")));
			StringBuilder stringBuilder = new StringBuilder();
			String lineNotFount;
			while ((lineNotFount = in.readLine()) != null) {
				stringBuilder.append(lineNotFount + "\n");
			}
			in.close();
			if (stringBuilder.toString().isEmpty()) {
				return null;
			}
			return UUID.fromString(
					stringBuilder.toString().replace("{", "").replace("}", "").replace("\"", "").split(",")[0].split(":")[1].replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@EventHandler
	public void onLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		AccountProvider olympaAccount = new AccountProvider(player.getUniqueId());
		OlympaPlayer olympaPlayer = null;
		try {
			olympaPlayer = olympaAccount.get();
			if (olympaPlayer == null) {
				olympaPlayer = olympaAccount.createOlympaPlayer(player.getName(), player.getAddress().getAddress().getHostAddress());
				if (!olympaAccount.createNew(olympaPlayer)) {
					player.disconnect(BungeeUtils.connectScreen("§cUne erreur de données est survenu, merci de réessayer."));
					return;
				}
				OlympaBungee.getInstance().sendMessage("Nouveau joueur: " + olympaPlayer.getName());
			}
		} catch (SQLException e) {
			player.disconnect(BungeeUtils.connectScreen("§cImpossible de récupérer vos donnés."));
			e.printStackTrace();
		}

		olympaAccount.saveToRedis(olympaPlayer);
	}

	@EventHandler
	public void onPing(ProxyPingEvent event) {
		PendingConnection connection = event.getConnection();
		this.cache.put(connection.getName(), connection.getAddress().getAddress().getHostAddress());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerHandshake(PlayerHandshakeEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		if (!event.getConnection().isConnected()) {
			return;
		}

		if (!name.matches("[a-zA-Z0-9_]*")) {
			connection.disconnect(BungeeUtils.connectScreen("§6Ton pseudo doit contenir uniquement des chiffres, des lettres et des tiret bas."));
			return;
		}

		String ip = this.cache.asMap().get(name);
		if (ip == null || !ip.equals(connection.getAddress().getAddress().getHostAddress())) {
			connection.disconnect(BungeeUtils.connectScreen("§7[§cSécuriter§7] §6Tu dois ajouter le serveur avant de pouvoir te connecter. La connexion direct n'est pas autoriser."));
			return;
		}

		UUID uuid_premium = event.getConnection().getUniqueId();
		AccountProvider olympaAccount = new AccountProvider(uuid_premium);

		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = olympaAccount.get();
		} catch (SQLException e) {
			connection.disconnect(BungeeUtils.connectScreen("§cUne erreur est survenue."));
			e.printStackTrace();
			return;
		}
		if (olympaPlayer == null) {
			UUID uuid = this.getUuid(connection);
			if (uuid == null) {
				this.cache.invalidate(name);
				connection.disconnect(BungeeUtils.connectScreen("§7[§cSécuriter§7] §eLes cracks ne sont pas encore autoriser."));
				return;
				// connection.setOnlineMode(false);
			} else {
				this.cache.put(name, ip);
				connection.setOnlineMode(true);
			}
		}

		olympaAccount.saveToRedis(olympaPlayer);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onServerConnect(ServerConnectEvent event) {
		ProxiedPlayer player = event.getPlayer();
		String name = player.getName();
		String ip = this.cache.asMap().get(name);
		if (ip == null) {
			return;
		}
		ServerInfo lobby = OlympaBungee.getInstance().getProxy().getServers().get("lobby1");
		event.setTarget(lobby);
		this.cache.invalidate(name);
	}
}
