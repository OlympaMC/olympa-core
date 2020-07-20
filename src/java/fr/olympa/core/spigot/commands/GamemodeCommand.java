package fr.olympa.core.spigot.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Matcher;
import fr.olympa.api.utils.Prefix;

public class GamemodeCommand extends OlympaCommand {

	private enum Gm {
		SURVIVAL("survie"),
		CREATIVE("creatif"),
		ADVENTURE("aventure"),
		SPECTATOR("spectateur");

		private Gm(String name) {
			this.name = name;
		}

		String name;

		public String getName() {
			return name;
		}

		public String getRealName() {
			return toString().toLowerCase();
		}

		public boolean isName(String name) {
			name = name.toLowerCase();
			return this.name.equals(name) || getRealName().equals(name);
		}

		public int getId() {
			return ordinal();
		}

		@SuppressWarnings("deprecation")
		public GameMode getGameMode() {
			return GameMode.getByValue(getId());
		}

		public static Gm getByStartWith(String startWith) {
			return Arrays.stream(Gm.values()).filter(gm -> gm.getName().startsWith(startWith)).findFirst().orElse(null);
		}

		public static Gm get(String nameOrId) {
			Gm gamemode;
			if (Matcher.isInt(nameOrId))
				gamemode = Arrays.stream(Gm.values()).filter(gm -> gm.getId() == Integer.parseInt(nameOrId)).findFirst().orElse(null);
			else
				gamemode = Arrays.stream(Gm.values()).filter(gm -> gm.isName(nameOrId)).findFirst().orElse(null);
			return gamemode;
		}

		public boolean isGameMode(GameMode gameMode) {
			return gameMode.toString() == toString();
		}

		public static Gm get(GameMode gameMode) {
			return Arrays.stream(Gm.values()).filter(gm -> gm.isGameMode(gameMode)).findFirst().orElse(null);
		}
	}

	public GamemodeCommand(Plugin plugin) {
		super(plugin, "gamemode", "Change ton mode de jeux", OlympaCorePermissions.GAMEMODE_COMMAND, "gm", "gms", "gma", "gmc", "gmsp");
		addArgs(false, "adventure", "creative", "survival", "spectator");
		addArgs(false, "joueur");
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target = player;
		Gm gm = null;
		if (label.startsWith("gm"))
			gm = Gm.getByStartWith(label.substring(2));
		if (gm == null)
			if (args.length == 0 || (gm = Gm.get(args[0])) == null) {
				sendUsage(label);
				return false;
			}
		if (target == null) {
			String targetName;
			if (args.length == 0) {
				sendUsage(label);
				return false;
			} else if (args.length == 1)
				targetName = args[0];
			else
				targetName = args[1];
			target = Bukkit.getPlayer(targetName);
			if (target == null) {
				sendUnknownPlayer(targetName);
				return false;
			}
		}
		if (gm == Gm.CREATIVE && !hasPermission(OlympaCorePermissions.GAMEMODE_COMMAND_CREATIVE)) {
			sendDoNotHavePermission();
			return false;
		}
		String oldGamemode = Gm.get(target.getGameMode()).getName();
		if (gm.isGameMode(target.getGameMode())) {
			if (target != player)
				sendMessage(Prefix.DEFAULT_BAD, "&4%s&c est déjà en gamemode &4%s&c.", target.getName(), oldGamemode);
			else
				Prefix.DEFAULT_BAD.sendMessage(target, "&cTu es déjà en gamemode &4%s&c.", oldGamemode);
			return false;
		}
		target.setGameMode(gm.getGameMode());
		if (target != player)
			sendMessage(Prefix.DEFAULT_GOOD, "&2%s&a est désormais en gamemode &2%s&a(avant %s).", target.getName(), gm.getName(), oldGamemode);
		Prefix.DEFAULT_GOOD.sendMessage(target, "&aTu es désormais en gamemode &2%s&a.", gm.getName());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}