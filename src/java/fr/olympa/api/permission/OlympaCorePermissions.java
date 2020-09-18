package fr.olympa.api.permission;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.server.ServerType;
import fr.olympa.core.bungee.api.permission.OlympaBungeePermission;

public class OlympaCorePermissions {

	public static final OlympaBungeePermission DISCORD_COMMAND_MANAGE = new OlympaBungeePermission(OlympaGroup.RESP_TECH, true);
	public static final OlympaPermission PERMISSION_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV }, true);
	public static final OlympaBungeePermission MAINTENANCE_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission SETSTATUS_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission GROUP_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.RESP_STAFF });
	public static final OlympaBungeePermission BUNGEE_REDIS_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);

	public static final OlympaPermission CHAT_COMMAND = new OlympaPermission(OlympaGroup.ASSISTANT, ServerType.SPIGOT);
	public static final OlympaPermission CHAT_SEEINSULTS = new OlympaPermission(OlympaGroup.ASSISTANT, ServerType.SPIGOT);
	public static final OlympaPermission CHAT_BYPASS = new OlympaPermission(OlympaGroup.RESP_TECH, ServerType.SPIGOT);
	public static final OlympaPermission CHAT_MUTEDBYPASS = new OlympaPermission(OlympaGroup.GRAPHISTE, ServerType.SPIGOT);

	public static final OlympaBungeePermission BAN_BYPASS_MAXTIME = new OlympaBungeePermission(OlympaGroup.FONDA);
	public static final OlympaBungeePermission BAN_BYPASS_SANCTION_STAFF = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_FORCEKICK_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission BAN_DEF = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_BANIP_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_BANIPDEF_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_DELBAN_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_BANDEF_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaBungeePermission BAN_BAN_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaBungeePermission BAN_UNBAN_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaBungeePermission BAN_KICK_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaBungeePermission BAN_MUTE_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission BAN_UNMUTE_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission BAN_HISTORY_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission BAN_SEEBANMSG = new OlympaBungeePermission(OlympaGroup.BUILDER);
	public static final OlympaBungeePermission BAN_BYPASS_BAN = new OlympaBungeePermission(OlympaGroup.BUILDER);

	public static final OlympaPermission UTILS_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission GETUUID_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });

	public static final OlympaPermission SERVER_SWITCH_COMMAND = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);

	public static final OlympaPermission SPAWN_SPAWN_COMMAND_SET = new OlympaPermission(OlympaGroup.RESP_TECH);

	public static final OlympaPermission CHAT_COLOR = new OlympaPermission(OlympaGroup.RESP_TECH);

	public static final OlympaPermission REPORT_SEEREPORT = new OlympaPermission(OlympaGroup.MOD);
	public static final OlympaBungeePermission REPORT_SEEREPORT_OTHERSERV = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaPermission REPORT_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);
	public static final OlympaPermission REPORT_SEE_COMMAND = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission REPORT_CHANGE_COMMAND = new OlympaPermission(OlympaGroup.MOD);

	public static final OlympaPermission STAFF = new OlympaPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaPermission PRIVATEMESSAGE_TOGGLE = new OlympaBungeePermission(OlympaGroup.MINI_YOUTUBER);
	public static final OlympaPermission VPN_BYPASS = new OlympaBungeePermission(OlympaGroup.FONDA);
	public static final OlympaPermission INFO_COMMAND = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);
	public static final OlympaPermission INFO_COMMAND_EXTRA = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission STAFF_CHAT = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);

	public static final OlympaPermission SERVER_LIST_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaPermission SERVER_START_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaPermission SERVER_RESTART_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	// TODO change to OlympaGroup.GRAPHISTE, change to OlympaGroup.ASSISTANT later
	//public static OlympaPermission SERVER_BYPASS_MAITENANCE_SPIGOT = new OlympaPermission(OlympaGroup.PLAYER);

	public static final OlympaPermission LOBBY_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaPermission GENDER_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaPermission PING_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);
	public static final OlympaPermission BPING_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);

	public static final OlympaPermission BUNGEE_LAG_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaPermission SPIGOT_LAG_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);

	// Don't fogot to change it in ZTA
	public static final OlympaPermission GAMEMODE_COMMAND = new OlympaPermission(OlympaGroup.BUILDER);
	public static final OlympaPermission GAMEMODE_COMMAND_CREATIVE = new OlympaPermission(OlympaGroup.BUILDER);
	public static final OlympaPermission FLY_COMMAND = new OlympaPermission(OlympaGroup.BUILDER);

	public static final OlympaPermission BUNGEE_CONFIG_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaPermission SPIGOT_CONFIG_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });

	public static final OlympaPermission BUNGEE_QUEUE_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaPermission TEAMSPEAK_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaPermission TEAMSPEAK_COMMAND_MANAGE = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission TEAMSPEAK_SEE_MODHELP = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission TEAMSPEAK_SEE_ADMINHELP = new OlympaBungeePermission(OlympaGroup.MODP);
}
