package fr.olympa.core.bungee.api.permission;

import java.sql.SQLException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.server.ServerType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class OlympaBungeePermission extends OlympaPermission {

	public OlympaBungeePermission(OlympaGroup minGroup) {
		super(minGroup);
		serverType = ServerType.BUNGEE;
	}

	public OlympaBungeePermission(OlympaGroup... allowedGroups) {
		super(allowedGroups);
		serverType = ServerType.BUNGEE;
	}

	public OlympaBungeePermission(OlympaGroup minGroup, ServerType serverType) {
		super(minGroup, serverType);
		serverType = ServerType.BUNGEE;
	}

	public OlympaBungeePermission(OlympaGroup minGroup, boolean lockPermission) {
		super(minGroup, lockPermission);
		serverType = ServerType.BUNGEE;
	}

	public OlympaBungeePermission(OlympaGroup minGroup, boolean lockPermission, ServerType serverType) {
		super(minGroup, lockPermission, serverType);
		serverType = ServerType.BUNGEE;
	}

	public OlympaBungeePermission(boolean lockPermission, OlympaGroup... allowedGroups) {
		super(lockPermission, allowedGroups);
		serverType = ServerType.BUNGEE;
	}

	public OlympaBungeePermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups) {
		super(minGroup, allowedGroups);
		serverType = ServerType.BUNGEE;
	}

	public OlympaBungeePermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups, ServerType serverType) {
		super(minGroup, allowedGroups, serverType);
		serverType = ServerType.BUNGEE;
	}

	public OlympaBungeePermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups, boolean lockPermission) {
		super(minGroup, allowedGroups, lockPermission);
		serverType = ServerType.BUNGEE;
	}

	public OlympaBungeePermission(OlympaGroup minGroup, OlympaGroup[] allowedGroups, boolean lockPermission, ServerType serverType) {
		super(minGroup, allowedGroups, lockPermission, serverType);
		serverType = ServerType.BUNGEE;
	}

	public void getPlayersBungee(Consumer<? super Set<ProxiedPlayer>> success) {
		Set<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers().stream().filter(p -> {
			try {
				return this.hasPermission(new AccountProvider(p.getUniqueId()).get());
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}).collect(Collectors.toSet());
		if (!players.isEmpty())
			success.accept(players);
	}

	@Override
	public void sendMessage(BaseComponent baseComponent) {
		getPlayersBungee(players -> players.forEach(player -> player.sendMessage(baseComponent)));
	}
}
