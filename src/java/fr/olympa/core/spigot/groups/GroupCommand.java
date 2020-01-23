package fr.olympa.core.spigot.groups;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.groups.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.groups.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.MySQL;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.UtilsCore;
import fr.olympa.core.spigot.OlympaCore;

public class GroupCommand extends OlympaCommand {

	public GroupCommand(Plugin plugin) {
		super(plugin, "group", "Permet la gestion des groupes de Olympa.", OlympaCorePermissions.GROUP_COMMAND, "groupe", "rank");
		this.setUsageString("<joueur> <group> [until] [add|remove]");
		this.setMinArg(1);
		this.isAsynchronous = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target;
		OlympaPlayer olympaTarget;
		AccountProvider olympaAccount = null;
		if (args.length == 0) {
			target = this.player;
			olympaTarget = this.getOlympaPlayer();
		} else if (args.length <= 3) {
			target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				olympaTarget = MySQL.getPlayer(args[0]);
				if (olympaTarget == null) {
					Collection<String> pentialsPlayers = UtilsCore.similarWords(args[0], MySQL.getAllPlayersNames());
					if (pentialsPlayers.isEmpty()) {
						this.sendMessage(Prefix.DEFAULT_BAD + "Le joueur &4%player&c ne s'est jamais connecté.".replaceFirst("%player", args[0]));
					} else {
						this.sendMessage(Prefix.DEFAULT_BAD + "Le joueur %player ne s'est jamais connecté. Essayez avec &4%potentialName&c."
								.replaceFirst("%player", args[0]).replaceFirst("%potentialName", String.join(", ", pentialsPlayers)));
					}
					return true;
				} else {
					olympaAccount = new AccountProvider(olympaTarget.getUniqueId());
				}
			} else {
				olympaAccount = new AccountProvider(target.getUniqueId());
				olympaTarget = olympaAccount.getFromCache();
			}
		} else {
			this.sendUsage(label);
			return true;
		}
		if (args.length <= 1) {
			TreeMap<OlympaGroup, Long> groups = olympaTarget.getGroups();
			String targetNamePrefix = groups.firstKey().getPrefix() + olympaTarget.getName() + Prefix.INFO.getColor();
			String groupString = olympaTarget.getGroupsToHumainString();
			this.sendMessage(Prefix.INFO + "%player est dans le%s groupe%s %group."
					.replaceFirst("%player", targetNamePrefix)
					.replaceAll("%s", groups.size() > 1 ? "s" : "")
					.replaceFirst("%group", Prefix.INFO.getColor2() + groupString + Prefix.INFO.getColor()));
		} else if (args.length <= 3) {
			OlympaGroup newGroup = OlympaGroup.getByName(args[1]);
			if (newGroup == null) {
				Collection<String> pentialsGroup = UtilsCore.similarWords(args[1], Arrays.stream(OlympaGroup.values()).map(OlympaGroup::getName).collect(Collectors.toSet()));
				if (pentialsGroup.isEmpty()) {

					this.sendMessage(Prefix.DEFAULT_BAD + "Le groupe &4%group&c n'existe pas.".replaceFirst("%group", args[1]));
				} else {
					this.sendMessage(Prefix.DEFAULT_BAD + "Le groupe &4%group&c n'existe pas. Essayez plutôt avec &4%pentialsGroup&c."
							.replaceFirst("%group", args[1]).replaceFirst("%pentialsGroup", String.join(", ", pentialsGroup)));
				}
				return true;
			}
			TreeMap<OlympaGroup, Long> oldGroups = olympaTarget.getGroups();
			if (oldGroups.containsKey(newGroup)) {
				this.sendMessage(Prefix.DEFAULT_BAD + "%player&c est déjà dans le groupe &4%group&c.".replaceFirst("%player", olympaTarget.getName()).replaceFirst("%group", newGroup.getName()));
				return true;
			}

			long timestamp = 0;
			if (args.length == 3) {
				if (Matcher.isInt(args[2])) {
					timestamp = Long.parseLong(args[2]);
				} else {
					this.sendMessage(Prefix.DEFAULT_BAD + "&4%arg3&c doit être un timestamp tel que &41587356400&c.".replaceFirst("%arg3", args[3]));
					return true;
				}
			}

			String timestampString = "";
			if (timestamp != 0) {
				timestampString = "pendant &2" + Utils.timestampToDuration(timestamp) + "&a";
			}

			if (target == null) {
				olympaTarget.setGroup(newGroup, timestamp);
				olympaAccount.saveToDb(olympaTarget);

				Consumer<? super Boolean> done = b -> {
					if (b) {
						this.sendMessage("&aLe nouveau grade du joueur &2%player&a bien été reçu sur un autre serveur.".replaceFirst("%player", olympaTarget.getName()));
					} else {
						this.sendMessage("&aLe joueur &2%player&a n'est pas connecté, la modification a bien été prise en compte.".replaceFirst("%player", olympaTarget.getName()));
					}
				};
				// olympaAccount.sendModifications(olympaTarget, done);
			} else {
				olympaTarget.setGroup(newGroup, timestamp);
				OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(target, ChangeType.ADD, olympaTarget, newGroup));
				olympaAccount.saveToRedis(olympaTarget);
				olympaAccount.saveToDb(olympaTarget);
				this.sendMessage(target, "&aVous êtes désormais dans le groupe &2%group&a%time.".replaceFirst("%group", newGroup.getName()).replaceFirst("%time", timestampString));
			}

			if (target == null || !SpigotUtils.isSamePlayer((Player) this.sender, target)) {
				this.sendMessage("&aLe joueur &2%player&a est désormais dans le groupe &2%group&a%time."
						.replaceFirst("%player", olympaTarget.getName())
						.replaceFirst("%group", newGroup.getName())
						.replaceFirst("%time", timestampString));
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length == 1) {
			List<String> postentielNames = Utils.startWords(args[0], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
			return postentielNames;
		} else if (args.length == 2) {
			List<String> postentielGroups = Utils.startWords(args[1], Arrays.stream(OlympaGroup.values()).map(OlympaGroup::getName).collect(Collectors.toList()));
			return postentielGroups;
		}
		return null;
	}
}
