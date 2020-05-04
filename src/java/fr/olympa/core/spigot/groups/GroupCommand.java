package fr.olympa.core.spigot.groups;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
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
import fr.olympa.core.spigot.redis.RedisSpigotSend;

@SuppressWarnings("deprecation")
public class GroupCommand extends OlympaCommand {

	public GroupCommand(Plugin plugin) {
		super(plugin, "group", "Permet la gestion des groupes de Olympa.", OlympaCorePermissions.GROUP_COMMAND, "groupe", "rank");
		addArgs(true, "joueur");
		addArgs(false, "group");
		addArgs(false, "until");
		addArgs(false, "add", "remove");
		isAsynchronous = true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target;
		OlympaPlayer olympaTarget;
		AccountProvider olympaAccount = null;
		if (args.length == 0) {
			target = player;
			olympaTarget = this.getOlympaPlayer();
		} else if (args.length <= 4) {
			target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				olympaTarget = MySQL.getPlayer(args[0]);
				if (olympaTarget == null) {
					Collection<String> pentialsPlayers = UtilsCore.similarWords(args[0], MySQL.getAllPlayersNames());
					if (pentialsPlayers.isEmpty()) {
						this.sendMessage(Prefix.DEFAULT_BAD + "Le joueur &4%player&c ne s'est jamais connecté.".replace("%player", args[0]));
					} else {
						this.sendMessage(Prefix.DEFAULT_BAD + "Le joueur %player ne s'est jamais connecté. Essayez avec &4%potentialName&c."
								.replace("%player", args[0]).replace("%potentialName", String.join(", ", pentialsPlayers)));
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
					.replace("%player", targetNamePrefix)
					.replaceAll("%s", groups.size() > 1 ? "s" : "")
					.replace("%group", Prefix.INFO.getColor2() + groupString + Prefix.INFO.getColor()));
		} else {
			OlympaGroup newGroup = OlympaGroup.getByName(args[1]);
			if (newGroup == null) {
				Collection<String> pentialsGroup = UtilsCore.similarWords(args[1], Arrays.stream(OlympaGroup.values()).map(OlympaGroup::getName).collect(Collectors.toSet()));
				if (pentialsGroup.isEmpty()) {
					this.sendMessage(Prefix.DEFAULT_BAD + "Le groupe &4%group&c n'existe pas.".replace("%group", args[1]));
				} else {
					this.sendMessage(Prefix.DEFAULT_BAD + "Le groupe &4%group&c n'existe pas. Essayez plutôt avec &4%pentialsGroup&c."
							.replace("%group", args[1]).replace("%pentialsGroup", String.join(", ", pentialsGroup)));
				}
				return true;
			}

			long timestamp = 0;
			if (args.length >= 3) {
				if (Matcher.isInt(args[2])) {
					timestamp = Long.parseLong(args[2]);
					if (timestamp != 0 && timestamp < Utils.getCurrentTimeInSeconds()) {
						this.sendMessage(Prefix.DEFAULT_BAD + ("&4%arg3&c est plus petit que le timestamp actuel: &4" + Utils.getCurrentTimeInSeconds() + "&c.").replace("%arg3", args[2]));
						return true;
					}
				} else {
					this.sendMessage(Prefix.DEFAULT_BAD + ("&4%arg3&c doit être un timestamp tel que &4" + Utils.getCurrentTimeInSeconds() + "&c.").replace("%arg3", args[2]));
					return true;
				}
			}

			TreeMap<OlympaGroup, Long> oldGroups = olympaTarget.getGroups();
			OlympaPlayer oldOlympaTarget = olympaTarget.clone();
			String timestampString = new String();
			if (timestamp != 0) {
				timestampString = "pendant &2" + Utils.timestampToDuration(timestamp) + "&a";
			}

			ChangeType state;
			String msg = "&aTu es désormais dans le groupe &2%group&a%time.";
			if (args.length >= 4) {
				if (args[3].equalsIgnoreCase("add")) {
					Entry<OlympaGroup, Long> oldGroup = oldGroups.entrySet().stream().filter(entry -> entry.getKey().getId() == newGroup.getId()).findFirst().orElse(null);
					if (oldGroup != null && oldGroup.getValue() == timestamp) {
						this.sendMessage(Prefix.DEFAULT_BAD + "%player&c est déjà dans le groupe &4%group&c.".replace("%player", olympaTarget.getName()).replace("%group", newGroup.getName()));
						return true;
					}
					state = ChangeType.ADD;
					olympaTarget.addGroup(newGroup, timestamp);
					Entry<OlympaGroup, Long> entry = olympaTarget.getGroups().firstEntry();
					OlympaGroup principalGroup = entry.getKey();
					Long timestamp2 = entry.getValue();
					String timestampString2 = new String();
					if (timestamp2 != 0) {
						timestampString2 = "pendant &2" + Utils.timestampToDuration(timestamp2) + "&a";
					}
					msg = "&aTu es désormais en plus dans le groupe &2%group&a%time. Ton grade principale est &2%group2&a%time2.".replace("%time2", timestampString2).replace("%group2", principalGroup.getName());
				} else if (args[3].equalsIgnoreCase("remove")) {
					if (!oldGroups.containsKey(newGroup)) {
						this.sendMessage(Prefix.DEFAULT_BAD + "%player&c n'est pas dans le groupe &4%group&c.".replace("%player", olympaTarget.getName()).replace("%group", newGroup.getName()));
						return true;
					}
					msg = null;
					state = ChangeType.REMOVE;
					olympaTarget.removeGroup(newGroup);
				} else {
					sendUsage(label);
					return true;
				}
			} else {
				if (oldGroups.containsKey(newGroup)) {
					this.sendMessage(Prefix.DEFAULT_BAD + "%player&c est déjà dans le groupe &4%group&c.".replace("%player", olympaTarget.getName()).replace("%group", newGroup.getName()));
					return true;
				}
				state = ChangeType.SET;
				olympaTarget.setGroup(newGroup, timestamp);
			}

			if (target == null) {
				olympaAccount.saveToDb(olympaTarget);

				Consumer<? super Boolean> done = b -> {
					if (b) {
						this.sendMessage("&aLe nouveau grade du joueur &2%player&a bien été reçu sur un autre serveur.".replace("%player", olympaTarget.getName()));
					} else {
						this.sendMessage("&aLe joueur &2%player&a n'est pas connecté, la modification a bien été prise en compte.".replace("%player", olympaTarget.getName()));
					}
				};
				olympaAccount.sendModifications(olympaTarget, done);
			} else {
				RedisSpigotSend.sendOlympaGroupChange(oldOlympaTarget, newGroup, timestamp, state);
				OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(target, ChangeType.ADD, olympaTarget, newGroup));
				olympaAccount.saveToRedis(olympaTarget);
				olympaAccount.saveToDb(olympaTarget);
				this.sendMessage(target, msg.replace("%group", newGroup.getName()).replace("%time", timestampString));
			}

			if (player != null && (target == null || !SpigotUtils.isSamePlayer(player, target))) {
				if (msg == null) {
					this.sendMessage("&cLe joueur &4%player&a n'est plus dans le groupe &4%group&c."
							.replace("%player", olympaTarget.getName())
							.replace("%group", newGroup.getName()));
				} else {
					this.sendMessage("&aLe joueur &2%player&a est désormais dans le groupe &2%group&a%time."
							.replace("%player", olympaTarget.getName())
							.replace("%group", newGroup.getName())
							.replace("%time", timestampString));
				}
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length == 1) {
			return Utils.startWords(args[0], Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
		} else if (args.length == 2) {
			return Utils.startWords(args[1], Arrays.stream(OlympaGroup.values()).map(OlympaGroup::getName).collect(Collectors.toList()));
		} else if (args.length == 3) {
			return Utils.startWords(args[2], Arrays.asList(String.valueOf(Utils.getCurrentTimeInSeconds() + 2628000), "0"));
		} else if (args.length == 4) {
			return Utils.startWords(args[3], Arrays.asList("add", "remove"));
		}
		return null;
	}
}
