package fr.olympa.core.bungee.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.olympa.api.command.CommandArgument;
import fr.olympa.api.command.IOlympaCommand;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.datamanagment.DataHandler;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public abstract class BungeeCommand extends Command implements IOlympaCommand {

	static Map<List<String>, BungeeCommand> commandPreProcess = new HashMap<>();

	protected String[] aliases;
	public boolean allowConsole = true;
	protected String command;
	protected String description;
	protected boolean bypassAuth = false;
	public OlympaPlayer olympaPlayer;
	protected OlympaPermission permission;
	protected LinkedHashMap<Boolean, List<CommandArgument>> args = new LinkedHashMap<>();

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

	public BungeeCommand(Plugin plugin, String command, OlympaPermission permission, String[] aliases, String description, String usageString, boolean allowConsole, Integer minArg) {
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

	@Override
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
					sendError("Tu dois être connecté. Fait &4/login <mdp>&c.");
					return;
				}

				if (permission != null) {
					olympaPlayer = getOlympaPlayer();
					if (olympaPlayer == null) {
						sendImpossibleWithOlympaPlayer();
						return;
					}
					if (!permission.hasPermission(olympaPlayer)) {
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

	@SuppressWarnings("unchecked")
	@Override
	public OlympaPlayer getOlympaPlayer() {
		if (proxiedPlayer == null)
			return null;
		return olympaPlayer == null ? olympaPlayer = new AccountProvider(proxiedPlayer.getUniqueId()).getFromRedis() : olympaPlayer;
	}

	public OlympaPermission getPerm() {
		return permission;
	}

	public ProxiedPlayer getProxiedPlayer() {
		return proxiedPlayer;
	}

	public abstract void onCommand(CommandSender sender, String[] args);

	@Override
	public void register() {
		build();
		plugin.getProxy().getPluginManager().registerCommand(plugin, this);
	}

	@Override
	public void registerPreProcess() {
		build();
		List<String> commands = new ArrayList<>();
		commands.add(command);
		commands.addAll(Arrays.asList(aliases));
		commandPreProcess.put(commands, this);
	}

	@Override
	public void sendDoNotHavePermission() {
		sendError("Tu as pas la permission &l(◑_◑)");
	}

	public void sendError(String message) {
		this.sendMessage(Prefix.DEFAULT_BAD, message);
	}

	@Override
	public void sendImpossibleWithConsole() {
		sendError("Impossible avec la console.");
	}

	@Override
	public void sendImpossibleWithOlympaPlayer() {
		sendError("Une erreur est survenu avec tes donnés.");
	}

	public static void sendMessage(CommandSender sender, Prefix prefix, String text) {
		sendMessage(sender, prefix + BungeeUtils.color(text));
	}

	public static void sendMessage(CommandSender sender, String text) {
		sender.sendMessage(TextComponent.fromLegacyText(BungeeUtils.color(text)));
	}

	public void sendMessage(Prefix prefix, String text) {
		sendMessage(sender, prefix, text);
	}

	public void sendMessage(String text) {
		sendMessage(sender, BungeeUtils.color(text));
	}

	public void sendMessage(TextComponent text) {
		proxiedPlayer.sendMessage(text);
	}

	@Override
	public void sendUsage(String label) {
		sendMessage(Prefix.USAGE, "/%s %s", label, usageString);
	}

	@Deprecated
	public void sendUsage() {
		this.sendMessage(Prefix.USAGE, "/" + command + " " + usageString);
	}

	@Override
	public void sendMessage(Prefix prefix, String message, Object... args) {
		sender.sendMessage(TextComponent.fromLegacyText(prefix.formatMessage(message, args)));

	}

	public void sendMessage(CommandSender sender, Prefix prefix, String text, Object... args) {
		sendMessage(new HashSet<>(Arrays.asList(sender)), prefix, text, args);
	}

	public void sendMessage(Iterable<? extends CommandSender> senders, Prefix prefix, String text, Object... args) {
		text = prefix.formatMessage(text, args);
		for (CommandSender sender : senders)
			sendMessage(sender, text);
	}

	@Override
	public void broadcast(Prefix prefix, String text, Object... args) {
		sendMessage(ProxyServer.getInstance().getPlayers(), prefix, text, args);
	}

	@Override
	public void broadcastToAll(Prefix prefix, String text, Object... args) {
		sendMessage(ProxyServer.getInstance().getPlayers(), prefix, text, args);
		sendMessage(ProxyServer.getInstance().getConsole(), prefix, text, args);
	}

	@Override
	public void sendComponents(BaseComponent... components) {
		sender.sendMessage(components);
	}

	@Override
	public ProxiedPlayer getPlayer() {
		return proxiedPlayer;
	}

	@Override
	public CommandSender getSender() {
		return sender;
	}

	@Override
	public boolean isConsole() {
		return proxiedPlayer == null;
	}

	@Override
	public boolean hasPermission() {
		return hasPermission(permission);
	}

	@Override
	public void setAllowConsole(boolean allowConsole) {
		this.allowConsole = allowConsole;
	}

	@Override
	public boolean isConsoleAllowed() {
		return allowConsole;
	}

	@Override
	public void addCommandArguments(boolean isMandatory, List<CommandArgument> ca) {
		args.put(isMandatory, ca);
	}

	private void build() {
		usageString = args.entrySet().stream().map(entry -> {
			boolean isMandatory = entry.getKey();
			List<CommandArgument> ca = entry.getValue();
			return (isMandatory ? "<" : "[") + ca.stream().map(c -> c.getArgName()).collect(Collectors.joining("|")) + (isMandatory ? ">" : "]");
		}).collect(Collectors.joining(" "));
		minArg = (int) args.entrySet().stream().count();
	}
}
