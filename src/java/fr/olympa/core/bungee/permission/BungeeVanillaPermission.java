package fr.olympa.core.bungee.permission;

import java.util.Arrays;
import java.util.Optional;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsBungee;

public enum BungeeVanillaPermission {

	BYPASS_PERM_NOT_EXIST(OlympaAPIPermissionsBungee.BYPASS_PERM_NOT_EXIST),
	CLIENTSTATS_CMD_STATS(OlympaAPIPermissionsBungee.COMMAND_STATS_INFO),
	CLIENTSTATS_CMD_VERSION(OlympaAPIPermissionsBungee.COMMAND_STATS_INFO),
	CLIENTSTATS_CMD_ONLINE(OlympaAPIPermissionsBungee.COMMAND_STATS_INFO),
	CLIENTSTATS_CMD_PLAYER(OlympaAPIPermissionsBungee.COMMAND_STATS_INFO),
	CLIENTSTATS_CMD_RESET(OlympaAPIPermissionsBungee.COMMAND_STATS_SETTINGS),
	CLIENTSTATS_CMD_RELOAD(OlympaAPIPermissionsBungee.COMMAND_STATS_SETTINGS),
	BUNGEECORD_COMMAND_ALERT(OlympaAPIPermissionsBungee.COMMAND_BUNGEE_ALERT),
	BUNGEECORD_COMMAND_END(OlympaAPIPermissionsBungee.COMMAND_BUNGEE_END),
	BUNGEECORD_COMMAND_FIND(OlympaAPIPermissionsBungee.COMMAND_BUNGEE_FIND),
	BUNGEECORD_COMMAND_LIST(OlympaAPIPermissionsBungee.COMMAND_BUNGEE_LIST),
	BUNGEECORD_COMMAND_RELOAD(OlympaAPIPermissionsBungee.COMMAND_BUNGEE_RELOAD),
	BUNGEECORD_COMMAND_IP(OlympaAPIPermissionsBungee.COMMAND_BUNGEE_IP),
	BUNGEECORD_COMMAND_SEND(OlympaAPIPermissionsBungee.COMMAND_BUNGEE_SEND),
	BUNGEECORD_COMMAND_SERVER(OlympaAPIPermissionsBungee.COMMAND_BUNGEE_SERVER),
	REDISBUNGEE_COMMAND_PLIST(OlympaAPIPermissionsBungee.COMMAND_BUNGEE_LIST),
	REDISBUNGEE_COMMAND_PPROXY(OlympaAPIPermissionsBungee.COMMAND_REDISBUNGEE_INFODIV),
	REDISBUNGEE_COMMAND_SERVERIDS(OlympaAPIPermissionsBungee.COMMAND_REDISBUNGEE_INFODIV),
	REDISBUNGEE_COMMAND_SERVERID(OlympaAPIPermissionsBungee.COMMAND_REDISBUNGEE_INFODIV),
	REDISBUNGEE_COMMAND_SENDTOALL(OlympaAPIPermissionsBungee.COMMAND_REDISBUNGEE_SENDTOALL),
	REDISBUNGEE_COMMAND_IP(OlympaAPIPermissionsBungee.COMMAND_BUNGEE_IP),
	REDISBUNGEE_COMMAND_LASTSEEN(OlympaAPIPermissionsBungee.COMMAND_REDISBUNGEE_INFODIV);

	public static BungeeVanillaPermission get(String bungeeVanillaName) {
		return Arrays.stream(BungeeVanillaPermission.values()).filter(p -> p.getBungeeVanillaName().equals(bungeeVanillaName)).findFirst().or(() -> {
			LinkSpigotBungee.Provider.link.sendMessage("&4%s&c is not register in %s. You need to add it to handle this permission with OlympaPermission system.",
					bungeeVanillaName, BungeeVanillaPermission.class.getName());
			return Optional.of(BYPASS_PERM_NOT_EXIST);
		}).get();

	}

	OlympaBungeePermission permission;

	BungeeVanillaPermission(OlympaBungeePermission permission) {
		this.permission = permission;
	}

	public String getBungeeVanillaName() {
		return name().replace("_", ".").toLowerCase();
	}

	public OlympaBungeePermission getPermission() {
		return permission;
	}

}
