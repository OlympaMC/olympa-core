package fr.olympa.core.bungee.ban.commands;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.execute.SanctionExecute;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionStatus;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;

public class BanHistoryCommand extends BungeeCommand {

	public BanHistoryCommand(OlympaBungee plugin) {
		super(plugin, "banhistory", OlympaCorePermissions.BAN_HISTORY_COMMAND, "banhist", "mutehist", "kickhist", "hist", "histban");
		minArg = 1;
		usageString = "<joueur|uuid|id|ip>";
	}

	//	@Override
	//	public void onCommand(CommandSender sender, String[] args) {
	//		String identifier = args[0];
	//
	//		String id = null;
	//		String target = null;
	//		try {
	//			if (RegexMatcher.LONG.is(identifier))
	//				id = identifier;
	//			else if (RegexMatcher.IP.is(identifier)) {
	//				if (!OlympaCorePermissions.BAN_BANIP_COMMAND.hasSenderPermissionBungee(sender)) {
	//					sendDoNotHavePermission();
	//					return;
	//				}
	//				target = identifier;
	//			} else if (RegexMatcher.UUID.is(identifier)) {
	//				OlympaPlayer op = new AccountProvider((UUID) RegexMatcher.UUID.parse(identifier)).get();
	//				target = String.valueOf(op.getId());
	//			} else if (RegexMatcher.USERNAME.is(identifier)) {
	//				OlympaPlayer op;
	//				op = AccountProvider.get(identifier);
	//				target = String.valueOf(op.getId());
	//			} else {
	//				Configuration config = OlympaBungee.getInstance().getConfig();
	//				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.typeunknown").replace("%type%", args[0]));
	//				return;
	//			}
	//		} catch (IllegalArgumentException | SQLException e) {
	//			e.printStackTrace();
	//			sendError(e);
	//		}
	//
	//	}
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		SanctionExecute banArg = SanctionExecute.formatArgs(this, args);
		banArg.printInfo();
	}

	@SuppressWarnings("deprecation")
	public static void histSanctions(CommandSender sender, String target) {
		Configuration config = OlympaBungee.getInstance().getConfig();
		List<OlympaSanction> sanctions = BanMySQL.getSanctions(target);
		if (sanctions == null) {
			sender.sendMessage(config.getString("ban.messages.errordb"));
			return;
		}
		if (sanctions.size() == 0) {
			sender.sendMessage(Prefix.DEFAULT_BAD.formatMessageB("&4%s&c n'a jamais été sanctionné.", target));
			return;
		}
		TextComponent msg = new TextComponent(ColorUtils.color("&6Sanctions de " + target + "&6:\n"));
		List<OlympaSanction> bans = new ArrayList<>();
		List<OlympaSanction> mutes = new ArrayList<>();
		List<OlympaSanction> kicks = new ArrayList<>();
		sanctions.stream().forEach(b -> {
			switch (b.getType()) {
			case BAN:
			case BANIP:
				bans.add(b);
				break;
			case MUTE:
			case MUTEIP:
				mutes.add(b);
				break;
			case KICK:
				kicks.add(b);
				break;
			default:
				break;
			}
		});
		msg.addExtra(ColorUtils.format("&cBan&7/&6Mute&7/&bKick&7: &c%s&7/&6%s&7/&b%s", bans.size(), mutes.size(), kicks.size()));
		msg.addExtra(ColorUtils.format("&6Temps de &cTempBan&7/&6TempMute&7: &c%s&7/&6%s",
				Utils.timestampToDuration(bans.stream().filter(s -> !s.isPermanent() && !s.getStatus().isStatus(OlympaSanctionStatus.CANCEL)).mapToLong(OlympaSanction::getBanTime).sum()),
				Utils.timestampToDuration(mutes.stream().filter(s -> !s.isPermanent() && !s.getStatus().isStatus(OlympaSanctionStatus.CANCEL)).mapToLong(OlympaSanction::getBanTime).sum()),
				kicks.size()));
		msg.addExtra(ColorUtils.format("&6Connu pour (%s) : ", sanctions.size()));
		sanctions.stream().forEach(b -> {
			BaseComponent[] comp = new ComponentBuilder(
					ColorUtils.format("&6%s %s &e%s\n", b.getType().getName(!b.isPermanent()).toUpperCase(), b.getStatus().getNameColored().toUpperCase(), b.getReason()))
							.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, b.toBaseComplement()))
							.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + b.getId()))
							.create();
			for (BaseComponent s : comp)
				msg.addExtra(s);
		});
		sender.sendMessage(msg);
	}

	//	@Override
	//	public void onCommand(CommandSender sender, String[] args) {
	//		Configuration config = OlympaBungee.getInstance().getConfig();
	//		if (Matcher.isInt(args[0]))
	//			IdHistory.histban(sender, Integer.parseInt(args[0]));
	//		else if (Matcher.isFakeIP(args[0])) {
	//
	//			if (Matcher.isIP(args[0]))
	//				IpHistory.histBan(sender, args[0]);
	//			else {
	//				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.ipinvalid").replaceAll("%ip%", args[0]));
	//				return;
	//			}
	//
	//		} else if (Matcher.isFakeUUID(args[0])) {
	//
	//			if (Matcher.isUUID(args[0]))
	//				PlayerHistory.histBan(sender, null, UUID.fromString(args[0]));
	//			else {
	//				this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.uuidinvalid").replaceAll("%uuid%", args[0]));
	//				return;
	//			}
	//		} else if (Matcher.isUsername(args[0]))
	//			PlayerHistory.histBan(sender, args[0], null);
	//		else {
	//			this.sendMessage(Prefix.DEFAULT_BAD, config.getString("default.typeunknown").replaceAll("%type%", args[0]));
	//			return;
	//		}
	//
	//	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		switch (args.length) {
		case 1:
			List<String> list = Utils.startWords(args[0], AccountProvider.getSQL().getNamesBySimilarName(args[0]));
			return list;
		default:
			return new ArrayList<>();
		}
	}
}
