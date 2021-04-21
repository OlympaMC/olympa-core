package fr.olympa.api.permission;

import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.groups.OlympaGroup;

public class OlympaCorePermissions {

	public static final OlympaBungeePermission DISCORD_COMMAND_MANAGE = new OlympaBungeePermission(OlympaGroup.RESP_TECH, true);
	public static final OlympaSpigotPermission PERMISSION_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV }, true);
	public static final OlympaBungeePermission MAINTENANCE_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission SETSTATUS_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission GROUP_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.RESP_STAFF });
	public static final OlympaBungeePermission BUNGEE_REDIS_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);

	public static final OlympaSpigotPermission CHAT_COMMAND = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission CHAT_SEEINSULTS = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission CHAT_BYPASS = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission CHAT_MUTEDBYPASS = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);

	public static final OlympaBungeePermission BAN_BYPASS_MAXTIME = new OlympaBungeePermission(OlympaGroup.FONDA);
	public static final OlympaBungeePermission BAN_BYPASS_SANCTION_STAFF = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_FORCEKICK_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission BAN_DEF = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_BANIP_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_BANIP_SOFT = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaBungeePermission BAN_BANIPDEF_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_DELBAN_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_BANDEF_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaBungeePermission BAN_BAN_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaBungeePermission BAN_UNBAN_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaBungeePermission BAN_KICK_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaBungeePermission BAN_MUTE_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission BAN_UNMUTE_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission BAN_HISTORY_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission BAN_SEEBANMSG = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);
	public static final OlympaBungeePermission BAN_SEEBANMSG_FULL = new OlympaBungeePermission(OlympaGroup.RESP_BUILDER);
	public static final OlympaBungeePermission BAN_BYPASS_BAN = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);

	public static final OlympaSpigotPermission UTILS_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission GETUUID_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });

	public static final OlympaBungeePermission SERVER_SWITCH_COMMAND = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);

	public static final OlympaSpigotPermission SPAWN_SPAWN_COMMAND_SET = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);

	public static final OlympaSpigotPermission CHAT_COLOR = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);

	public static final OlympaSpigotPermission REPORT_SEEREPORT = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission REPORT_SEEREPORT_OTHERSERV = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission REPORT_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission REPORT_SEE_COMMAND = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission REPORT_CHANGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.MOD);

	public static final OlympaSpigotPermission STAFF = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaBungeePermission PRIVATEMESSAGE_TOGGLE = new OlympaBungeePermission(OlympaGroup.MINI_YOUTUBER);
	public static final OlympaBungeePermission VPN_BYPASS = new OlympaBungeePermission(OlympaGroup.FONDA);
	public static final OlympaBungeePermission INFO_COMMAND = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);
	public static final OlympaBungeePermission INFO_COMMAND_EXTRA = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission INFO_COMMAND_EXTRA_EXTRA = new OlympaBungeePermission(OlympaGroup.FONDA, OlympaGroup.ADMIN, OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission STAFF_CHAT = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);

	public static final OlympaBungeePermission SERVER_LIST_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission SERVER_START_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission SERVER_RESTART_BUNGEE_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaSpigotPermission SERVER_RESTART_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	// TODO change to OlympaGroup.GRAPHISTE, change to OlympaGroup.ASSISTANT later
	//public static OlympaSpigotPermission SERVER_BYPASS_MAITENANCE_SPIGOT = new OlympaSpigotPermission(OlympaGroup.PLAYER);

	public static final OlympaSpigotPermission GENDER_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission LOBBY_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission BPING_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);

	public static final OlympaBungeePermission BUNGEE_LAG_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission SPIGOT_LAG_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);

	public static final OlympaSpigotPermission SPIGOT_CONFIG_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });

	public static final OlympaBungeePermission BUNGEE_BROADCAST_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission BUNGEE_SECURITY_COMMAND = new OlympaSpigotPermission(OlympaGroup.ADMIN, OlympaGroup.RESP_TECH);

	public static final OlympaBungeePermission BUNGEE_QUEUE_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission TEAMSPEAK_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission TEAMSPEAK_COMMAND_MANAGE = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission TEAMSPEAK_SEE_MODHELP = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission TEAMSPEAK_SEE_ADMINHELP = new OlympaBungeePermission(OlympaGroup.MODP);
	public static final OlympaSpigotPermission VERIFMOD_SEE_IN_TAB = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaSpigotPermission VANISH_SEE_IN_TAB = new OlympaSpigotPermission(OlympaGroup.MODP);

	public static final OlympaBungeePermission NICK_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission BUNGEE_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission BUNGEE_COMMAND_VPNINFO = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BUNGEE_COMMAND_TPS = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission BUNGEE_COMMAND_CONFIGS = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission BUNGEE_COMMAND_MAXPLAYERS = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission BUNGEE_COMMAND_SETTINGS = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission BUNGEE_COMMAND_ANTIBOT = new OlympaBungeePermission(OlympaGroup.RESP_BUILDER, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission BUNGEE_COMMAND_CACHE = new OlympaBungeePermission(OlympaGroup.RESP_TECH, OlympaGroup.ADMIN).lockPermission();
	public static final OlympaSpigotPermission SPIGOT_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission SPIGOT_COMMAND_ANTIBOT = new OlympaSpigotPermission(OlympaGroup.RESP_BUILDER, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaSpigotPermission SPIGOT_COMMAND_CACHE = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, OlympaGroup.ADMIN).lockPermission();

	public static final OlympaBungeePermission IP_COMMAND = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);
	public static final OlympaBungeePermission IP_COMMAND_SEE_IP = new OlympaBungeePermission(OlympaGroup.RESP_TECH);

	public static final OlympaSpigotPermission NAMETAG_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
}
