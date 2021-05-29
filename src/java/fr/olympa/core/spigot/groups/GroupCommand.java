package fr.olympa.core.spigot.groups;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent;
import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.permission.list.OlympaCorePermissionsSpigot;
import fr.olympa.api.player.Gender;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.UtilsCore;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.redis.RedisSpigotSend;

@SuppressWarnings("deprecation")
public class GroupCommand extends OlympaCommand {

	public GroupCommand(Plugin plugin) {
		super(plugin, "group", "Permet de gérer les groupes d'un joueur Olympa.", OlympaCorePermissionsSpigot.GROUP_COMMAND, "groupe", "rank");
		addArgs(false, "JOUEUR");
		addArgs(false, "group");
		addArgs(false, "time");
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
			if (isConsole()) {
				sendImpossibleWithConsole();
				sendUsage(label);
				return true;
			}
			olympaTarget = this.getOlympaPlayer();
		} else if (args.length <= 4) {
			target = Bukkit.getPlayer(args[0]);
			if (target == null)
				try {
					olympaTarget = AccountProvider.get(args[0]);
					if (olympaTarget == null) {
						this.sendUnknownPlayer(args[0], AccountProvider.getSQL().getNamesBySimilarName(args[0]));
						return true;
					} else
						olympaAccount = new AccountProvider(olympaTarget.getUniqueId());
				} catch (SQLException e) {
					e.printStackTrace();
					sendError(e.getMessage());
					return true;
				}
			else {
				olympaAccount = new AccountProvider(target.getUniqueId());
				olympaTarget = olympaAccount.getFromCache();
			}
		} else {
			sendUsage(label);
			return true;
		}
		if (args.length <= 1) {
			Map<OlympaGroup, Long> groups = olympaTarget.getGroups();
			String targetNamePrefix = olympaTarget.getNameWithPrefix() + Prefix.INFO.getColor();
			String groupString = olympaTarget.getGroupsToHumainString();
			sendInfo("%player est dans le%s groupe%s %group."
					.replace("%player", targetNamePrefix)
					.replace("%s", groups.size() > 1 ? "s" : "")
					.replace("%group", Prefix.INFO.getColor2() + groupString + Prefix.INFO.getColor()));
		} else {
			String arg1 = args[1];
			OlympaGroup newGroup = OlympaGroup.getByName(arg1);
			if (newGroup == null) {
				Collection<String> pentialsGroup = UtilsCore.similarWords(arg1, Arrays.stream(OlympaGroup.values()).map(OlympaGroup::getName).collect(Collectors.toSet()));
				if (pentialsGroup.isEmpty())
					this.sendError("Le groupe &4%s&c n'existe pas.", arg1);
				else
					this.sendError("Le groupe &4%s&c n'existe pas. Essayez plutôt avec &4%s&c.", arg1, ColorUtils.joinRed(pentialsGroup));
				return true;
			}

			int nextArg = 2;

			long timestamp = 0;
			if (args.length >= nextArg + 1)
				if (RegexMatcher.INT.is(args[nextArg])) {
					timestamp = Long.parseLong(args[nextArg]);
					if (timestamp != 0 && timestamp < Utils.getCurrentTimeInSeconds()) {
						this.sendError("&4%s&c est plus petit que le timestamp actuel: &4%d&c.", args[2], Utils.getCurrentTimeInSeconds());
						return true;
					}
					nextArg++;
				}
			//					this.sendError("&4%s&c doit être un timestamp tel que &4%d&c.", args[2], Utils.getCurrentTimeInSeconds());
			//					return true;

			Gender gender = olympaTarget.getGender();
			Map<OlympaGroup, Long> oldGroups = olympaTarget.getGroups();
			String timestampString = new String();
			if (timestamp != 0)
				timestampString = " /groupendant &2" + Utils.timestampToDuration(timestamp) + "&a";

			ChangeType state;
			String msg = "&aTu es désormais dans le groupe &2%group&a%time.";
			if (args.length >= nextArg + 1) {
				if (args[nextArg].equalsIgnoreCase("add")) {
					Entry<OlympaGroup, Long> oldGroup = oldGroups.entrySet().stream().filter(entry -> entry.getKey().getId() == newGroup.getId()).findFirst().orElse(null);
					if (oldGroup != null && oldGroup.getValue() == timestamp) {
						this.sendError("%s&c est déjà dans le groupe &4%s&c.", olympaTarget.getName(), newGroup.getName(gender));
						return true;
					}
					state = ChangeType.ADD;
					olympaTarget.addGroup(newGroup, timestamp);
					Entry<OlympaGroup, Long> entry = ((TreeMap<OlympaGroup, Long>) olympaTarget.getGroups()).firstEntry();
					OlympaGroup principalGroup = entry.getKey();
					Long timestamp2 = entry.getValue();
					String timestampString2 = new String();
					if (timestamp2 != 0)
						timestampString2 = " pendant &2" + Utils.timestampToDuration(timestamp2) + "&a";
					msg = "&aTu es désormais en plus dans le groupe &2%group&a%time. Ton grade principale est &2%group2&a%time2.".replace("%time2", timestampString2).replace("%group2", principalGroup.getName(gender));
				} else if (args[nextArg].equalsIgnoreCase("remove")) {
					if (!oldGroups.containsKey(newGroup)) {
						this.sendError("%s&c n'est pas dans le groupe &4%s&c.", olympaTarget.getName(), newGroup.getName(gender));
						return true;
					}
					olympaTarget.removeGroup(newGroup);
					Entry<OlympaGroup, Long> entry = ((TreeMap<OlympaGroup, Long>) olympaTarget.getGroups()).firstEntry();
					OlympaGroup principalGroup = entry.getKey();
					Long timestamp2 = entry.getValue();
					String timestampString2 = new String();
					if (timestamp2 != 0)
						timestampString2 = " pendant &4" + Utils.timestampToDuration(timestamp2) + "&c";
					msg = "&cTu as été démote du groupe &4%group&c%time&c. Ton grade principale deviens &4%group2&c%time2&c.".replace("%time2", timestampString2).replace("%group2", principalGroup.getName(gender));
					state = ChangeType.REMOVE;
				} else {
					sendUsage(label);
					return true;
				}
			} else {
				if (oldGroups.containsKey(newGroup)) {
					this.sendError("%s&c est déjà dans le groupe &4%s&c.", olympaTarget.getName(), newGroup.getName(gender));
					return true;
				}
				state = ChangeType.SET;
				olympaTarget.setGroup(newGroup, timestamp);
			}
			if (target == null) {
				Consumer<? super Boolean> done = b -> {
					if (b)
						sendInfo("&6Le joueur est connecté sur un autre serveur. &aLe changement de grade de &2%s&a bien été reçu sur l'infrastructure (dont discord).", olympaTarget.getName());
					else {
						sendInfo("&cLe joueur &4%s&c n'est pas connecté, &cle changement de grade a bien été reçu (dont discord).", olympaTarget.getName());
						AccountProvider olympaAccount2 = new AccountProvider(olympaTarget.getUniqueId());
						olympaAccount2.removeFromRedis();
						//((OlympaPlayerObject) olympaTarget).updateGroups();
						//						olympaAccount2.saveToDb(olympaTarget);
					}
				};
				RedisSpigotSend.sendOlympaGroupChange(olympaTarget, newGroup, timestamp, state, done);
				olympaAccount.removeFromCache();
			} else {
				olympaAccount.saveToRedis(olympaTarget);
				//olympaAccount.saveToDb(olympaTarget);
				olympaAccount.saveToCache(olympaTarget);
				Prefix.DEFAULT.sendMessage(target, msg.replace("%group", newGroup.getName()).replace("%time", timestampString));
				OlympaCore.getInstance().getServer().getPluginManager().callEvent(new AsyncOlympaPlayerChangeGroupEvent(target, state, olympaTarget, null, timestamp, newGroup));
				RedisSpigotSend.sendOlympaGroupChange(olympaTarget, newGroup, timestamp, state, null);
			}
			if (player != null && (target == null || !SpigotUtils.isSamePlayer(player, target)))
				if (!olympaTarget.getGroups().containsKey(newGroup))
					sendSuccess("&cLe joueur &4%s&c n'est plus dans le groupe &4%s&c.", olympaTarget.getName(), newGroup.getName(gender));
				else
					sendSuccess("&aLe joueur &2%s&a est désormais dans le groupe &2%s&a%s.", olympaTarget.getName(), newGroup.getName(gender), timestampString);
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (args.length == 1) {
			List<String> potentialArgs = new ArrayList<>();
			potentialArgs.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
			if (args[0].length() > 2)
				potentialArgs.addAll(AccountProvider.getSQL().getNamesBySimilarName(args[0]));
			return Utils.startWords(args[0], potentialArgs);
		} else if (args.length == 2)
			return Utils.startWords(args[1], Arrays.stream(OlympaGroup.values()).map(OlympaGroup::getName).collect(Collectors.toList()));
		else if (args.length == 3)
			return Utils.startWords(args[2], Arrays.asList("add", "remove", String.valueOf(Utils.getCurrentTimeInSeconds() + 2628000), "0"));
		else if (args.length == 4)
			return Utils.startWords(args[3], Arrays.asList("add", "remove"));
		return null;
	}
}
