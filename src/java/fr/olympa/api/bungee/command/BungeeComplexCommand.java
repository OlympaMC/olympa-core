package fr.olympa.api.bungee.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.command.complex.ArgumentParser;
import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.IComplexCommand;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeComplexCommand extends BungeeCommand implements IComplexCommand {

	public class BungeeInternalCommand {
		public Cmd cmd;
		public OlympaBungeePermission perm;
		public Method method;
		public Object commands;
		public String name;

		BungeeInternalCommand(Cmd cmd, Method method, Object commandsClass) {
			this.cmd = cmd;
			this.method = method;
			commands = commandsClass;
			name = method.getName();
			String permName = cmd.permissionName();
			if (!permName.isBlank()) {
				perm = (OlympaBungeePermission) OlympaPermission.permissions.get(cmd.permissionName());
				if (perm == null) {
					LinkSpigotBungee.Provider.link.sendMessage("&cBungeeComplexCommand &4%s&c > &cpermission &4%s&c introuvable, la permission est mise à &4OlympaGroup.FONDA&c.", name, cmd.permissionName());
					perm = new OlympaBungeePermission(OlympaGroup.FONDA);
				}
			}
		}

		boolean canRun() {
			return hasPermission(perm) && (!cmd.player() || !isConsole());
		}
	}

	public class BungeeArgumentParser extends ArgumentParser {

		public Function<CommandSender, Collection<String>> tabArgumentsFunction;

		/**
		 *
		 * @param tabArgumentsFunction
		 * @param supplyArgumentFunction
		 * @param wrongArgTypeMessageFunction Le message ne doit pas finir par un point, et doit avoir un sens en utiliser le message suivie d'un ou (ex: Ton message d'erreur OU un autre message d'erreur)
		 */
		public BungeeArgumentParser(Function<CommandSender, Collection<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction, Function<String, String> wrongArgTypeMessageFunction) {
			super(supplyArgumentFunction, wrongArgTypeMessageFunction);
			this.tabArgumentsFunction = tabArgumentsFunction;
		}

	}

	protected static final HoverEvent COMMAND_HOVER = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacyText("§bSuggérer la commande.")));
	protected static final List<String> INTEGERS = Arrays.asList("1", "2", "3");
	private static final String TEMP_UUID = UUID.randomUUID().toString();
	protected static final List<String> UUIDS = Arrays.asList(TEMP_UUID, TEMP_UUID.replace("-", ""));
	protected static final List<String> IP = Arrays.asList("127.0.0.1");
	protected static final List<String> BOOLEAN = Arrays.asList("true", "false");
	protected static final List<String> HEX_COLOR = Arrays.asList("#123456", "#FFFFFF");
	public final Map<List<String>, BungeeInternalCommand> commands = new HashMap<>();
	private final Map<String, BungeeArgumentParser> parsers = new HashMap<>();

	public BungeeComplexCommand(Plugin plugin, String command, String description, OlympaBungeePermission permission, String... aliases) {
		super(plugin, command, description, permission, aliases);
		addArgumentParser("PLAYERS", sender -> plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()), x -> {
			return plugin.getProxy().getPlayer(x);
		}, x -> String.format("Le joueur &4%s&c est introuvable", x));
		addArgumentParser("INTEGER", sender -> INTEGERS, x -> {
			if (RegexMatcher.INT.is(x))
				return RegexMatcher.INT.parse(x);
			return null;
		}, x -> String.format("&4%s&c doit être un nombre entier", x));
		addArgumentParser("UUID", sender -> UUIDS, x -> {
			if (RegexMatcher.UUID.is(x))
				return RegexMatcher.UUID.parse(x);
			return null;
		}, x -> {
			String random = UUID.randomUUID().toString();
			return String.format("&4%s&c doit être un uuid sous la forme &4%s&c ou &4%s&c", x, random, random.replace("-", ""));
		});
		addArgumentParser("DOUBLE", sender -> Collections.emptyList(), x -> {
			if (RegexMatcher.DOUBLE.is(x))
				return RegexMatcher.DOUBLE.parse(x);
			return null;
		}, x -> String.format("&4%s&c doit être un nombre décimal", x));
		addArgumentParser("HEX_COLOR", sender -> HEX_COLOR, x -> {
			if (RegexMatcher.HEX_COLOR.is(x))
				return RegexMatcher.HEX_COLOR.parse(x);
			return null;
		}, x -> String.format("&4%s&c n'est pas un code hexadicimal sous la forme &4%s&c ou &4%s&c.", x, HEX_COLOR.get(0), HEX_COLOR.get(1)));
		addArgumentParser("IP", sender -> IP, x -> {
			if (RegexMatcher.IP.is(x))
				return RegexMatcher.IP.parse(x);
			return null;
		}, x -> String.format("&4%s&c n'est pas une IPv4 sous la forme &4%s&c.", x, IP));
		addArgumentParser("BOOLEAN", sender -> BOOLEAN, Boolean::parseBoolean, null);
		addArgumentParser("SUBCOMMAND", sender -> commands.entrySet().stream().filter(e -> !e.getValue().cmd.otherArg()).map(Entry::getKey).flatMap(List::stream).collect(Collectors.toList()), x -> {
			BungeeInternalCommand result = getCommand(x);
			if (result != null && result.cmd.otherArg())
				return null;
			return result;
		}, x -> String.format("La commande &4%s&c n'existe pas", x));
		addArgumentParser("CONFIGS", sender -> BungeeCustomConfig.getConfigs().stream().map(BungeeCustomConfig::getName).collect(Collectors.toList()), x -> {
			return BungeeCustomConfig.getConfig(x);
		}, x -> String.format("La config &4%s&c n'existe pas", x));
		registerCommandsClass(this);
	}

	public BungeeInternalCommand getCommand(String argName) {
		return commands.entrySet().stream().filter(entry -> entry.getKey().contains(argName.toLowerCase())).findFirst().map(entry -> entry.getValue())
				.orElse(commands.entrySet().stream().filter(entry -> entry.getValue().cmd.otherArg()).map(entry -> entry.getValue()).findFirst().orElse(null));
	}

	@Override
	public boolean containsCommand(String argName) {
		return commands.entrySet().stream().anyMatch(entry -> entry.getKey().contains(argName.toLowerCase()) || entry.getValue().cmd.otherArg());
	}

	@Override
	public <T extends Enum<T>> void addArgumentParser(String name, Class<T> enumClass) {
		List<String> values = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
		addArgumentParser(name, sender -> values, playerInput -> {
			for (T each : enumClass.getEnumConstants())
				if (each.name().equalsIgnoreCase(playerInput))
					return each;
			return null;
		}, x -> String.format("La valeur %s n'existe pas.", x));
	}

	public void addArgumentParser(String name, Function<CommandSender, Collection<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction, Function<String, String> errorMessageArgumentFunction) {
		parsers.put(name, new BungeeArgumentParser(tabArgumentsFunction, supplyArgumentFunction, errorMessageArgumentFunction));
	}

	public boolean noArguments(CommandSender sender) {
		return false;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (args.length == 0) {
			if (!noArguments(sender))
				sendError("Syntaxe incorrecte. Essaye &4/%s help&c.", command);
			return;
		}

		BungeeInternalCommand internal = getCommand(args[0]);
		if (internal == null) {
			sendError("La commande n'existe pas.");
			return;
		}

		Cmd cmd = internal.cmd;
		if (cmd.player() && !(sender instanceof ProxiedPlayer)) {
			sendImpossibleWithConsole();
			return;
		}
		if (!isConsole() && !hasPermission(internal.perm)) {
			sendDoNotHavePermission();
			return;
		}

		int minArg = cmd.min();
		if (cmd.otherArg())
			minArg--;
		if (args.length - 1 < minArg) {
			if ("".equals(cmd.syntax()))
				this.sendIncorrectSyntax();
			this.sendIncorrectSyntax("/" + command + " " + (!cmd.otherArg() ? internal.method.getName() : "") + " " + cmd.syntax());
			return;
		}

		int i1 = 1;
		if (cmd.otherArg())
			i1 = 0;
		Object[] argsCmd = new Object[args.length - i1];
		for (int i2 = 0; i2 < argsCmd.length; i2++) {
			String arg = args[i1++];
			String[] types = (i2 >= cmd.args().length ? "" : cmd.args()[i2]).split("\\|");
			Object result = null;
			List<BungeeArgumentParser> potentialParsers = parsers.entrySet().stream().filter(entry -> Arrays.stream(types).anyMatch(type -> entry.getKey().equals(type)))
					.map(Entry::getValue).collect(Collectors.toList());
			boolean hasStringType = potentialParsers.size() != types.length;
			if (potentialParsers.isEmpty())
				result = arg;
			else {
				BungeeArgumentParser parser = potentialParsers.stream().filter(p -> p.tabArgumentsFunction.apply(sender).contains(arg)).findFirst().orElse(null);
				if (parser != null)
					result = parser.supplyArgumentFunction.apply(arg);
				else
					// TODO : Choose between 2 parses here
					for (BungeeArgumentParser p : potentialParsers) {
						result = p.supplyArgumentFunction.apply(arg);
						if (result != null)
							break;
					}
				if (result == null && !hasStringType) {
					if ("".equals(cmd.syntax()) && potentialParsers.isEmpty()) {
						this.sendIncorrectSyntax();
						return;
					} else if (!potentialParsers.isEmpty()) {
						sendError("%s.", potentialParsers.stream().filter(e -> e.wrongArgTypeMessageFunction != null)
								.map(e -> e.wrongArgTypeMessageFunction.apply(arg).replaceFirst("\\.$", ""))
								.collect(Collectors.joining(" &4&lou&c ")));
						if (potentialParsers.size() <= 1)
							return;
					}
					this.sendIncorrectSyntax("/" + command + " " + (!cmd.otherArg() ? internal.method.getName() : "") + " " + cmd.syntax());
					return;
				}
			}

			argsCmd[i2] = result;
		}

		try {
			internal.method.invoke(internal.commands, new CommandContext(this, argsCmd, command));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sendError("Une erreur est survenue.");
			e.printStackTrace();
		}
		return;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		List<String> tmp = new ArrayList<>();
		List<String> find = new ArrayList<>();
		String sel = args[0];
		if (args.length == 1) {
			for (Entry<List<String>, BungeeInternalCommand> en : commands.entrySet())
				if (en.getValue().cmd.otherArg())
					find.addAll(findPotentialArgs(args));
				else if (!en.getValue().cmd.hide() || en.getValue().canRun())
					find.add(en.getKey().get(0));
		} else if (args.length >= 2) {
			find = findPotentialArgs(args);
			sel = args[args.length - 1];
		} else
			return tmp;
		if (sel.isBlank())
			return find;
		for (String arg : find)
			if (arg.toLowerCase().startsWith(sel.toLowerCase()))
				tmp.add(arg);
		return tmp;
	}

	private List<String> findPotentialArgs(String[] args) {
		List<String> find = new ArrayList<>();
		int index = args.length - 2;
		String sel = args[0];
		if (!containsCommand(sel))
			return find;
		BungeeInternalCommand internal = getCommand(sel);
		String[] needed = internal.cmd.args();
		if (internal.cmd.otherArg())
			index++;
		if (args.length == 1 || needed.length <= index || !internal.cmd.permissionName().isBlank() && !internal.perm.hasSenderPermissionBungee(sender))
			return find;
		String[] types = needed[index].split("\\|");
		for (String type : types) {
			BungeeArgumentParser parser = parsers.get(type);
			if (parser != null)
				find.addAll(parser.tabArgumentsFunction.apply(sender));
			else
				find.add(type);
		}
		return find;
	}

	/**
	 * Register all available commands from an instance of a Class
	 * @param commandsClassInstance Instance of the Class
	 */
	@Override
	public void registerCommandsClass(Object commandsClassInstance) {
		Class<?> clazz = commandsClassInstance.getClass();
		do
			registerCommandsClass(clazz, commandsClassInstance);
		while ((clazz = clazz.getSuperclass()) != null);
	}

	private void registerCommandsClass(Class<?> clazz, Object commandsClassInstance) {
		for (Method method : clazz.getDeclaredMethods())
			if (method.isAnnotationPresent(Cmd.class)) {
				Cmd cmd = method.getDeclaredAnnotation(Cmd.class);
				if (method.getParameterCount() == 1)
					if (method.getParameterTypes()[0] == CommandContext.class) {
						List<String> argNames = new ArrayList<>();
						argNames.add(method.getName().toLowerCase());
						if (cmd.aliases() != null)
							argNames.addAll(Arrays.asList(cmd.aliases()));
						commands.put(argNames, new BungeeInternalCommand(cmd, method, commandsClassInstance));
						continue;
					}
				OlympaCore.getInstance()
						.sendMessage("Error when loading command annotated method " + method.getName() + " in class " + method.getDeclaringClass().getName() + ". Required argument: fr.olympa.api.command.complex.CommandContext");
			}
	}

	@Override
	public void sendHelp(CommandSender sender) {
		super.sendHelp(sender);
		for (BungeeInternalCommand command : commands.values()) {
			if (!command.canRun())
				continue;
			sender.sendMessage(getHelpCommandComponent(command));
		}
	}

	@Override
	@Cmd(args = "SUBCOMMAND", syntax = "[commande]")
	public void help(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0)
			sendHelp(sender);
		else {
			if (!(cmd.getArgument(0) instanceof BungeeInternalCommand)) {
				sendIncorrectSyntax();
				return;
			}
			BungeeInternalCommand command = cmd.getArgument(0);
			if (!command.canRun()) {
				sendIncorrectSyntax();
				return;
			}
			sender.sendMessage(getHelpCommandComponent(command));
		}
	}

	private TextComponent getHelpCommandComponent(BungeeInternalCommand command) {
		String fullCommand;
		if (!command.cmd.otherArg())
			fullCommand = "/" + this.command + " " + command.name;
		else
			fullCommand = "/" + this.command;
		TextComponent component = new TextComponent();
		component.setHoverEvent(COMMAND_HOVER);
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, fullCommand + " "));

		for (BaseComponent commandCompo : TextComponent.fromLegacyText("§7➤ §6" + fullCommand + " §e" + command.cmd.syntax()))
			component.addExtra(commandCompo);

		return component;
	}

}