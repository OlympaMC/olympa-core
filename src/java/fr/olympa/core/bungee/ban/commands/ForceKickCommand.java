package fr.olympa.core.bungee.ban.commands;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.match.RegexMatcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ForceKickCommand extends BungeeCommand {

	public ForceKickCommand(OlympaBungee plugin) {
		super(plugin, "forcekick", OlympaCorePermissionsBungee.BAN_FORCEKICK_COMMAND, "forceeject");
		minArg = 1;
		usageString = plugin.getConfig().getString("ban.usagekick");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (RegexMatcher.USERNAME.is(args[0])) {
			ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
			if (target == null) {
				this.sendMessage(Prefix.DEFAULT_BAD + "Le joueur &4" + args[0] + "&c n'est pas connecté.");
				return;
			}
			boolean force = args.length > 1 && Boolean.parseBoolean(args[1]);
			PendingConnection connection = target.getPendingConnection();
			try {
				Field channelField = connection.getClass().getField("ch");
				channelField.setAccessible(true);
				Object channel = channelField.get(connection);
				Method isClosing = channel.getClass().getDeclaredMethod("isClosing");
				Method isClosed = channel.getClass().getDeclaredMethod("isClosed");
				this.sendInfo("Connection is closing: %s, is closed: %s", isClosing.invoke(channel), isClosed.invoke(channel));
			}catch (ReflectiveOperationException ex) {
				ex.printStackTrace();
			}
			connection.disconnect();
			this.sendMessage(Prefix.DEFAULT_GOOD + "Le joueur &2" + target.getName() + "&a a été kick.");
			if (force) {
				try {
					Method removeConnection = ProxyServer.getInstance().getClass().getDeclaredMethod("removeConnection", connection.getClass());
					removeConnection.invoke(ProxyServer.getInstance(), connection);
					sendSuccess("Le joueur §2%s§a a été force-kick, sa connection est retirée.");
				}catch (ReflectiveOperationException ex) {
					ex.printStackTrace();
				}
			}

		} else
			sendUsage(command);
	}

}
