package fr.olympa.api.permission;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.core.bungee.api.permission.OlympaBungeePermission;

public class OlympaCorePermissions {

	public static OlympaPermission DEV = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission MAINTENANCE_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission GROUP_COMMAND = new OlympaPermission(OlympaGroup.FONDA, OlympaGroup.ADMIN, OlympaGroup.RESP_STAFF, OlympaGroup.RESP_TECH, OlympaGroup.MODP);
	public static OlympaPermission BUNGEE_REDIS_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);

	public static OlympaPermission CHAT_COMMAND = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static OlympaPermission CHAT_SEEINSULTS = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static OlympaPermission CHAT_BYPASS = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission CHAT_MUTEDBYPASS = new OlympaPermission(OlympaGroup.GRAPHISTE);

	public static OlympaPermission BAN_BYPASS_MAXTIME = new OlympaBungeePermission(OlympaGroup.FONDA);
	public static OlympaPermission BAN_BYPASS_SANCTION_STAFF = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission BAN_FORCEKICK_COMMAND = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static OlympaPermission BAN_DEF = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission BAN_BANIP_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission BAN_BANIPDEF_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission BAN_DELBAN_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission BAN_BANDEF_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_BAN_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_UNBAN_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_KICK_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_MUTE_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static OlympaPermission BAN_UNMUTE_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static OlympaPermission BAN_HISTORY_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static OlympaPermission BAN_SEEBANMSG = new OlympaBungeePermission(OlympaGroup.BUILDER);
	public static OlympaPermission BAN_BYPASS_BAN = new OlympaBungeePermission(OlympaGroup.BUILDER);

	public static OlympaPermission UTILS_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);

	public static OlympaPermission SERVER_SWITCH_COMMAND = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);

	public static OlympaPermission SPAWN_SPAWN_COMMAND_SET = new OlympaPermission(OlympaGroup.RESP_TECH);

	public static OlympaPermission CHAT_COLOR = new OlympaPermission(OlympaGroup.RESP_TECH);

	public static OlympaPermission REPORT_SEEREPORT = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static OlympaPermission REPORT_SEE_COMMAND = new OlympaPermission(OlympaGroup.ASSISTANT);

	public static OlympaPermission STAFF = new OlympaPermission(OlympaGroup.GRAPHISTE);
	public static OlympaPermission PRIVATEMESSAGE_TOGGLE = new OlympaPermission(OlympaGroup.MINI_YOUTUBER);
	public static OlympaPermission SETSTATUS_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission VPN_BYPASS = new OlympaPermission(OlympaGroup.FONDA);
	public static OlympaPermission INFO_COMMAND = new OlympaPermission(OlympaGroup.GRAPHISTE);
	public static OlympaPermission INFO_COMMAND_EXTRA = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission STAFF_CHAT = new OlympaPermission(OlympaGroup.GRAPHISTE);

	public static OlympaPermission SERVER_LIST_COMMAND = new OlympaPermission(OlympaGroup.DEV, OlympaGroup.RESP_TECH, OlympaGroup.ADMIN, OlympaGroup.FONDA);
	public static OlympaPermission SERVER_START_COMMAND = new OlympaPermission(OlympaGroup.DEV, OlympaGroup.RESP_TECH, OlympaGroup.ADMIN, OlympaGroup.FONDA);
	public static OlympaPermission SERVER_RESTART_COMMAND = new OlympaPermission(OlympaGroup.DEV, OlympaGroup.RESP_TECH, OlympaGroup.ADMIN, OlympaGroup.FONDA);
	// TODO change to OlympaGroup.GRAPHISTE, change to OlympaGroup.ASSISTANT later
	//public static OlympaPermission SERVER_BYPASS_MAITENANCE_SPIGOT = new OlympaPermission(OlympaGroup.PLAYER);

	// USLESS PERMISSION
	public static OlympaPermission LOBBY_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);
	public static OlympaPermission GENDER_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);

	public static OlympaPermission BUNGEE_LAG_COMMAND_EXTRA = new OlympaPermission(OlympaGroup.FRIEND);
	public static OlympaPermission SPIGOT_LAG_COMMAND = new OlympaPermission(OlympaGroup.FRIEND);

	public static OlympaPermission GAMEMODE_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission GAMEMODE_COMMAND_CREATIVE = new OlympaPermission(OlympaGroup.MODP);
	public static OlympaPermission FLY_COMMAND = new OlympaPermission(OlympaGroup.MODP);
	public static OlympaPermission CONFIG_COMMAND = new OlympaPermission(OlympaGroup.DEV, OlympaGroup.RESP_TECH, OlympaGroup.ADMIN, OlympaGroup.FONDA);

}
