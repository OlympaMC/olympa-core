package fr.olympa.core.spigot.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Sets;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.permission.OlympaSpigotPermission;
import fr.olympa.api.player.Gender;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.provider.OlympaPlayerObject;
import fr.olympa.api.server.ServerType;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

@SuppressWarnings("deprecation")
public class PermissionCommand extends ComplexCommand {

	public PermissionCommand(Plugin plugin) {
		super(plugin, "permission", "Voir et modifier les permissions jusqu'au redémarrage", OlympaCorePermissions.PERMISSION_COMMAND, "perm", "p");
		super.addArgumentParser("PERMISSION", (player, arg) -> OlympaPermission.permissions.entrySet().stream().map(Entry::getKey).collect(Collectors.toList()),
				arg -> OlympaPermission.permissions.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(arg)).findFirst().orElse(null),
				x -> String.format("La permission &4%s&c n'existe pas", x));
		super.addArgumentParser("GROUPS", OlympaGroup.class, g -> g.getName());
		//		super.addArgumentParser("GROUPS", (player, arg) -> {
		//			return Arrays.stream(OlympaGroup.values()).map(OlympaGroup::getName).collect(Collectors.toList());
		//		}, arg -> {
		//			return Arrays.stream(OlympaGroup.values()).filter(e -> e.getName().equalsIgnoreCase(arg)).findFirst().orElse(null);
		//		}, x -> String.format("Le groupe &4%s&c n'existe pas", x));
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		if (sender instanceof Player) {
			Collection<OlympaPermission> allPerms = OlympaPermission.permissions.values();
			List<String> noPermission = OlympaPermission.permissions.entrySet().stream().filter(entry -> !hasPermission(entry.getValue())).map(Entry::getKey).collect(Collectors.toList());

			sendMessage(Prefix.DEFAULT_GOOD, "Tu as %s permission%s sur %s.", allPerms.size() - noPermission.size(), noPermission.size() > 1 ? "s" : "", allPerms.size());
			if (!noPermission.isEmpty())
				sendMessage(Prefix.DEFAULT_BAD, "Il te manque l%s permission%s :\n&6%s", noPermission.size() > 1 ? "es" : "a", noPermission.size() > 1 ? "s" : "", String.join("&e, &6", noPermission));
			return true;
		}
		return false;
	}

	@Cmd()
	public void bukkit(CommandContext cmd) {
		Set<Permission> allPerms = Bukkit.getPluginManager().getPermissions();
		Set<Permission> noPermission = Sets.difference(allPerms, player.getEffectivePermissions());
		sendMessage(Prefix.DEFAULT_GOOD, "Tu as %s permission%s bukkit sur %s. %s", allPerms.size() - noPermission.size(), noPermission.size() > 1 ? "s" : "", allPerms.size(), player.isOp() ? "&a[&2OP]" : "");
		//		if (!noPermission.isEmpty())
		//			sendMessage(Prefix.DEFAULT_BAD, "Il te manque l%s permission%s :\n&6%s", noPermission.size() > 1 ? "es" : "a", noPermission.size() > 1 ? "s" : "",
		//					noPermission.stream().map(Permission::getName).collect(Collectors.joining("&e, &6")));
	}

	@Cmd(args = { "PERMISSION" })
	public void info(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0) {
			Collection<OlympaPermission> allPerms = OlympaPermission.permissions.values();
			List<String> noPermission = OlympaPermission.permissions.entrySet().stream().filter(entry -> !hasPermission(entry.getValue())).map(Entry::getKey).collect(Collectors.toList());

			sendMessage(Prefix.DEFAULT_GOOD, "Tu as %s permission%s sur %s.", allPerms.size() - noPermission.size(), noPermission.size() > 1 ? "s" : "", allPerms.size());
			if (!noPermission.isEmpty())
				sendMessage(Prefix.DEFAULT_BAD, "Il te manque l%s permission%s :\n&6%s", noPermission.size() > 1 ? "es" : "a", noPermission.size() > 1 ? "s" : "", String.join("&e, &6", noPermission));
			return;
		}
		Entry<String, OlympaPermission> entry = cmd.getArgument(0);
		OlympaPermission perm = entry.getValue();
		boolean hasPermission = hasPermission(perm);
		StringJoiner sj = new StringJoiner("\n");
		sj.add(String.format("&ePermission &6%s&e :", entry.getKey()));
		if (hasPermission)
			sj.add("&aTu as cette permission.");
		else
			sj.add("&aTu n'as pas cette permission.");
		Gender gender = player != null ? getOlympaPlayer().getGender() : Gender.UNSPECIFIED;
		sj.add(String.format("&eGroups: &6%s", Arrays.stream(perm.getAllGroupsAllowed()).map(g -> g.getName(gender)).collect(Collectors.joining("&e, &6"))));
		if (perm.getAllowedBypass() != null && perm.getAllowedBypass().length != 0)
			sj.add(String.format("&eExtra Permission: &6%s", Arrays.stream(perm.getAllowedBypass()).map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.joining("&e, &6"))));
		sendMessage(Prefix.NONE, sj.toString());
	}

	@Cmd(player = true, min = 1, syntax = "<bukkit permission>", args = { "permission", "PLAYERS" })
	public void giveBukkitPerm(CommandContext cmd) {
		Player target;
		if (cmd.getArgumentsLength() > 1)
			target = cmd.getArgument(1);
		else
			target = player;
		PermissionAttachment attachment = target.addAttachment(OlympaCore.getInstance());
		attachment.setPermission(cmd.<String>getArgument(0), true);
		target.recalculatePermissions();
		target.updateCommands();
		sendMessage(Prefix.DEFAULT_GOOD, "Le joueur &2%s&a a désormais la permission bukkit &2%s&a.", target.getName(), cmd.<String>getArgument(0));
	}

	@Cmd(player = true, min = 1, syntax = "<bukkit permission>", args = { "permission", "PLAYERS" })
	public void removeBukkitPerm(CommandContext cmd) {
		Player target;
		if (cmd.getArgumentsLength() > 1)
			target = cmd.getArgument(1);
		else
			target = player;
		PermissionAttachment attachment = target.addAttachment(OlympaCore.getInstance());
		attachment.setPermission(cmd.<String>getArgument(0), false);
		target.recalculatePermissions();
		target.updateCommands();
		sendMessage(Prefix.DEFAULT_GOOD, "Le joueur &2%s&a n'a plus la permission bukkit &2%s&a.", target.getName(), cmd.<String>getArgument(0));
	}

	@Cmd(args = { "PERMISSION", "GROUPS|PLAYERS", "save" }, min = 2)
	public void allow(CommandContext cmd) {
		Entry<String, OlympaPermission> entry = cmd.getArgument(0);
		String permName = entry.getKey();
		OlympaPermission perm = entry.getValue();
		if (!(perm instanceof OlympaSpigotPermission)) {
			sendError("La permission &4%s&c est gérée par le Bungee. Feature à venir.", permName);
			return;
		}
		OlympaSpigotPermission spigotPerm = (OlympaSpigotPermission) perm;
		OlympaGroup olympaGroup = null;
		Player player = null;
		if (spigotPerm.getServerType() != ServerType.SPIGOT) {
			sendError("La permission &4%s&c est une permission &4%s&c.", permName, Utils.capitalize(spigotPerm.getServerType().name()));
			return;
		}
		if (cmd.getArgument(1) instanceof OlympaGroup) {
			olympaGroup = cmd.getArgument(1);
			if (spigotPerm.hasPermission(olympaGroup)) {
				sendError("Le groupe &4%s&c a déjà la permission &4%s&c.", olympaGroup.getName(), permName);
				return;
			}
		} else if (cmd.getArgument(1) instanceof Player) {
			player = cmd.getArgument(1);
			if (spigotPerm.hasPermission(player.getUniqueId())) {
				sendError("Le joueur &4%s&c a déjà la permission &4%s&c.", player.getName(), permName);
				return;
			}
		} else if (cmd.getArgument(1) instanceof String) {
			String all = cmd.getArgument(1);
			if (!all.equals("ALL"))
				return;
			spigotPerm.enable();
			sendMessage(Prefix.DEFAULT_GOOD, "La permission &2%s&a a été réactiver.", permName);
			return;
		} else {
			sendError("Le type de &4%s&c n'est pas connu par la commande.", cmd.getArgument(1));
			return;
		}
		if (spigotPerm.isLocked()) {
			sendError("La permission &4%s&c est verrouiller. Impossible de la modifier.", permName);
			return;
		}
		if (olympaGroup != null) {
			spigotPerm.allowGroup(olympaGroup);
			sendMessage(Prefix.DEFAULT_GOOD, "Le group &2%s&a a désormais la permission &2%s&a.", olympaGroup.getName(), permName);
			if (cmd.getArgumentsLength() > 2 && "save".equalsIgnoreCase(cmd.getArgument(2))) {
				AccountProvider accProvider = new AccountProvider(player.getUniqueId());
				OlympaPlayer op = accProvider.getFromCache();
				((OlympaPlayerObject) op).removeCustomPermission(spigotPerm, OlympaCore.getInstance().getOlympaServer());
			}
		} else if (player != null) {
			spigotPerm.allowPlayer(player);
			sendMessage(Prefix.DEFAULT_GOOD, "Le joueur &2%s&a a désormais la permission &2%s&a.", player.getName(), permName);
			if (cmd.getArgumentsLength() > 2 && "save".equalsIgnoreCase(cmd.getArgument(2))) {
				AccountProvider accProvider = new AccountProvider(player.getUniqueId());
				OlympaPlayer op = accProvider.getFromCache();
				((OlympaPlayerObject) op).addCustomPermission(spigotPerm, OlympaCore.getInstance().getOlympaServer());
			}
		}
	}

	@Cmd(args = { "PERMISSION", "GROUPS|PLAYERS|ALL" }, min = 2)
	public void disallow(CommandContext cmd) {
		Entry<String, OlympaPermission> entry = cmd.getArgument(0);
		String permName = entry.getKey();
		OlympaPermission perm = entry.getValue();
		if (!(perm instanceof OlympaSpigotPermission)) {
			sendError("La permission &4%s&c est gérée par le Bungee. Feature à venir.", permName);
			return;
		}
		OlympaSpigotPermission spigotPerm = (OlympaSpigotPermission) perm;
		Object arg1 = cmd.getArgument(1);
		OlympaGroup olympaGroup = null;
		Player player = null;
		if (arg1 instanceof OlympaGroup) {
			olympaGroup = cmd.getArgument(1);
			if (!spigotPerm.hasPermission(olympaGroup)) {
				sendError("Le groupe &4%s&c n'a pas la permission &4%s&c.", olympaGroup.getName(), permName);
				return;
			}
		} else if (arg1 instanceof Player) {
			player = cmd.getArgument(1);
			if (!spigotPerm.hasPermission(player.getUniqueId())) {
				sendError("Le joueur &4%s&c n'a pas la permission &4%s&c.", player.getName(), permName);
				return;
			}
		} else if (arg1 instanceof String) {
			String all = cmd.getArgument(1);
			if (!all.equals("ALL"))
				return;
			spigotPerm.disable();
			sendMessage(Prefix.DEFAULT_GOOD, "La permission &2%s&a a été désactiver pour tous le monde, sauf pour le haut-staff et les perms par joueur.", permName);
			return;
		}
		if (spigotPerm.isLocked()) {
			sendError("La permission &4%s&c est verrouiller. Impossible de la modifier.", permName);
			return;
		}
		if (olympaGroup != null) {
			spigotPerm.disallowGroup(olympaGroup);
			sendMessage(Prefix.DEFAULT_BAD, "Le group &4%s&c n'a désormais plus la permission &4%s&c.", olympaGroup.getName(), permName);
		} else {
			spigotPerm.disallowPlayer(player);
			sendMessage(Prefix.DEFAULT_BAD, "Le joueur &4%s&c n'a désormais plus la permission &4%s&c.", player.getName(), permName);
		}
	}
	//   /permission see <groups|player>

	@Cmd(args = { "GROUPS|PLAYERS" }, min = 1)
	public void see(CommandContext cmd) {
		OlympaGroup olympaGroup = null;
		Player player = null;
		Collection<OlympaPermission> allPerms = OlympaPermission.permissions.values();
		if (cmd.getArgument(0) instanceof OlympaGroup) {
			olympaGroup = cmd.getArgument(0);
			OlympaGroup olympaGroup2 = olympaGroup;
			List<String> noPermission = OlympaPermission.permissions.entrySet().stream().filter(entry -> !entry.getValue().hasPermission(olympaGroup2)).map(Entry::getKey).collect(Collectors.toList());
			sendMessage(Prefix.DEFAULT_GOOD, "&2%s&a a %s permission%s sur %s.", olympaGroup.getPrefix(), allPerms.size() - noPermission.size(), noPermission.size() > 1 ? "s" : "", allPerms.size());
			if (!noPermission.isEmpty())
				sendMessage(Prefix.DEFAULT_BAD, "Il manque l%s permission%s :\n&6%s", noPermission.size() > 1 ? "es" : "a", noPermission.size() > 1 ? "s" : "", String.join("&e, &6", noPermission));
		} else if (cmd.getArgument(0) instanceof Player) {
			player = cmd.getArgument(0);
			Player player2 = player;
			List<String> extraPermission = OlympaPermission.permissions.entrySet().stream().filter(entry -> !entry.getValue().isInAllowedBypass(player2.getUniqueId())).map(Entry::getKey).collect(Collectors.toList());
			List<String> noPermission = OlympaPermission.permissions.entrySet().stream().filter(entry -> !entry.getValue().hasPermission(player2.getUniqueId())).map(Entry::getKey).collect(Collectors.toList());
			sendMessage(Prefix.DEFAULT_GOOD, "&2%s&a a %s permission%s sur %s. %s", player.getName(), allPerms.size() - noPermission.size(), noPermission.size() > 1 ? "s" : "", allPerms.size(), player.isOp() ? "&a[&2OP]" : "");
			if (!noPermission.isEmpty())
				sendMessage(Prefix.NONE, "&cIl manque l%s permission%s :\n&6%s", noPermission.size() > 1 ? "es" : "a", noPermission.size() > 1 ? "s" : "", String.join("&e, &6", noPermission));
			if (!extraPermission.isEmpty())
				sendMessage(Prefix.ERROR, "Extra permission%s :\n&6%s", noPermission.size() > 1 ? "es" : "a", noPermission.size() > 1 ? "s" : "", String.join("&e, &6", noPermission));
		} else {
			sendError("Le type de &4%s&c n'est pas connu par la commande.", cmd.getArgument(1));
			return;
		}
	}
}
