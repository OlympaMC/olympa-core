package fr.olympa.core.bungee.ban.commands;

import java.sql.SQLException;
import java.util.List;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionType;
import fr.olympa.core.common.permission.list.OlympaCorePermissionsBungee;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class BanListCommand extends BungeeCommand {

	public BanListCommand(Plugin plugin) {
		super(plugin, "banlist", OlympaCorePermissionsBungee.BAN_BANLIST_COMMAND);
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		List<OlympaSanction> sanctions = null;
		try {
			sanctions = BanMySQL.getLastSanctions(10);
		} catch (SQLException e) {
			sendError(e);
			e.printStackTrace();
			return;
		}
		TxtComponentBuilder builder = new TxtComponentBuilder(Prefix.DEFAULT_GOOD, "Les %d dernières sanctions.", sanctions.size()).extraSpliterBN();
		for (OlympaSanction sanction : sanctions)
			builder.extra(new TxtComponentBuilder("%d - %s %s %s %s", sanction.getId(), sanction.getStatus().getNameColored(),
					sanction.getType().getName(!sanction.isPermanent()), sanction.getTarget(), sanction.getReason(),
					sanction.getExpires() > 0 ? Utils.timeToDuration(sanction.getBanTime()) : sanction.getType() != OlympaSanctionType.KICK ? "&cPermanant" : "")
							.onHoverText(sanction.toBaseComplement()).onClickCommand("/histban %d", sanction.getId()));
		sender.sendMessage(builder.build());
	}
}
