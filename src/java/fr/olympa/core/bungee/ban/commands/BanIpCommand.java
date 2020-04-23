package fr.olympa.core.bungee.ban.commands;

import java.sql.SQLException;
import java.util.UUID;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.commands.methods.BanIp;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class BanIpCommand extends BungeeCommand {

	public static OlympaPermission permToBandef;

	public BanIpCommand(OlympaBungee plugin) {
		super(plugin, "banip", OlympaCorePermissions.BAN_BANIP_COMMAND, "tempbanip");
		permToBandef = OlympaCorePermissions.BAN_BANIPDEF_COMMAND;
		minArg = 2;
		usageString = plugin.getConfig().getString("ban.messages.usageban");
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if (sender instanceof ProxiedPlayer) {
			author = proxiedPlayer.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		Configuration config = OlympaBungee.getInstance().getConfig();
		try {
			if (Matcher.isUsername(args[0])) {

				ProxiedPlayer target = ProxyServer.getInstance().getPlayer(args[0]);
				OlympaPlayer olympaTarget;
				if (target != null) {
					olympaTarget = AccountProvider.get(target.getUniqueId());
				} else {
					olympaTarget = AccountProvider.getFromDatabase(args[0]);
				}
				if (olympaTarget != null) {
					BanIp.addBanIP(author, sender, olympaTarget.getIp(), args, olympaPlayer);
				}

			} else if (Matcher.isFakeIP(args[0])) {

				if (Matcher.isIP(args[0])) {
					BanIp.addBanIP(author, sender, args[0], args, olympaPlayer);

				} else {
					sendMessage(Prefix.DEFAULT_BAD, config.getString("default.ipinvalid").replaceAll("%ip%", args[0]));
					return;
				}

			} else if (Matcher.isFakeUUID(args[0])) {

				if (Matcher.isUUID(args[0])) {
					OlympaPlayer olympaTarget = new AccountProvider(UUID.fromString(args[0])).get();
					if (olympaTarget != null) {
						BanIp.addBanIP(author, sender, olympaTarget.getIp(), args, olympaPlayer);
					}

				} else {
					sendMessage(Prefix.DEFAULT_BAD, config.getString("default.uuidinvalid").replaceAll("%uuid%", args[0]));
					return;
				}

			} else {
				sendMessage(Prefix.DEFAULT_BAD, config.getString("default.typeunknown").replaceAll("%type%", args[0]));
				return;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			sendMessage(Prefix.DEFAULT_BAD, config.getString("ban.errordb"));
			return;
		}
	}
}
