package fr.olympa.core.bungee.api.command;

import java.sql.SQLException;
import java.util.Arrays;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.datamanagment.DataHandler;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public abstract class BungeeCommand extends Command {

	protected String[] aliases;
	public boolean allowConsole = true;
	protected String command;
	protected String description;
	protected boolean bypassAuth = false;
	public OlympaPlayer olympaPlayer;
	protected OlympaPermission permission;

	/**
	 * Don't foget to set {@link BungeeCommand#usageString}
	 */
	public Integer minArg = 0;

	protected Plugin plugin;
	public ProxiedPlayer proxiedPlayer;
	private CommandSender sender;

	/**
	 * Format: Usage » %command% <%obligatory%|%obligatory%> [%optional%] Variable
	 * name: 'joueur' ...
	 *
	 */
	public String usageString;

	public BungeeCommand(Plugin plugin, String command) {
		super(command);
		this.plugin = plugin;
		this.command = command;
	}

	public BungeeCommand(Plugin plugin, String command, OlympaPermission permission, String... aliases) {
		super(command, null, aliases);
		this.plugin = plugin;
		this.command = command;
		this.permission = permission;
		this.aliases = aliases;
	}

	public BungeeCommand(Plugin plugin, String command, OlympaPermission permission, String[] aliases, String description, String usageString, boolean allowConsole,
			Integer minArg) {

		super(command, null, aliases);
		this.plugin = plugin;
		this.command = command;
		this.permission = permission;
		this.aliases = aliases;
		this.description = description;
		this.usageString = usageString;
		this.allowConsole = allowConsole;
		this.minArg = minArg;
		register();
	}

	public BungeeCommand(Plugin plugin, String command, String... aliases) {
		super(command, null, aliases);
		this.plugin = plugin;
		this.command = command;
		this.aliases = aliases;
	}

	public String buildText(int min, String[] args) {
		return String.join(" ", Arrays.copyOfRange(args, min, args.length));
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
			this.sender = sender;
			if (sender instanceof ProxiedPlayer) {
				proxiedPlayer = (ProxiedPlayer) sender;
				if (!bypassAuth && DataHandler.isUnlogged(proxiedPlayer)) {
					sendErreur("Tu dois être connecté. Fait &4/login <mdp>&c.");
					return;
				}

				if (permission != null) {
					olympaPlayer = getOlympaPlayer();
					if (olympaPlayer == null) {
						sendImpossibleWithOlympaPlayer();
						return;
					}
					if (!olympaPlayer.hasPermission(permission)) {
						sendDoNotHavePermission();
						return;
					}
				}
			} else if (!allowConsole) {
				sendImpossibleWithConsole();
				return;
			}

			if (args.length < minArg) {
				sendUsage();
				return;
			}
			onCommand(sender, args);
		});

	}

	public String getCommand() {
		return command;
	}

	protected OlympaPlayer getOlympaPlayer() {
		if (proxiedPlayer != null) {
			try {
				return new AccountProvider(proxiedPlayer.getUniqueId()).get();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public OlympaPermission getPerm() {
		return permission;
	}

	public ProxiedPlayer getProxiedPlayer() {
		return proxiedPlayer;
	}

	public abstract void onCommand(CommandSender sender, String[] args);

	public void register() {
		plugin.getProxy().getPluginManager().registerCommand(plugin, this);
	}

	public void sendDoNotHavePermission() {
		sendErreur("Tu as pas la permission &l(◑_◑)");
	}

	public void sendErreur(String message) {
		this.sendMessage(Prefix.DEFAULT_BAD, message);
	}

	public void sendImpossibleWithConsole() {
		sendErreur("Impossible avec la console.");
	}

	public void sendImpossibleWithOlympaPlayer() {
		sendErreur("Une erreur est survenu avec tes donnés.");
	}

	public void sendMessage(CommandSender sender, Prefix prefix, String text) {
		this.sendMessage(sender, prefix + BungeeUtils.color(text));
	}

	@SuppressWarnings("deprecation")
	public void sendMessage(CommandSender sender, String text) {
		sender.sendMessage(BungeeUtils.color(text));
	}

	public void sendMessage(Prefix prefix, String text) {
		this.sendMessage(sender, prefix, text);
	}

	public void sendMessage(String text) {
		this.sendMessage(sender, BungeeUtils.color(text));
	}

	public void sendMessage(TextComponent text) {
		proxiedPlayer.sendMessage(text);
	}

	public void sendUnknownPlayer(String name) {
		sendErreur("Le joueur &4" + name + "&c est introuvable.");
		// TODO check historique player
	}

	public void sendUsage() {
		this.sendMessage(Prefix.USAGE, "/" + command + " " + usageString);
	}
}
