package fr.olympa.core.bungee.ban.commands.methods;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.ban.BanMySQL;
import fr.olympa.core.bungee.ban.objects.OlympaSanction;
import fr.olympa.core.bungee.ban.objects.OlympaSanctionHistory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class IdHistory {

	@SuppressWarnings("deprecation")
	public static void histban(CommandSender sender, int id) {
		OlympaSanction ban = BanMySQL.getSanction(id);
		if (ban == null) {
			sender.sendMessage(SpigotUtils.color(Prefix.DEFAULT_BAD + "L'id de ban n°&4" + id + "&c n'existe pas."));
			return;
		}

		TextComponent msg = new TextComponent("");
		for (BaseComponent s : ban.toBaseComplement()) {
			msg.addExtra(s);
		}

		msg.addExtra(new TextComponent(SpigotUtils.color("\n&6Historique: ")));

		int i = ban.getHistorys().size();
		if (i > 0) {
			List<TextComponent> msglist = new ArrayList<>();

			for (OlympaSanctionHistory banhist : ban.getHistorys()) {
				TextComponent msg3 = new TextComponent(banhist.getStatus().getColor() + banhist.getAuthorName());

				BaseComponent[] showMsg = new ComponentBuilder(SpigotUtils.color("&6Auteur: &e" + banhist.getAuthorName() + "\n"))
						.append(SpigotUtils.color("&6Status: &e" + banhist.getStatus().getNameColored() + " (" + banhist.getReason() + ")\n"))
						.append(SpigotUtils.color("&6Date: &e" + Utils.timestampToDateAndHour(banhist.getTime())))
						.create();
				msg3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, showMsg));
				msglist.add(msg3);
			}

			Utils.toTextComponent(msg, msglist, "&e,", "");
		}
		sender.sendMessage(msg);

	}

}
