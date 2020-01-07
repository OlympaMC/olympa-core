package fr.olympa.bungee.ban.commands;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.bungee.api.command.BungeeCommand;
import fr.olympa.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class ForceKickCommand extends BungeeCommand {

	public ForceKickCommand(Plugin plugin) {
		super(plugin, "forcekick", OlympaCorePermissions.BAN_FORCEKICK_COMMAND, "forceeject");
		this.minArg = 1;
		this.usageString = BungeeConfigUtils.getString("ban.messages.usagekick");
		this.register();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {

		if (Matcher.isUsername(args[0])) {
			ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
			if (target == null) {
				this.sendMessage(SpigotUtils.color(Prefix.DEFAULT_BAD + "Le joueur &4" + args[0] + "&c n'est pas connecté."));
				return;
			}
			target.getPendingConnection().disconnect();
			this.sendMessage(SpigotUtils.color(Prefix.DEFAULT_GOOD + "Le joueur &2" + target.getName() + "&a a été kick."));

		} else {
			this.sendUsage();
		}
	}

}