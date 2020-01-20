package fr.olympa.core.bungee.auth;

public class Auth2 {

	/*@EventHandler(priority = EventPriority.LOWEST)
	public void onServerConnect(ServerConnectEvent event) {

		ProxiedPlayer player = event.getPlayer();
		String name = player.getName();
		String ip = this.cache.asMap().get(name);
		if (ip == null) {
			return;
		}
		ServerInfo lobby = OlympaBungee.getInstance().getProxy().getServers().get("lobby1");
		if (lobby == null) {
			event.setTarget(lobby);
		}
		this.cache.invalidate(name);
	}*/
	/*@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(LoginEvent event) {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		String ip = connection.getAddress().getAddress().getHostAddress();
	
		UUID uuid_premium = event.getConnection().getUniqueId();
		AccountProvider olympaAccount = new AccountProvider(uuid_premium);
	
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = olympaAccount.get();
		} catch (SQLException e) {
	
			event.setCancelled(true);
			event.setCancelReason(BungeeUtils.connectScreen("&cUne erreur est survenue. \n\n&e&lMerci de la signaler au staff.\n&eCode d'erreur: &l#SQLBungeeLogin"));
			e.printStackTrace();
			return;
		}
		this.cache.invalidate(ip);
	}*/
}
