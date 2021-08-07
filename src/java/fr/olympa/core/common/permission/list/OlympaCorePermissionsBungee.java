package fr.olympa.core.common.permission.list;

import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.common.groups.OlympaGroup;

public class OlympaCorePermissionsBungee {

	/* BUNGEECORD */
	public static final OlympaBungeePermission DISCORD_COMMAND_MANAGE = new OlympaBungeePermission(OlympaGroup.RESP_TECH, true);
	public static final OlympaBungeePermission MAINTENANCE_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BUNGEE_REDIS_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);

	public static final OlympaBungeePermission BAN_BYPASS_MAXTIME = new OlympaBungeePermission(OlympaGroup.FONDA);
	public static final OlympaBungeePermission BAN_BYPASS_SANCTION_STAFF = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_FORCEKICK_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission BAN_DEF = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_BANIP_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BAN_BANIP_SOFT = new OlympaBungeePermission(OlympaGroup.MOD);
	public static final OlympaBungeePermission BAN_BYPASSTIME = new OlympaBungeePermission(OlympaGroup.MODP);
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
	public static final OlympaBungeePermission BAN_BANMENU_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission PRIVATEMESSAGE_TOGGLE = new OlympaBungeePermission(OlympaGroup.MINI_YOUTUBER);
	public static final OlympaBungeePermission REPORT_SEEREPORT_OTHERSERV = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission VPN_BYPASS = new OlympaBungeePermission(OlympaGroup.FONDA);
	public static final OlympaBungeePermission INFO_COMMAND = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);
	public static final OlympaBungeePermission INFO_COMMAND_EXTRA = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission INFO_COMMAND_EXTRA_EXTRA = new OlympaBungeePermission(OlympaGroup.FONDA, OlympaGroup.ADMIN, OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission STAFF_CHAT = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);

	public static final OlympaBungeePermission BUNGEE_BROADCAST_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_BUILDER);
	public static final OlympaBungeePermission SERVER_LIST_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission PLAYER_LIST_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission SERVER_SWITCH_COMMAND = new OlympaBungeePermission(OlympaGroup.YOUTUBER);
	public static final OlympaBungeePermission FIND_COMMAND = new OlympaBungeePermission(OlympaGroup.BUILDER);
	public static final OlympaBungeePermission SERVER_START_COMMAND = new OlympaBungeePermission(OlympaGroup.GAMEMASTER);
	public static final OlympaBungeePermission SERVER_RESTART_BUNGEE_COMMAND = new OlympaBungeePermission(OlympaGroup.GAMEMASTER);
	public static final OlympaBungeePermission LOBBY_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission BPING_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);

	public static final OlympaBungeePermission BUNGEE_LAG_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);

	public static final OlympaBungeePermission BUNGEE_QUEUE_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission TEAMSPEAK_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission TEAMSPEAK_COMMAND_MANAGE = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission TEAMSPEAK_SEE_MODHELP = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static final OlympaBungeePermission TEAMSPEAK_SEE_ADMINHELP = new OlympaBungeePermission(OlympaGroup.MODP);

	public static final OlympaBungeePermission NICK_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission BUNGEE_COMMAND = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission BUNGEE_COMMAND_VPNINFO = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static final OlympaBungeePermission BUNGEE_COMMAND_TPS = new OlympaBungeePermission(OlympaGroup.PLAYER);
	public static final OlympaBungeePermission BUNGEE_COMMAND_CONFIGS = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission BUNGEE_COMMAND_MAXPLAYERS = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission BUNGEE_COMMAND_SETTINGS = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission BUNGEE_COMMAND_ANTIBOT = new OlympaBungeePermission(OlympaGroup.RESP_BUILDER, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission BUNGEE_COMMAND_CACHE = new OlympaBungeePermission(OlympaGroup.RESP_TECH, OlympaGroup.ADMIN).lockPermission();
	public static final OlympaBungeePermission BUNGEE_COMMAND_RESOURCE_PACK = new OlympaBungeePermission(OlympaGroup.RESP_TECH, OlympaGroup.ADMIN).lockPermission();

	public static final OlympaBungeePermission IP_COMMAND = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);
	public static final OlympaBungeePermission IP_COMMAND_SEE_IP = new OlympaBungeePermission(OlympaGroup.RESP_TECH);

	public static final OlympaBungeePermission STAFF = new OlympaBungeePermission(OlympaGroup.GRAPHISTE);
	public static final OlympaBungeePermission PING_COMMAND = new OlympaBungeePermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaBungeePermission ALLPLUGINS_COMMAND = new OlympaBungeePermission(OlympaGroup.FRIEND);
}
