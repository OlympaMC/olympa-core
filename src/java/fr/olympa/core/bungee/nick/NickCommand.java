package fr.olympa.core.bungee.nick;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.commun.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class NickCommand extends BungeeCommand {

	public NickCommand(Plugin plugin) {
		super(plugin, "nick", OlympaCorePermissionsBungee.NICK_COMMAND);
		minArg = 1;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		proxiedPlayer.setDisplayName(args[0]);
		sendMessage(Prefix.DEFAULT_GOOD, "Tu nom est désormais &2%s&a.", args[0]);
	}

}
