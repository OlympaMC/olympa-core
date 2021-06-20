package fr.olympa.core.bungee.motd;

import fr.olympa.api.bungee.command.BungeeComplexCommand;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.plugin.Plugin;

public class ShowPingMotdCommand extends BungeeComplexCommand {

	public ShowPingMotdCommand(Plugin plugin) {
		super(plugin, "showpingmotd", "Affiche dans la console les joueurs qui ping le serveur bungee", OlympaCorePermissionsBungee.PING_COMMAND, "pingmotd");
	}

	@Cmd
	public void on(CommandContext cmd) {
		ShowPingMotdListener.isEnable = true;
		sendMessage(Prefix.DEFAULT_GOOD, "La console affichera désormais les ping motd.");
	}

	@Cmd
	public void off(CommandContext cmd) {
		ShowPingMotdListener.isEnable = false;
		sendMessage(Prefix.DEFAULT_BAD, "La console n'affichera plus les ping motd.");
	}

	/*@Cmd
	public void debugoff(CommandContext cmd) {
		ShowPingMotdListener.debug = false;
		sendMessage(Prefix.DEFAULT_BAD, "La console n'affichera plus le debug des ping motd.");
	}

	@Cmd
	public void debugon(CommandContext cmd) {
		ShowPingMotdListener.debug = true;
		ShowPingMotdListener.isEnable = true;
		sendMessage(Prefix.DEFAULT_GOOD, "La console affichera désormais le debug des ping motd.");
	}*/
}
