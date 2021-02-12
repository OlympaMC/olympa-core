package fr.olympa.core.spigot.commands;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.captcha.MapCaptcha;
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

	public NewSpigotCommand(Plugin plugin) {
		super(plugin, "spigot", "Diverses gestion du serveur spigot.", OlympaCorePermissions.SPIGOT_COMMAND, "spig");
		addArgumentParser("CACHE", (sender, msg) -> CacheStats.getCaches().keySet(), x -> CacheStats.getCache(x), x -> "&4%s&c doit être un id de cache qui existe.");
		addArgumentParser("DEBUG_LIST", (sender, msg) -> CacheStats.getDebugLists().keySet(), x -> CacheStats.getDebugList(x), x -> "&4%s&c doit être un id de debugList qui existe.");
		addArgumentParser("DEBUG_MAP", (sender, msg) -> CacheStats.getDebugMaps().keySet(), x -> CacheStats.getDebugMap(x), x -> "&4%s&c doit être un id de debugMap qui existe.");
		addArgumentParser("ALIGNMENT", Alignment.class);
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

	@Cmd(min = 1, args = "INTEGER")
	public void captcha(CommandContext cmd) {
		getPlayer().getInventory().addItem(new MapCaptcha(cmd.getArgument(0)).getMap());
	}

	//	@Cmd(args = { "ALIGNMENT", "ALIGNMENT" })
	//	public void table(CommandContext cmd) {
	//		//		Alignment
	//		TableGenerator table = new TableGenerator();
	//		CacheStats.executeOnList(this, cmd);
	//	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "DEBUG_MAP", "clear|print" })
	public void map(CommandContext cmd) {
		CacheStats.executeOnMap(this, cmd);
	}

	@Cmd(args = { "MODULES|debug", "ON|OFF" }, min = 1)
	public void module(CommandContext cmd) {
		String arg1 = cmd.getArgumentsLength() > 1 ? (String) cmd.getArgument(1) : "";
		Boolean toOn = cmd.getArgumentsLength() > 1 ? arg1.equalsIgnoreCase("ON") ? true : !arg1.equalsIgnoreCase("OFF") : null;
		Object arg0 = cmd.getArgument(0);
		if (arg0 instanceof String) {
			if (toOn == null)
				sendMessage(Prefix.DEFAULT, "Le module &8%s&7 est %s", "de debug", OlympaModule.DEBUG ? "&2Activé" : "&4Désativé");
			else if (toOn) {
				OlympaModule.DEBUG = true;
				sendMessage(Prefix.DEFAULT_GOOD, "Le module %s est désormais %s", "de debug", OlympaModule.DEBUG ? "&2Activé" : "&4Désativé");
			} else {
				OlympaModule.DEBUG = false;
				sendMessage(Prefix.DEFAULT_BAD, "Le module %s est désormais %s", "de debug", OlympaModule.DEBUG ? "&2Activé" : "&4Désativé");
			}
		} else {
			OlympaModule<? extends Object, Listener, ? extends Plugin, OlympaCommand> module = cmd.getArgument(0);
			if (toOn == null)
				sendMessage(Prefix.DEFAULT, "Le module &8%s&7 est %s", module.getName(), module.isEnabledString());
			else if (toOn) {
				module.enable();
				sendMessage(Prefix.DEFAULT_GOOD, "Démarrage du module %s, il est désormais %s", module.getName(), module.isEnabledString());
			} else {
				module.disable();
				sendMessage(Prefix.DEFAULT_BAD, "Arrêt du module %s, il est désormais %s", module.getName(), module.isEnabledString());
			}

		}
	}
}
