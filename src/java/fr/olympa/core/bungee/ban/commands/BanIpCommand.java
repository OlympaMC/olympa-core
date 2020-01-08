package fr.olympa.core.bungee.ban.commands;

import java.sql.SQLException;
import java.util.UUID;

import fr.olympa.api.objects.OlympaConsole;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Matcher;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.commands.methods.BanIp;
import fr.olympa.core.bungee.utils.BungeeConfigUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

@SuppressWarnings("deprecation")
public class BanIpCommand extends BungeeCommand {

	public BanIpCommand(Plugin plugin) {
		super(plugin, "banip", OlympaCorePermissions.BAN_BANIP_COMMAND, "tempbanip");
		this.minArg = 2;
		this.usageString = BungeeConfigUtils.getString("bungee.ban.messages.usageban");
		this.register();
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		UUID author;
		if (sender instanceof ProxiedPlayer) {
			author = this.proxiedPlayer.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

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
					BanIp.addBanIP(author, sender, olympaTarget.getIp(), args, this.olympaPlayer);
				}

			} else if (Matcher.isFakeIP(args[0])) {

				if (Matcher.isIP(args[0])) {
					BanIp.addBanIP(author, sender, args[0], args, this.olympaPlayer);

				} else {
					sender.sendMessage(BungeeConfigUtils.getString("commun.messages.ipinvalid").replaceAll("%ip%", args[0]));
					return;
				}

			} else if (Matcher.isFakeUUID(args[0])) {

				if (Matcher.isUUID(args[0])) {
					OlympaPlayer olympaTarget = new AccountProvider(UUID.fromString(args[0])).get();
					if (olympaTarget != null) {
						BanIp.addBanIP(author, sender, olympaTarget.getIp(), args, this.olympaPlayer);
					}

				} else {
					sender.sendMessage(BungeeConfigUtils.getString("commun.messages.uuidinvalid").replaceAll("%uuid%", args[0]));
					return;
				}

			} else {
				sender.sendMessage(BungeeConfigUtils.getString("commun.messages.typeunknown").replaceAll("%type%", args[0]));
				return;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			sender.sendMessage(BungeeConfigUtils.getString("ban.errordb"));
			return;
		}
	}
}
