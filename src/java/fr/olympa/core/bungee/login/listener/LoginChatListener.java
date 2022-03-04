package fr.olympa.core.bungee.login.listener;

import fr.olympa.api.bungee.player.DataHandler;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.login.HandlerLogin;
import fr.olympa.core.common.provider.AccountProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * TODO add message for information, /discord link, /changepassword ...
 *
 */
public class LoginChatListener implements Listener {

	private TextComponent joinMessageCrackNew;
	private TextComponent joinMessageCrackCreated;
	private TextComponent joinMessagePremiumNew;
	private TextComponent joinMessagePremiumCreated;

	public LoginChatListener() {
		HoverEvent hoverTooltip = new HoverEvent(Action.SHOW_TEXT, new Text(new ComponentBuilder("Clique pour avoir directement la commande").color(ChatColor.YELLOW).create()));

		TextComponent base = new TextComponent();
		addLegacyText(base, "§4§l§m----------------§7 [§cLogin§7] §4§l§m----------------\n"
				+ "§bBienvenue sur §6§lOlympa§b§r.\n\n");
		TextComponent end = new TextComponent("\n\n--------------------------------------");
		end.setColor(ChatColor.DARK_RED);
		end.setBold(true);
		end.setStrikethrough(true);

		joinMessageCrackNew = base.duplicate();
		TextComponent link = new TextComponent();
		addLegacyText(link, "§aCrée ton compte avec §2/register <mot de passe>§a <- Clique.");
		link.setHoverEvent(hoverTooltip);
		link.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/register "));
		joinMessageCrackNew.addExtra(link);
		joinMessageCrackNew.addExtra(end);

		joinMessageCrackCreated = base.duplicate();
		link = new TextComponent();
		addLegacyText(link, "§aTon mot de passe est le même sur le site et le forum.\n§aFais §2/login <mot de passe>§a.");
		link.setHoverEvent(hoverTooltip);
		link.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/login "));
		joinMessageCrackCreated.addExtra(link);
		joinMessageCrackCreated.addExtra(end);

		joinMessagePremiumCreated = base.duplicate();
		joinMessagePremiumCreated.addExtra(end);
	}

	private void addLegacyText(TextComponent component, String legacyText) {
		for (BaseComponent compo : TextComponent.fromLegacyText(legacyText))
			component.addExtra(compo);
	}

	@EventHandler
	public void onChat(TabCompleteEvent event) {
		if (!(event.getReceiver() instanceof ProxiedPlayer))
			return;
		ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
		if (DataHandler.isUnlogged(player))
			event.setCancelled(true);
	}

	@EventHandler
	public void onChat(ChatEvent event) {
		if (!(event.getSender() instanceof ProxiedPlayer))
			return;
		String[] args = event.getMessage().split(" ");
		String command = args[0].substring(1);
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		if (DataHandler.isUnlogged(player) && (!event.isCommand() || !HandlerLogin.command.contains(command))) {
			OlympaPlayer olympaPlayer = new AccountProvider(player.getUniqueId()).getFromRedis();
			if ((olympaPlayer == null || olympaPlayer.getPassword() == null || olympaPlayer.getPassword().isEmpty()) && !olympaPlayer.isPremium())
				player.sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Tu dois t'enregistrer. Fais &4/register <mdp>&c."));
			else
				player.sendMessage(Prefix.DEFAULT_BAD.formatMessageB("Tu dois être connecté%s. Fais &4/login <mdp>&c.", olympaPlayer.getGender().getTurne()));
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = DataHandler.get(player.getName()).getOlympaPlayer();
		boolean hasPassword = olympaPlayer.getPassword() != null;
		TextComponent message = hasPassword ? olympaPlayer.isPremium() ? joinMessagePremiumCreated : joinMessageCrackCreated : olympaPlayer.isPremium() ? joinMessagePremiumNew : joinMessageCrackNew;
		player.sendMessage(message);
	}

}
