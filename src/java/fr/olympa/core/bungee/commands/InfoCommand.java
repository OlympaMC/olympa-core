package fr.olympa.core.bungee.commands;

import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class InfoCommand extends BungeeCommand {

	public InfoCommand(Plugin plugin) {
		super(plugin, "info", OlympaCorePermissions.INFO_COMMAND);
		minArg = 0;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		OlympaPlayer target = null;
		ProxiedPlayer targetProxied = null;
		if (args.length != 0)
			try {
				target = AccountProvider.get(args[0]);
				if (target == null) {
					sendUnknownPlayer(args[0]);
					return;
				}
				targetProxied = ProxyServer.getInstance().getPlayer(target.getUniqueId());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		else if (proxiedPlayer != null) {
			target = getOlympaPlayer();
			targetProxied = proxiedPlayer;
		} else {
			sendImpossibleWithConsole();
			return;
		}

		TextComponent out = new TextComponent();
		TextComponent out2 = new TextComponent(TextComponent.fromLegacyText("§6Info " + target.getName()));
		out.addExtra(out2);
		out2 = new TextComponent(TextComponent.fromLegacyText("§3Première connexion: §b" + Utils.timestampToDuration(target.getFirstConnection())));
		out.addExtra("\n");
		out.addExtra(out2);
		if (targetProxied != null)
			out2 = new TextComponent(TextComponent.fromLegacyText("§3Dernière connexion: §b" + Utils.timestampToDuration(target.getLastConnection())));
		else
			out2 = new TextComponent(TextComponent.fromLegacyText("§3Connecté depuis: §b" + Utils.timestampToDuration(target.getLastConnection())));
		out.addExtra(out2);
		out.addExtra("\n");
		out2 = new TextComponent(TextComponent.fromLegacyText("§3Grades: §b" + target.getGroupsToHumainString()));
		out.addExtra(out2);
		out.addExtra("\n");
		TreeMap<Long, String> histName = target.getHistHame();
		if (!histName.isEmpty()) {
			int size = histName.size();
			String s = Utils.withOrWithoutS(size);
			out2 = new TextComponent(TextComponent.fromLegacyText("§3Ancien%s pseudo%s: ".replace("%s", s)));
			out.addExtra(out2);
			for (Entry<Long, String> entry : histName.entrySet()) {
				out2 = new TextComponent(TextComponent.fromLegacyText("§b" + entry.getValue()));
				out2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("Changé depuis " + Utils.timestampToDuration(entry.getKey()))));
				out.addExtra(out2);
				if (--size != 0)
					out.addExtra(new TextComponent(TextComponent.fromLegacyText("§3, §b")));
			}
			out.addExtra("\n");
		}
		out2 = new TextComponent(TextComponent.fromLegacyText("§3Premium: §b" + (target.isPremium() ? "oui" : "non")));
		out.addExtra(out2);
		out.addExtra("\n");
		out2 = new TextComponent(TextComponent.fromLegacyText("§3Link Discord: §b" + (target.getDiscordId() != 0 ? "oui" : "non")));
		out.addExtra(out2);
		out.addExtra("\n");
		if (hasPermission(OlympaCorePermissions.INFO_COMMAND_EXTRA)) {
			out2 = new TextComponent(TextComponent.fromLegacyText("§3IP: §b[Cachée]"));
			out2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§c" + target.getIp())));
			out2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, target.getIp()));
			out.addExtra(out2);
			out.addExtra("\n");
		}
		List<OlympaSanction> sanctions = BanMySQL.getSanctions(target.getUniqueId());
		out2 = new TextComponent(TextComponent.fromLegacyText("§3Sanctions: §b" + sanctions.size()));
		out.addExtra(out2);
		out.addExtra("\n");
		out.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("")));
		sendMessage(out);
	}

}
