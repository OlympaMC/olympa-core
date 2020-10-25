package fr.olympa.api.bungee.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.command.CommandArgument;
import fr.olympa.api.command.IOlympaCommand;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.datamanagment.DataHandler;
import fr.olympa.core.bungee.utils.BungeeUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public abstract class BungeeCommand extends Command implements IOlympaCommand, TabExecutor {

	static Map<List<String>, BungeeCommand> commandPreProcess = new HashMap<>();

	public List<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		return new ArrayList<>();
	}

	public abstract void onCommand(CommandSender sender, String[] args);

	protected String[] aliases;
	public boolean allowConsole = true;
	protected String command;
	protected String description;
	protected boolean bypassAuth = false;
	protected OlympaPermission permission;
	protected LinkedHashMap<List<CommandArgument>, Boolean> args = new LinkedHashMap<>();

	/**
	 * Don't foget to set {@link BungeeCommand#usageString}
	 */
	public Integer minArg = 0;

	protected Plugin plugin;
	protected CommandSender sender;
	protected ProxiedPlayer proxiedPlayer;
	protected OlympaPlayer olympaPlayer;

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

	public BungeeCommand(Plugin plugin, String command, String description, OlympaPermission permission, String... aliases) {
		super(command, null, aliases);
		this.plugin = plugin;
		this.description = description;
		this.command = command;
		this.permission = permission;
		this.aliases = aliases;
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
				olympaPlayer = new AccountProvider(proxiedPlayer.getUniqueId()).getFromRedis();
				if (!bypassAuth && DataHandler.isUnlogged(proxiedPlayer)) {
					sendError("Tu dois être connecté. Fais &4/login <mdp>&c.");
					return;
				}

				if (permission != null) {
					if (olympaPlayer == null) {
						sendImpossibleWithOlympaPlayer();
						return;
					}
					if (!permission.hasPermission(olympaPlayer)) {
						sendDoNotHavePermission();
						return;
					}
				}
			} else {
				proxiedPlayer = null;
				olympaPlayer = null;
				if (!allowConsole) {
					sendImpossibleWithConsole();
					return;
				}
			}

			if (args.length < minArg) {
				sendUsage();
				return;
			}
			onCommand(sender, args);
		});

	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		this.sender = sender;
		if (sender instanceof ProxiedPlayer) {
			proxiedPlayer = (ProxiedPlayer) sender;
			if (!this.hasPermission())
				return new ArrayList<>();
		} else {
			proxiedPlayer = null;
			if (!allowConsole) {
				sendImpossibleWithConsole();
				return new ArrayList<>();
			}
		}
		List<String> customResponse = onTabComplete(this.sender, this, args);
		if (customResponse != null)
			return customResponse;
		Set<List<CommandArgument>> defaultArgs = this.args.keySet();
		if (defaultArgs.isEmpty())
			return new ArrayList<>();
		Iterator<List<CommandArgument>> iterator = defaultArgs.iterator();
		List<CommandArgument> cas = null;
		List<String> potentialArgs = new ArrayList<>();
		int i = 0;
		while (iterator.hasNext() && args.length > i) {
			cas = iterator.next();
			i++;
		}
		if (args.length != i || cas == null)
			return new ArrayList<>();
		for (CommandArgument ca : cas) {
			if (ca.getPermission() != null && !ca.getPermission().hasPermission(getOlympaPlayer()) || !ca.hasRequireArg(args, i))
				continue;
			switch (ca.getArgName().toUpperCase()) {
			case "CONFIGS":
				potentialArgs.addAll(BungeeCustomConfig.getConfigs().stream().map(BungeeCustomConfig::getName).collect(Collectors.toList()));
				break;
			case "JOUEUR":
				potentialArgs.addAll(ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()));
				break;
			case "TIME":
				potentialArgs.addAll(Arrays.asList("1h", "2h", "4h", "6h", "12h", "1j", "2j", "3j", "1semaine", "2semaines", "1mois", "1an"));
				break;
			default:
				potentialArgs.add(ca.getArgName());
				break;
			}
		}
		return Utils.startWords(args[i - 1], potentialArgs);
	}

	public String getCommand() {
		return command;
	}

	@Override
	public OlympaPlayer getOlympaPlayer() {
		return olympaPlayer;
	}

	@Override
	public OlympaPermission getOlympaPermission() {
		return permission;
	}

	public ProxiedPlayer getProxiedPlayer() {
		return proxiedPlayer;
	}

	@Override
	public BungeeCommand register() {
		build();
		plugin.getProxy().getPluginManager().registerCommand(plugin, this);
		return this;
	}

	@Override
	public BungeeCommand registerPreProcess() {
		build();
		List<String> commands = new ArrayList<>();
		commands.add(command);
		commands.addAll(Arrays.asList(aliases));
		commandPreProcess.put(commands, this);
		return this;
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
		sender.sendMessage(text);
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
		for (CommandSender s : senders)
			sendMessage(s, text);
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
		args.put(ca, isMandatory);
	}

	public void sendHelp(CommandSender sender) {
		sendMessage(sender, Prefix.DEFAULT, "§eCommande §6%s", command + (aliases == null || aliases.length == 0 ? "" : " §e(" + String.join(", ", aliases) + ")"));
		if (description != null)
			sendMessage(sender, Prefix.DEFAULT, "§e%s", description);
	}

	private void build() {
		usageString = args.entrySet().stream().map(entry -> {
			boolean isMandatory = entry.getValue();
			List<CommandArgument> ca = entry.getKey();
			return (isMandatory ? "<" : "[") + ca.stream().map(c -> c.getArgName()).collect(Collectors.joining("|")) + (isMandatory ? ">" : "]");
		}).collect(Collectors.joining(" "));
		if (minArg == 0)
			minArg = (int) args.entrySet().stream().count();
	}
}
