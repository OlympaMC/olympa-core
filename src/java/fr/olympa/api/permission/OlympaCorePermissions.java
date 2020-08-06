package fr.olympa.api.permission;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.core.bungee.api.permission.OlympaBungeePermission;

public class OlympaCorePermissions {

	public static final OlympaPermission DISCORD_BOT = new OlympaPermission(OlympaGroup.RESP_TECH, true);
	public static final OlympaPermission PERMISSION_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV }, true);
	public static final OlympaPermission MAINTENANCE_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission GROUP_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.RESP_STAFF });
	public static final OlympaPermission BUNGEE_REDIS_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);

	public static final OlympaPermission CHAT_COMMAND = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission CHAT_SEEINSULTS = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission CHAT_BYPASS = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission CHAT_MUTEDBYPASS = new OlympaPermission(OlympaGroup.GRAPHISTE);

	public static final OlympaPermission BAN_BYPASS_MAXTIME = new OlympaBungeePermission(OlympaGroup.FONDA);
	public static final OlympaPermission BAN_BYPASS_SANCTION_STAFF = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission BAN_FORCEKICK_COMMAND = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission BAN_DEF = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission BAN_BANIP_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission BAN_BANIPDEF_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission BAN_DELBAN_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission BAN_BANDEF_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaPermission BAN_BAN_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaPermission BAN_UNBAN_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaPermission BAN_KICK_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaPermission BAN_MUTE_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission BAN_UNMUTE_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission BAN_HISTORY_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission BAN_SEEBANMSG = new OlympaBungeePermission(OlympaGroup.BUILDER);
	public static final OlympaPermission BAN_BYPASS_BAN = new OlympaBungeePermission(OlympaGroup.BUILDER);

	public static final OlympaPermission UTILS_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission GETUUID_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });

	public static final OlympaPermission SERVER_SWITCH_COMMAND = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);

	public static final OlympaPermission SPAWN_SPAWN_COMMAND_SET = new OlympaPermission(OlympaGroup.RESP_TECH);

	public static final OlympaPermission CHAT_COLOR = new OlympaPermission(OlympaGroup.RESP_TECH);

	public static final OlympaPermission REPORT_SEEREPORT = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static final OlympaPermission REPORT_SEE_COMMAND = new OlympaPermission(OlympaGroup.ASSISTANT);

	public static final OlympaPermission STAFF = new OlympaPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaPermission PRIVATEMESSAGE_TOGGLE = new OlympaPermission(OlympaGroup.MINI_YOUTUBER);
	public static final OlympaPermission SETSTATUS_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission VPN_BYPASS = new OlympaPermission(OlympaGroup.FONDA);
	public static final OlympaPermission INFO_COMMAND = new OlympaPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaPermission INFO_COMMAND_EXTRA = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission STAFF_CHAT = new OlympaPermission(OlympaGroup.GRAPHISTE);

	public static final OlympaPermission SERVER_LIST_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaPermission SERVER_START_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaPermission SERVER_RESTART_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	// TODO change to OlympaGroup.GRAPHISTE, change to OlympaGroup.ASSISTANT later
	//public static OlympaPermission SERVER_BYPASS_MAITENANCE_SPIGOT = new OlympaPermission(OlympaGroup.PLAYER);

	// USLESS PERMISSION
	public static final OlympaPermission LOBBY_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);
	public static final OlympaPermission GENDER_COMMAND = new OlympaPermission(OlympaGroup.PLAYER);

	public static final OlympaPermission BUNGEE_LAG_COMMAND_EXTRA = new OlympaPermission(OlympaGroup.FRIEND);
	public static final OlympaPermission SPIGOT_LAG_COMMAND = new OlympaPermission(OlympaGroup.FRIEND);

	public static final OlympaPermission GAMEMODE_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static final OlympaPermission GAMEMODE_COMMAND_CREATIVE = new OlympaPermission(OlympaGroup.MODP);
	public static final OlympaPermission FLY_COMMAND = new OlympaPermission(OlympaGroup.MODP);
	public static final OlympaPermission CONFIG_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });

}
