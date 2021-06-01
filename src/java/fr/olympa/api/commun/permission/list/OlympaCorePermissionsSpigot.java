package fr.olympa.api.commun.permission.list;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaSpigotPermission;

public class OlympaCorePermissionsSpigot {
	
	public static final OlympaSpigotPermission PERMISSION_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV }, true);
	public static final OlympaSpigotPermission SETSTATUS_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission GROUP_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.RESP_STAFF });
	
	public static final OlympaSpigotPermission CHAT_COMMAND = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission CHAT_SEEINSULTS = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	public static final OlympaSpigotPermission CHAT_BYPASS = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission CHAT_MUTEDBYPASS = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	
	public static final OlympaSpigotPermission UTILS_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	public static final OlympaSpigotPermission GETUUID_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	
	public static final OlympaSpigotPermission CHAT_COLOR = new OlympaSpigotPermission(OlympaGroup.RESP_TECH);
	
	public static final OlympaSpigotPermission REPORT_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission REPORT_SEE_NOTIF = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaSpigotPermission REPORT_SEE_COMMAND = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaSpigotPermission REPORT_CHANGE_COMMAND = new OlympaSpigotPermission(OlympaGroup.ASSISTANT);
	
	public static final OlympaSpigotPermission STAFF = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	
	public static final OlympaSpigotPermission SERVER_RESTART_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	// TODO change to OlympaGroup.GRAPHISTE, change to OlympaGroup.ASSISTANT later
	//public static OlympaSpigotPermission SERVER_BYPASS_MAITENANCE_SPIGOT = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	
	public static final OlympaSpigotPermission GENDER_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	
	public static final OlympaSpigotPermission SPIGOT_LAG_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission SPIGOT_CONFIG_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	
	public static final OlympaSpigotPermission VERIFMOD_SEE_IN_TAB = new OlympaSpigotPermission(OlympaGroup.GRAPHISTE);
	public static final OlympaSpigotPermission VANISH_SEE_IN_TAB = new OlympaSpigotPermission(OlympaGroup.MODP);
	
	public static final OlympaSpigotPermission SPIGOT_COMMAND = new OlympaSpigotPermission(OlympaGroup.PLAYER);
	public static final OlympaSpigotPermission SPIGOT_COMMAND_ANTIBOT = new OlympaSpigotPermission(OlympaGroup.RESP_BUILDER, new OlympaGroup[] { OlympaGroup.DEV });
	public static final OlympaSpigotPermission SPIGOT_COMMAND_CACHE = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, OlympaGroup.ADMIN).lockPermission();
	
	public static final OlympaSpigotPermission NAMETAG_COMMAND = new OlympaSpigotPermission(OlympaGroup.RESP_TECH, new OlympaGroup[] { OlympaGroup.DEV });
	
	public static final OlympaSpigotPermission BUNGEE_SECURITY_COMMAND = new OlympaSpigotPermission(OlympaGroup.ADMIN, OlympaGroup.RESP_TECH); // XXX what ?
	
}
