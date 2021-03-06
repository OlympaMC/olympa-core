package fr.olympa.core.spigot.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.chat.TableGenerator.Alignment;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.spigot.captcha.MapCaptcha;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.utils.CacheStats;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.core.spigot.login.PlayerLogin;

public class NewSpigotCommand extends ComplexCommand {

	public NewSpigotCommand(Plugin plugin) {
		super(plugin, "spigot", "Diverses gestion du serveur spigot.", OlympaCorePermissionsSpigot.SPIGOT_COMMAND, "spig");
		addArgumentParser("CACHE", (sender, msg) -> CacheStats.getCaches().keySet(), x -> CacheStats.getCache(x), x -> "&4%s&c doit être un id de cache qui existe.");
		addArgumentParser("DEBUG_LIST", (sender, msg) -> CacheStats.getDebugLists().keySet(), x -> CacheStats.getDebugList(x), x -> "&4%s&c doit être un id de debugList qui existe.");
		addArgumentParser("DEBUG_MAP", (sender, msg) -> CacheStats.getDebugMaps().keySet(), x -> CacheStats.getDebugMap(x), x -> "&4%s&c doit être un id de debugMap qui existe.");
		addArgumentParser("ALIGNMENT", Alignment.class);
		addArgumentParser("MODULES", (sender, msg) -> OlympaModule.getModulesNames(), x -> OlympaModule.getModule(x),
				x -> String.format("&4%s&c n'est pas un module, essaye &4%s&c.", x, String.join(", ", OlympaModule.getModulesNames())));
	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "CACHE", "clear|print|stats" }, description = "Affiche tous les Caches qui ont été enregister")
	public void cache(CommandContext cmd) {
		CacheStats.executeOnCache(this, cmd);
	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "DEBUG_LIST", "clear|print" }, description = "Affiche toutes les List<> qui ont été enregister")
	public void list(CommandContext cmd) {
		CacheStats.executeOnList(this, cmd);
	}

	@Cmd(permissionName = "SPIGOT_COMMAND_CACHE", args = { "DEBUG_MAP", "clear|print|remove" }, description = "Affiche tous les Map<K,V> qui ont été enregister")
	public void map(CommandContext cmd) {
		CacheStats.executeOnMap(this, cmd);
	}

	@Cmd(min = 1, args = "INTEGER|PLAYERS")
	public void captcha(CommandContext cmd) {
		if (cmd.getArgument(0) instanceof Player)
			PlayerLogin.captchaToPlayer(cmd.getArgument(0));
		else
			getPlayer().getInventory().addItem(new MapCaptcha(cmd.getArgument(0)).getMap());
	}

	@Cmd
	public void offlinePlayers(CommandContext cmd) {
		sender.sendMessage("Joueurs offlines : " + plugin.getServer().getOfflinePlayers().length);
	}

	//	@Cmd(args = { "ALIGNMENT", "ALIGNMENT" })
	//	public void table(CommandContext cmd) {
	//		//		Alignment
	//		TableGenerator table = new TableGenerator();
	//		CacheStats.executeOnList(this, cmd);
	//	}

	@Cmd(args = "on|off", min = 1, description = "Active ou désactive le mode debug global sur les modules")
	public void debug(CommandContext cmd) {
		String arg0 = cmd.getArgument(0);
		Boolean toOn;
		if (arg0.equalsIgnoreCase("on"))
			toOn = true;
		else if (arg0.equalsIgnoreCase("off"))
			toOn = false;
		else
			toOn = null;
		if (toOn == null)
			sendMessage(Prefix.DEFAULT, "Le module &8%s&7 est %s", "de debug", OlympaModule.DEBUG ? "&2Activé" : "&4Désativé");
		else if (toOn) {
			OlympaModule.DEBUG = true;
			sendMessage(Prefix.DEFAULT_GOOD, "Le module %s est désormais %s", "de debug", OlympaModule.DEBUG ? "&2Activé" : "&4Désativé");
		} else {
			OlympaModule.DEBUG = false;
			sendMessage(Prefix.DEFAULT_BAD, "Le module %s est désormais %s", "de debug", OlympaModule.DEBUG ? "&2Activé" : "&4Désativé");
		}
	}

	@Cmd (permissionName = "SPIGOT_COMMAND_MODULE", args = { "MODULES", "on|off|debugon|debugoff" }, min = 1, description = "Active ou désactive un module")
	public void module(CommandContext cmd) {
		OlympaModule<? extends Object, Listener, ? extends Plugin, OlympaCommand> module = cmd.getArgument(0);
		if (cmd.getArgumentsLength() == 1) {
			sendMessage(Prefix.DEFAULT, "Le module &8%s&7 est %s", module.getName(), module.isEnabledString());
			return;
		}
		try {
			switch (cmd.<String>getArgument(1).toLowerCase()) {
			case "on":
				module.enableModule();
				sendMessage(Prefix.DEFAULT_GOOD, "Démarrage du module %s, il est désormais %s", module.getName(), module.isEnabledString());
				break;
			case "off":
				module.disableModule();
				sendMessage(Prefix.DEFAULT_BAD, "Arrêt du module %s, il est désormais %s", module.getName(), module.isEnabledString());
				break;
			case "debugon":
				module.setDebug(true);
				sendMessage(Prefix.DEFAULT_GOOD, "Le mode DEBUG du module %s est désormais", module.getName(), module.isEnabledString());
				break;
			case "debugoff":
				module.setDebug(false);
				sendMessage(Prefix.DEFAULT_GOOD, "Le mode DEBUG du module %s est désormais", module.getName(), module.isEnabledString());
				break;
			default:
				sendIncorrectSyntax();
				break;
			}
		} catch (Exception e) {
			sendError(e);
			e.printStackTrace();
		}
	}

	@Cmd (args = "INTEGER", syntax = "[joueurs maximum]", description = "Permet de voir ou de changer le nombre maximal de joueurs en ligne.")
	public void maxPlayers(CommandContext cmd) {
		int oldSlots = Bukkit.getMaxPlayers();
		if (cmd.getArgumentsLength() == 0)
			sendSuccess("Nombre maximal de joueurs: %d (%d en ligne).", oldSlots, Bukkit.getOnlinePlayers().size());
		else {
			int newSlots = cmd.getArgument(0);
			if (newSlots <= 0)
				sendError("Voyons... Le nombre maximal de joueur doit être positif !");
			else {
				Bukkit.setMaxPlayers(newSlots);
				OlympaCorePermissionsSpigot.SPIGOT_COMMAND.sendMessage(Prefix.BROADCAST_SERVER.formatMessage("Le nombre maximal de joueurs est passé de %d à %d slots!", oldSlots, newSlots));
			}
		}
	}
}
