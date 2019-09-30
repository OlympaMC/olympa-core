package fr.tristiisch.olympa.core.ban.commands.methods;

import java.util.ArrayList;
import java.util.List;

import fr.tristiisch.emeraldmc.api.bungee.ban.BanMySQL;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBan;
import fr.tristiisch.emeraldmc.api.bungee.ban.objects.EmeraldBanHistory;
import fr.tristiisch.emeraldmc.api.commons.Prefix;
import fr.tristiisch.emeraldmc.api.commons.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class IdHistory {

	@SuppressWarnings("deprecation")
	public static void histban(final CommandSender sender, final int id) {
		final EmeraldBan ban = BanMySQL.getBanByID(id);
		if(ban == null) {
			sender.sendMessage(Utils.color(Prefix.DEFAULT_BAD + "L'id de ban n°&4" + id + "&c n'existe pas."));
			return;
		}

		final TextComponent msg = new TextComponent("");
		for(final BaseComponent s : ban.toBaseComplement()) {
			msg.addExtra(s);
		}

		msg.addExtra(new TextComponent(Utils.color("\n&6Historique: ")));

		final int i = ban.getHistorys().size();
		if(i > 0) {
			final List<TextComponent> msglist = new ArrayList<>();

			for(final EmeraldBanHistory banhist : ban.getHistorys()) {
				final TextComponent msg3 = new TextComponent(banhist.getStatus().getColor() + banhist.getAuthorName());

				final BaseComponent[] showMsg = new ComponentBuilder(Utils.color("&6Auteur: &e" + banhist.getAuthorName() + "\n"))
						.append(Utils.color("&6Status: &e" + banhist.getStatus().getNameColored() + " (" + banhist.getReason() + ")\n"))
						.append(Utils.color("&6Date: &e" + Utils.timestampToDateAndHour(banhist.getTime())))
						.create();
				msg3.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, showMsg));
				msglist.add(msg3);
			}

			Utils.toTextComponent(msg, msglist, "&e,", "");
		}
		sender.sendMessage(msg);

	}

}
