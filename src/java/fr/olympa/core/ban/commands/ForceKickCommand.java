package fr.olympa.core.ban.commands;

import fr.tristiisch.emeraldmc.api.bungee.commands.BungeeCommand;
import fr.tristiisch.emeraldmc.api.bungee.utils.BungeeConfigUtils;
import fr.tristiisch.emeraldmc.api.commons.Matcher;
import fr.tristiisch.emeraldmc.api.commons.Prefix;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import fr.tristiisch.emeraldmc.api.commons.object.EmeraldGroup;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class ForceKickCommand extends BungeeCommand {

	public ForceKickCommand(Plugin plugin) {
		super(plugin, "forcekick", EmeraldGroup.RESPMODO, "forceeject");
		this.minArg = 1;
		this.usageString = BungeeConfigUtils.getString("bungee.ban.messages.usagekick");
		this.register();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {

		if(Matcher.isUsername(args[0])) {
			ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
			if(target == null) {
				this.sendMessage(Utils.color(Prefix.DEFAULT_BAD + "Le joueur &4" + args[0] + "&c n'est pas connecté."));
				return;
			}
			target.getPendingConnection().disconnect();
			this.sendMessage(Utils.color(Prefix.DEFAULT_GOOD + "Le joueur &2" + target.getName() + "&a a été kick."));

		} else {
			this.sendUsage();
		}
	}

}
