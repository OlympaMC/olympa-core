package fr.olympa.core.spigot.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.Gender;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

@SuppressWarnings("deprecation")
public class PermissionCommand extends ComplexCommand {

	public PermissionCommand(Plugin plugin) {
		super(plugin, "permission", "Voir et modifier les permissions jusqu'au redémarrage", OlympaCorePermissions.PERMISSION_COMMAND, "perm", "p");
		super.addArgumentParser("PERMISSION", (player) -> {
			return OlympaPermission.permissions.entrySet().stream().map(Entry::getKey).collect(Collectors.toList());
		}, arg -> {
			Optional<Entry<String, OlympaPermission>> op = OlympaPermission.permissions.entrySet().stream().filter(e -> e.getKey().equalsIgnoreCase(arg)).findFirst();
			if (op.isPresent())
				return op.get();
			sendError("La permission &4%s&c n'existe pas", arg);
			return null;
		});
		super.addArgumentParser("GROUPS", (player) -> {
			return Arrays.stream(OlympaGroup.values()).map(OlympaGroup::getName).collect(Collectors.toList());
		}, arg -> {
			Optional<OlympaGroup> op = Arrays.stream(OlympaGroup.values()).filter(e -> e.getName().equalsIgnoreCase(arg)).findFirst();
			if (op.isPresent())
				return op.get();
			sendError("Le groupe &4%s&c n'existe pas", arg);
			return null;
		});
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
			sj.add(String.format("&aTu as cette permission."));
		else
			sj.add(String.format("&aTu n'as pas cette permission."));
		Gender gender = player != null ? getOlympaPlayer().getGender() : Gender.UNSPECIFIED;
		sj.add(String.format("&eGroups: &6%s", Arrays.stream(perm.getAllGroupsAllowed()).map(g -> g.getName(gender)).collect(Collectors.joining("&e, &6"))));
		sendMessage(Prefix.NONE, sj.toString());
		if (player != null)
			sendSuccess("Tu as les permissions Bukkit suivantes : §6%s", player.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission).collect(Collectors.joining(", ")));
	}

	@Cmd(player = true, min = 1, syntax = "<bukkit permission>")
	public void giveBukkitPerm(CommandContext cmd) {
		PermissionAttachment attachment = player.addAttachment(OlympaCore.getInstance());
		attachment.setPermission(cmd.<String>getArgument(0), true);
		player.recalculatePermissions();
	}

	@Cmd(args = { "PERMISSION", "GROUPS|PLAYERS", }, min = 2)
	public void allow(CommandContext cmd) {
		Entry<String, OlympaPermission> entry = cmd.getArgument(0);
		String permName = entry.getKey();
		OlympaPermission perm = entry.getValue();
		Object arg1 = cmd.getArgument(1);
		OlympaGroup olympaGroup = null;
		Player player = null;
		if (arg1 instanceof OlympaGroup) {
			olympaGroup = cmd.getArgument(1);
			if (perm.hasPermission(olympaGroup)) {
				sendError("Le groupe &4%s&c a déjà la permission &4%s&c.", olympaGroup.getName(), permName);
				return;
			}
		} else if (arg1 instanceof Player) {
			player = cmd.getArgument(1);
			if (perm.hasPermission(player.getUniqueId())) {
				sendError("Le joueur &4%s&c a déjà la permission &4%s&c.", player.getName(), permName);
				return;
			}
		} else if (arg1 instanceof String) {
			String all = cmd.getArgument(1);
			if (!all.equals("ALL"))
				return;
			perm.enable();
			sendMessage(Prefix.DEFAULT_GOOD, "La permission &2%s&a a été réactiver.", permName);
			return;
		}
		if (perm.isLocked()) {
			sendError("La permission &4%s&c est verrouiller. Impossible de la modifier.", permName);
			return;
		}
		if (olympaGroup != null) {
			perm.allowGroup(olympaGroup);
			sendMessage(Prefix.DEFAULT_GOOD, "Le group &2%s&a a désormais la permission &2%s&a.", olympaGroup.getName(), permName);
		} else {
			perm.allowPlayer(player);
			sendMessage(Prefix.DEFAULT_GOOD, "Le joueur &2%s&a a désormais la permission &2%s&a.", player.getName(), permName);
		}
	}

	@Cmd(args = { "GROUPS|PLAYERS" }, min = 1)
	public void see(CommandContext cmd) {
		Object arg1 = cmd.getArgument(1);
		OlympaGroup olympaGroup = null;
		Player player = null;
		Collection<OlympaPermission> allPerms = OlympaPermission.permissions.values();
		if (arg1 instanceof OlympaGroup) {
			olympaGroup = cmd.getArgument(1);
			OlympaGroup olympaGroup2 = olympaGroup;
			List<String> noPermission = OlympaPermission.permissions.entrySet().stream().filter(entry -> !entry.getValue().hasPermission(olympaGroup2)).map(Entry::getKey).collect(Collectors.toList());
			sendMessage(Prefix.DEFAULT_GOOD, "&2%s&a a %s permission%s sur %s.", olympaGroup.getPrefix(), allPerms.size() - noPermission.size(), noPermission.size() > 1 ? "s" : "", allPerms.size());
			if (!noPermission.isEmpty())
				sendMessage(Prefix.DEFAULT_BAD, "Il manque l%s permission%s :\n&6%s", noPermission.size() > 1 ? "es" : "a", noPermission.size() > 1 ? "s" : "", String.join("&e, &6", noPermission));
		} else {
			player = cmd.getArgument(1);
			Player player2 = player;
			List<String> extraPermission = OlympaPermission.permissions.entrySet().stream().filter(entry -> !entry.getValue().isInAllowedBypass(player2.getUniqueId())).map(Entry::getKey).collect(Collectors.toList());
			List<String> noPermission = OlympaPermission.permissions.entrySet().stream().filter(entry -> !entry.getValue().hasPermission(player2.getUniqueId())).map(Entry::getKey).collect(Collectors.toList());
			sendMessage(Prefix.DEFAULT_GOOD, "&2%s&a a %s permission%s sur %s.", player.getName(), allPerms.size() - noPermission.size(), noPermission.size() > 1 ? "s" : "", allPerms.size());
			if (!noPermission.isEmpty())
				sendMessage(Prefix.NONE, "&cIl manque l%s permission%s :\n&6%s", noPermission.size() > 1 ? "es" : "a", noPermission.size() > 1 ? "s" : "", String.join("&e, &6", noPermission));
			if (!extraPermission.isEmpty())
				sendMessage(Prefix.ERROR, "Extra permission%s :\n&6%s", noPermission.size() > 1 ? "es" : "a", noPermission.size() > 1 ? "s" : "", String.join("&e, &6", noPermission));
		}
	}

	@Cmd(args = { "PERMISSION", "GROUPS|PLAYERS|ALL" }, min = 2)
	public void disallow(CommandContext cmd) {
		Entry<String, OlympaPermission> entry = cmd.getArgument(0);
		String permName = entry.getKey();
		OlympaPermission perm = entry.getValue();
		Object arg1 = cmd.getArgument(1);
		OlympaGroup olympaGroup = null;
		Player player = null;
		if (arg1 instanceof OlympaGroup) {
			olympaGroup = cmd.getArgument(1);
			if (!perm.hasPermission(olympaGroup)) {
				sendError("Le groupe &4%s&c n'a pas la permission &4%s&c.", olympaGroup.getName(), permName);
				return;
			}
		} else if (arg1 instanceof Player) {
			player = cmd.getArgument(1);
			if (!perm.hasPermission(player.getUniqueId())) {
				sendError("Le joueur &4%s&c n'a pas la permission &4%s&c.", player.getName(), permName);
				return;
			}
		} else if (arg1 instanceof String) {
			String all = cmd.getArgument(1);
			if (!all.equals("ALL"))
				return;
			perm.disable();
			sendMessage(Prefix.DEFAULT_GOOD, "La permission &2%s&a a été désactiver pour tous le monde, sauf pour le haut-staff et les perms par joueur.", permName);
			return;
		}
		if (perm.isLocked()) {
			sendError("La permission &4%s&c est verrouiller. Impossible de la modifier.", permName);
			return;
		}
		if (olympaGroup != null) {
			perm.disallowGroup(olympaGroup);
			sendMessage(Prefix.DEFAULT_BAD, "Le group &4%s&c n'a désormais plus la permission &4%s&c.", olympaGroup.getName(), permName);
		} else {
			perm.disallowPlayer(player);
			sendMessage(Prefix.DEFAULT_BAD, "Le joueur &4%s&c n'a désormais plus la permission &4%s&c.", player.getName(), permName);
		}
	}
}
