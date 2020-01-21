package fr.olympa.api.permission;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.core.bungee.api.permission.OlympaBungeePermission;

public class OlympaCorePermissions {

	public static OlympaPermission DEV = new OlympaPermission(OlympaGroup.ADMIN_SYS, OlympaGroup.RESP_TECH);
	// TODO change to OlympaGroup.ADMIN
	public static OlympaPermission MAINTENANCE_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission GROUP_COMMAND = new OlympaPermission(OlympaGroup.RESP_TECH);

	public static OlympaPermission CHAT_COMMAND = new OlympaPermission(OlympaGroup.MOD);
	public static OlympaPermission CHAT_SEEINSULTS = new OlympaPermission(OlympaGroup.MOD);
	public static OlympaPermission CHAT_BYPASS = new OlympaPermission(OlympaGroup.MODP);
	public static OlympaPermission CHAT_MUTEDBYPASS = new OlympaPermission(OlympaGroup.MOD);

	public static OlympaPermission BAN_BYPASS_MAXTIME = new OlympaBungeePermission(OlympaGroup.FONDA);
	public static OlympaPermission BAN_BYPASS_SANCTION_STAFF = new OlympaBungeePermission(OlympaGroup.RESP_TECH);
	public static OlympaPermission BAN_FORCEKICK_COMMAND = new OlympaPermission(OlympaGroup.ASSISTANT);
	public static OlympaPermission BAN_DEF = new OlympaBungeePermission(OlympaGroup.MODP);
	public static OlympaPermission BAN_BANIP_COMMAND = new OlympaBungeePermission(OlympaGroup.MODP);
	public static OlympaPermission BAN_UNBAN_COMMAND = new OlympaBungeePermission(OlympaGroup.MODP);
	public static OlympaPermission BAN_DELBAN_COMMAND = new OlympaBungeePermission(OlympaGroup.DEV);
	public static OlympaPermission BAN_KICK_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_MUTE_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_UNMUTE_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_BANHIST_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_BAN_COMMAND = new OlympaBungeePermission(OlympaGroup.MOD);
	public static OlympaPermission BAN_HISTORY_COMMAND = new OlympaBungeePermission(OlympaGroup.ASSISTANT);
	public static OlympaPermission BAN_SEEBANMSG = new OlympaBungeePermission(OlympaGroup.BUILDER);
	public static OlympaPermission BAN_BYPASS_BAN = new OlympaBungeePermission(OlympaGroup.BUILDER);

	public static OlympaPermission SPAWN_SPAWN_COMMAND_SET = new OlympaPermission(OlympaGroup.RESP_TECH);

	public static OlympaPermission CHAT_COLOR = new OlympaPermission(OlympaGroup.MODP);

	public static OlympaPermission STAFF = new OlympaPermission(OlympaGroup.GRAPHISTE);
	public static OlympaPermission MESSAGE_TOGGLE = new OlympaPermission(OlympaGroup.MINI_YOUTUBER);

}
