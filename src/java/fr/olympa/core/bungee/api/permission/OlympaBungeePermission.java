package fr.olympa.core.bungee.api.permission;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class OlympaBungeePermission extends OlympaPermission {

	public OlympaBungeePermission(OlympaGroup min_group) {
		super(min_group);
	}

	public OlympaBungeePermission(OlympaGroup[] groups_allowGroup) {
		super(groups_allowGroup);
	}

	public void getPlayersBungee(Consumer<? super Set<ProxiedPlayer>> success) {
		Set<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers().stream().filter(player -> this.hasPermission(AccountProvider.get(player.getUniqueId()))).collect(Collectors.toSet());
		if (!players.isEmpty()) {
			success.accept(players);
		}
	}

	@Override
	public void sendMessage(BaseComponent baseComponent) {
		this.getPlayersBungee(players -> players.forEach(player -> player.sendMessage(baseComponent)));
	}
}
