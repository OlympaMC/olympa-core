package fr.olympa.core.bungee.login.listener;

import java.util.concurrent.TimeUnit;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.bungee.login.HandlerLogin;
import fr.olympa.core.bungee.login.events.OlympaPlayerLoginEvent;
import fr.olympa.core.bungee.servers.ServersConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LoginChatListener implements Listener {
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChat(ChatEvent event) {
		if (!(event.getSender() instanceof ProxiedPlayer)) {
			return;
		}
		String[] args = event.getMessage().split(" ");
		String command = args[0].substring(1);
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		if (!HandlerLogin.isLogged(player) && (!event.getMessage().startsWith("/") || !HandlerLogin.command.contains(command))) {
			OlympaPlayer olympaPlayer = new AccountProvider(player.getUniqueId()).getFromRedis();
			if (olympaPlayer == null || olympaPlayer.getPassword() == null || olympaPlayer.getPassword().isEmpty()) {
				player.sendMessage(Prefix.DEFAULT_BAD + SpigotUtils.color("Tu dois t'enregistrer. Fait &4/register <mdp>&c."));
			} else {
				player.sendMessage(Prefix.DEFAULT_BAD + SpigotUtils.color("Tu dois être connecté. Fait &4/login <mdp>&c."));
			}
			event.setCancelled(true);
		}
	}


	@EventHandler
	public void onPostLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProvider.get(player.getUniqueId());
		TextComponent textComponent = new TextComponent();
		TextComponent textComponent2 = new TextComponent("----------------");
		TextComponent textComponent3;
		
		textComponent2.setColor(ChatColor.DARK_RED);
		textComponent2.setBold(true);
		textComponent2.setStrikethrough(true);
		textComponent.addExtra(textComponent2);
		
		textComponent2 = new TextComponent(" [");
		textComponent2.setColor(ChatColor.GRAY);
		textComponent.addExtra(textComponent2);
		
		textComponent2 = new TextComponent("Login");
		textComponent2.setColor(ChatColor.RED);
		textComponent.addExtra(textComponent2);
		
		textComponent2 = new TextComponent("] ");
		textComponent2.setColor(ChatColor.GRAY);
		textComponent.addExtra(textComponent2);
		
		textComponent2 = new TextComponent("----------------\n");
		textComponent2.setColor(ChatColor.DARK_RED);
		textComponent2.setBold(true);
		textComponent2.setStrikethrough(true);
		textComponent.addExtra(textComponent2);

		textComponent2 = new TextComponent("Bienvenue sur ");
		textComponent2.setColor(ChatColor.AQUA);
		textComponent.addExtra(textComponent2);
		
		textComponent2 = new TextComponent("Olympa");
		textComponent2.setColor(ChatColor.GOLD);
		textComponent2.setBold(true);
		textComponent.addExtra(textComponent2);
		
		textComponent2 = new TextComponent(".\n");
		textComponent2.setColor(ChatColor.AQUA);
		textComponent.addExtra(textComponent2);
		if (!olympaPlayer.isPremium()) {
			
			if (olympaPlayer.getPassword() != null) {
				
				textComponent2 = new TextComponent("Afin de sécuriser la connexion, tu dois enter un mot de passe. Il sera le même sur le site et sur le forum.\n");
				textComponent2.setColor(ChatColor.AQUA);
				textComponent.addExtra(textComponent2);
				
				textComponent2 = new TextComponent("Fait ");
				textComponent2.setColor(ChatColor.GREEN);
				textComponent.addExtra(textComponent2);
				
				textComponent3 = new TextComponent("/register <mot de passe>");
				textComponent3.setColor(ChatColor.DARK_GREEN);
				textComponent2.addExtra(textComponent3);

				textComponent3 = new TextComponent(" ou ");
				textComponent3.setColor(ChatColor.GREEN);
				textComponent2.addExtra(textComponent3);
				
				textComponent3 = new TextComponent("clique-ici");
				textComponent3.setColor(ChatColor.DARK_GREEN);
				textComponent2.addExtra(textComponent3);
				
				textComponent3 = new TextComponent("et écrit ton mot de passe.\n");
				textComponent3.setColor(ChatColor.GREEN);
				textComponent2.addExtra(textComponent3);
				
				textComponent2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Clique pour avoir directement la commande").color(ChatColor.YELLOW).create()));
				textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/register "));
				textComponent.addExtra(textComponent2);
			} else {
				textComponent2 = new TextComponent("Ton mot de passe est le même sur le site et le forum\n");
				textComponent2.setColor(ChatColor.AQUA);
				textComponent.addExtra(textComponent2);
				
				textComponent2 = new TextComponent("Fait ");
				textComponent2.setColor(ChatColor.GREEN);
				textComponent.addExtra(textComponent2);
				
				textComponent3 = new TextComponent("/login <mot de passe>");
				textComponent3.setColor(ChatColor.DARK_GREEN);
				textComponent2.addExtra(textComponent3);

				textComponent3 = new TextComponent(" ou ");
				textComponent3.setColor(ChatColor.GREEN);
				textComponent2.addExtra(textComponent3);
				
				textComponent3 = new TextComponent("clique-ici");
				textComponent3.setColor(ChatColor.DARK_GREEN);
				textComponent2.addExtra(textComponent3);
				
				textComponent3 = new TextComponent("et écrit ton mot de passe.\n");
				textComponent3.setColor(ChatColor.GREEN);
				textComponent2.addExtra(textComponent3);
				
				textComponent2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Clique pour avoir directement la commande").color(ChatColor.YELLOW).create()));
				textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/login "));
				textComponent.addExtra(textComponent2);
			}
		} else {
			textComponent2 = new TextComponent("En tant que compte premium, tu n'a pas besoin de mot de passe. Cependant, pour te connecter au site et forum, tu dois enregister un ");
			textComponent2.setColor(ChatColor.GREEN);
			textComponent.addExtra(textComponent2);
			
			textComponent2 = new TextComponent("mot de passe ");
			textComponent2.setColor(ChatColor.DARK_GREEN);
			textComponent.addExtra(textComponent2);
			
			textComponent2 = new TextComponent(". Pour cela, tu dois faire une seul fois ");
			textComponent2.setColor(ChatColor.GREEN);
			textComponent.addExtra(textComponent2);
			
			textComponent3 = new TextComponent("/register <mot de passe>");
			textComponent3.setColor(ChatColor.DARK_GREEN);
			textComponent2.addExtra(textComponent3);

			textComponent3 = new TextComponent(" ou ");
			textComponent3.setColor(ChatColor.GREEN);
			textComponent2.addExtra(textComponent3);
			
			textComponent3 = new TextComponent("clique-ici");
			textComponent3.setColor(ChatColor.DARK_GREEN);
			textComponent2.addExtra(textComponent3);
			
			textComponent3 = new TextComponent("et écrit ton mot de passe.\n");
			textComponent3.setColor(ChatColor.GREEN);
			textComponent2.addExtra(textComponent3);
			
			textComponent2.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Clique pour avoir directement la commande").color(ChatColor.YELLOW).create()));
			textComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/register "));
			textComponent.addExtra(textComponent2);
		}

		textComponent3 = new TextComponent("\n\n");

		textComponent2 = new TextComponent("Ajoute ton email: ");
		textComponent2.setColor(ChatColor.GREEN);
		textComponent.addExtra(textComponent2);

		textComponent2 = new TextComponent("/email\n");
		textComponent2.setColor(ChatColor.DARK_GREEN);
		textComponent.addExtra(textComponent2);

		textComponent3 = new TextComponent("\n\n");
		textComponent2.addExtra(textComponent3);
		
		textComponent2 = new TextComponent("---------------------------------------");
		textComponent2.setColor(ChatColor.DARK_RED);
		textComponent2.setBold(true);
		textComponent2.setStrikethrough(true);
		textComponent.addExtra(textComponent2);
		
		player.sendMessage(textComponent);
	}

}
