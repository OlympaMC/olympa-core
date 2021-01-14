package fr.olympa.core.spigot.commands;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.CacheStats;

public class NewSpigotCommand extends ComplexCommand {

	public NewSpigotCommand(Plugin plugin) {
		super(plugin, "spigot", "Diverses gestion du serveur spigot.", OlympaCorePermissions.SPIGOT_COMMAND, "spig");
		addArgumentParser("CACHE", sender -> CacheStats.getCaches().keySet(), x -> CacheStats.getCache(x), x -> "&4%s&c doit être un id de cache qui existe.");
		addArgumentParser("DEBUG_LIST", sender -> CacheStats.getDebugLists().keySet(), x -> CacheStats.getDebugList(x), x -> "&4%s&c doit être un id de debugList qui existe.");
		addArgumentParser("DEBUG_MAP", sender -> CacheStats.getDebugMaps().keySet(), x -> CacheStats.getDebugMap(x), x -> "&4%s&c doit être un id de debugMap qui existe.");
	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "CACHE", "clear|print" })
	public void cache(CommandContext cmd) {
		CacheStats.executeOnCache(this, cmd);
	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "DEBUG_LIST", "clear|print" })
	public void list(CommandContext cmd) {
		CacheStats.executeOnList(this, cmd);
	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "DEBUG_MAP", "clear|print" })
	public void map(CommandContext cmd) {
		CacheStats.executeOnMap(this, cmd);
	}
}
