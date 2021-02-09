package fr.olympa.core.spigot.commands;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.chat.TableGenerator.Alignment;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.module.OlympaModule;
import fr.olympa.api.module.PluginModule;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.Prefix;

public class NewSpigotCommand extends ComplexCommand {

	private static Set<String> aligements = Arrays.stream(Alignment.values()).map(Alignment::name).collect(Collectors.toSet());

	public NewSpigotCommand(Plugin plugin) {
		super(plugin, "spigot", "Diverses gestion du serveur spigot.", OlympaCorePermissions.SPIGOT_COMMAND, "spig");
		addArgumentParser("CACHE", (sender, msg) -> CacheStats.getCaches().keySet(), x -> CacheStats.getCache(x), x -> "&4%s&c doit être un id de cache qui existe.");
		addArgumentParser("DEBUG_LIST", (sender, msg) -> CacheStats.getDebugLists().keySet(), x -> CacheStats.getDebugList(x), x -> "&4%s&c doit être un id de debugList qui existe.");
		addArgumentParser("DEBUG_MAP", (sender, msg) -> CacheStats.getDebugMaps().keySet(), x -> CacheStats.getDebugMap(x), x -> "&4%s&c doit être un id de debugMap qui existe.");
		addArgumentParser("ALIGNMENT", (sender, msg) -> aligements, x -> CacheStats.getDebugMap(x), x -> "&4%s&c doit être un alignement tel que " + String.join(", ", aligements) + " .");
		addArgumentParser("MODULES", (sender, msg) -> PluginModule.getModulesNames(), x -> PluginModule.getModule(x), x -> "&4%s&c n'est pas un module, essaye " + String.join(", ", PluginModule.getModulesNames()) + " .");
	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "CACHE", "clear|print" })
	public void cache(CommandContext cmd) {
		CacheStats.executeOnCache(this, cmd);
	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "DEBUG_LIST", "clear|print" })
	public void list(CommandContext cmd) {
		CacheStats.executeOnList(this, cmd);
	}

	//	@Cmd(args = { "ALIGNMENT", "ALIGNMENT" })
	//	public void table(CommandContext cmd) {
	//		//		Alignment
	//		TableGenerator table = new TableGenerator();
	//		CacheStats.executeOnList(this, cmd);
	//	}

	@Cmd(args = { "MODULES", "ON|OFF" }, min = 1)
	public void module(CommandContext cmd) {
		Boolean toOn = cmd.getArgumentsLength() > 1 ? ((String) cmd.getArgument(1)).equalsIgnoreCase("ON") : null;
		OlympaModule<? extends Object, Listener, ? extends Plugin, OlympaCommand> module = cmd.getArgument(0);
		if (toOn == null)
			sendMessage(Prefix.DEFAULT, "Le module &8%s&7 est %s", module.getName(), module.isEnabledString());
		else if (toOn) {
			module.enable();
			sendMessage(Prefix.DEFAULT, "Démarrage du module %s, il est désormais %s", module.getName(), module.isEnabledString());
		} else {
			module.disable();
			sendMessage(Prefix.DEFAULT, "Arrêt du module %s, il est désormais %s", module.getName(), module.isEnabledString());
		}

	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "DEBUG_MAP", "clear|print" })
	public void map(CommandContext cmd) {
		CacheStats.executeOnMap(this, cmd);
	}
}
