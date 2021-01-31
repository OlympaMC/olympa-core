package fr.olympa.core.spigot.commands;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.chat.TableGenerator;
import fr.olympa.api.chat.TableGenerator.Alignment;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.CacheStats;

public class NewSpigotCommand extends ComplexCommand {

	private static Set<String> aligements = Arrays.stream(Alignment.values()).map(Alignment::name).collect(Collectors.toSet());

	public NewSpigotCommand(Plugin plugin) {
		super(plugin, "spigot", "Diverses gestion du serveur spigot.", OlympaCorePermissions.SPIGOT_COMMAND, "spig");
		addArgumentParser("CACHE", sender -> CacheStats.getCaches().keySet(), x -> CacheStats.getCache(x), x -> "&4%s&c doit être un id de cache qui existe.");
		addArgumentParser("DEBUG_LIST", sender -> CacheStats.getDebugLists().keySet(), x -> CacheStats.getDebugList(x), x -> "&4%s&c doit être un id de debugList qui existe.");
		addArgumentParser("DEBUG_MAP", sender -> CacheStats.getDebugMaps().keySet(), x -> CacheStats.getDebugMap(x), x -> "&4%s&c doit être un id de debugMap qui existe.");
		addArgumentParser("ALIGNMENT", sender -> aligements, x -> CacheStats.getDebugMap(x), x -> "&4%s&c doit être un alignement tel que " + String.join(", ", aligements) + " .");
	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "CACHE", "clear|print" })
	public void cache(CommandContext cmd) {
		CacheStats.executeOnCache(this, cmd);
	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "DEBUG_LIST", "clear|print" })
	public void list(CommandContext cmd) {
		CacheStats.executeOnList(this, cmd);
	}

	@Cmd(args = { "ALIGNMENT", "ALIGNMENT" })
	public void table(CommandContext cmd) {
		//		Alignment
		TableGenerator table = new TableGenerator();
		CacheStats.executeOnList(this, cmd);
	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "DEBUG_MAP", "clear|print" })
	public void map(CommandContext cmd) {
		CacheStats.executeOnMap(this, cmd);
	}
}
