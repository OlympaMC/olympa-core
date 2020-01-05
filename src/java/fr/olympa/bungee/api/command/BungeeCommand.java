package fr.olympa.bungee.api.command;

import java.util.Arrays;

import fr.olympa.api.objects.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
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

	public OlympaPlayer olympaPlayer;
	protected OlympaGroup[] groups;

	/**
	 * Don't foget to set {@link BungeeCommand#usageString}
	 */
	public Integer minArg = 0;

	protected Plugin plugin;
	public ProxiedPlayer proxiedPlayer;
	private CommandSender sender;

	/**
	 * Format: Usage » %command% <%obligatory%|%obligatory%> [%optional%]
	 * Variable name: 'joueur' ...
	 *
	 */
	public String usageString;

	public BungeeCommand(Plugin plugin, String command) {
		super(command);
		this.plugin = plugin;
		this.command = command;
	}

	public BungeeCommand(Plugin plugin, String command, OlympaGroup group, String... aliases) {
		super(command, null, aliases);
		this.plugin = plugin;
		this.command = command;
		this.groups = new OlympaGroup[] { group };
		this.aliases = aliases;
	}

	public BungeeCommand(Plugin plugin, String command, OlympaGroup[] groups, String... aliases) {
		super(command, null, aliases);
		this.plugin = plugin;
		this.command = command;
		this.groups = groups;
		this.aliases = aliases;
	}

	public BungeeCommand(Plugin plugin, String command, OlympaGroup[] groups, String[] aliases, String description, String usageString, boolean allowConsole,
			Integer minArg) {

		super(command, null, aliases);
		this.plugin = plugin;
		this.command = command;
		this.groups = groups;
		this.aliases = aliases;
		this.description = description;
		this.usageString = usageString;
		this.allowConsole = allowConsole;
		this.minArg = minArg;
		this.register();
	}

	public BungeeCommand(Plugin plugin, String command, String[] aliases) {
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
		ProxyServer.getInstance().getScheduler().runAsync(this.plugin, () -> {
			this.sender = sender;
			if (sender instanceof ProxiedPlayer) {
				this.proxiedPlayer = (ProxiedPlayer) sender;

				if (this.groups != null && this.groups.length != 0) {
					this.olympaPlayer = this.getOlympaPlayer();
					if (this.olympaPlayer == null) {
						this.sendImpossibleWithEmeraldPlayer();
						return;
					}
					if (!this.hasPermission()) {
						this.sendDoNotHavePermission();
						return;
					}
				}
			} else if (!this.allowConsole) {
				this.sendImpossibleWithConsole();
				return;
			}

			if (args.length < this.minArg) {
				this.sendUsage();
				return;
			}
			this.onCommand(sender, args);

		});

	}

	public String getCommand() {
		return this.command;
	}

	public OlympaPlayer getEmeraldPlayer() {
		return this.olympaPlayer;
	}

	private OlympaPlayer getOlympaPlayer() {
		return AccountProvider.get(this.proxiedPlayer.getUniqueId());
	}

	public ProxiedPlayer getProxiedPlayer() {
		return this.proxiedPlayer;
	}

	public boolean hasPermission(OlympaGroup... groups) {
		if (this.proxiedPlayer == null) {
			return true;
		}

		if (groups.length == 0) {
			groups = this.groups;
		}

		if (this.olympaPlayer == null) {
			this.olympaPlayer = this.getOlympaPlayer();
		}

		return this.olympaPlayer.hasPower(groups);
	}

	public abstract void onCommand(CommandSender sender, String[] args);

	public void register() {
		this.plugin.getProxy().getPluginManager().registerCommand(this.plugin, this);
	}

	public void sendDoNotHavePermission() {
		this.sendErreur("Vous n'avez pas la permission &l(◑_◑)");
	}

	public void sendErreur(String message) {
		this.sendMessage(Prefix.DEFAULT_BAD, message);
	}

	public void sendImpossibleWithConsole() {
		this.sendErreur("Impossible avec la console.");
	}

	public void sendImpossibleWithEmeraldPlayer() {
		this.sendErreur("Une erreur est survenu avec vos donnés.");
	}

	public void sendMessage(CommandSender sender, Prefix prefix, String text) {
		this.sendMessage(sender, prefix + SpigotUtils.color(text));
	}

	@SuppressWarnings("deprecation")
	public void sendMessage(CommandSender sender, String text) {
		sender.sendMessage(SpigotUtils.color(text));
	}

	public void sendMessage(Prefix prefix, String text) {
		this.sendMessage(this.sender, prefix, text);
	}

	public void sendMessage(String text) {
		this.sendMessage(this.sender, SpigotUtils.color(text));
	}

	public void sendMessage(TextComponent text) {
		this.proxiedPlayer.sendMessage(text);
	}

	public void sendUnknownPlayer(String name) {
		this.sendErreur("Le joueur &4" + name + "&c est introuvable.");
		// TODO check historique player
	}

	public void sendUsage() {
		this.sendMessage(Prefix.USAGE, this.usageString);
	}
}
