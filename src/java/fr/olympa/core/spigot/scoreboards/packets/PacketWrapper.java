package fr.olympa.core.spigot.scoreboards.packets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.spigot.scoreboard.tab.FakeTeam;
import fr.olympa.core.spigot.module.CoreModules;
import fr.olympa.core.spigot.scoreboards.NameTagManager;
import fr.olympa.core.spigot.scoreboards.utils.UtilsScoreboard;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PacketWrapper {

	private static Class<? extends Enum> typeEnumChatFormat;

	static {
		try {
			if (!PacketAccessor.isLegacyVersion()) {
				String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
				typeEnumChatFormat = (Class<? extends Enum>) Class.forName("net.minecraft.server." + version + ".EnumChatFormat");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static PacketWrapper delete(FakeTeam team) {
		if (!team.isValidTeam())
			throw new IllegalAccessError("FakeTeam team is not valid : " + new Gson().toJson(team));
		if (CoreModules.NAME_TAG.isDebugEnabled())
			LinkSpigotBungee.getInstance().sendMessage("Team &cDelete&6 %s '%s' '%s' for %s", team.getName(), team.getPrefix(), team.getSuffix(), team.getMembers() != null ? String.join(", ", team.getMembers()) : null);
		FakeTeam.removeId(team);
		return new PacketWrapper(team.getName(), 1);
	}

	public static PacketWrapper create(FakeTeam team) {
		if (!team.isValidTeam())
			throw new IllegalAccessError("FakeTeam team is not valid : " + new Gson().toJson(team));
		if (CoreModules.NAME_TAG.isDebugEnabled())
			LinkSpigotBungee.getInstance().sendMessage("Team &2Create&6 %s '%s' '%s' for %s", team.getName(), team.getPrefix(), team.getSuffix(), String.join(", ", team.getMembers()));
		return new PacketWrapper(team.getName(), team.getPrefix(), team.getSuffix(), 0, team.getMembers());
	}
	//
	//	public static PacketWrapper update(FakeTeam team) {
	//		return new PacketWrapper(team.getName(), team.getPrefix(), team.getSuffix(), 2);
	//	}

	public static PacketWrapper addMember(FakeTeam team, List<String> members) {
		if (!team.isValidTeam())
			throw new IllegalAccessError("FakeTeam team is not valid : " + new Gson().toJson(team));
		if (CoreModules.NAME_TAG.isDebugEnabled())
			LinkSpigotBungee.getInstance().sendMessage("Team &2add member&6 %s '%s' '%s' for %s", team.getName(), team.getPrefix(), team.getSuffix(), String.join(", ", members));
		return new PacketWrapper(team.getName(), 3, members);
	}

	public static PacketWrapper removeMember(FakeTeam team, List<String> members) {
		if (!team.isValidTeam())
			throw new IllegalAccessError("FakeTeam team is not valid : " + new Gson().toJson(team));
		if (CoreModules.NAME_TAG.isDebugEnabled())
			LinkSpigotBungee.getInstance().sendMessage("Team &cremove member&6 %s '%s' '%s' for %s", team.getName(), team.getPrefix(), team.getSuffix(), String.join(", ", members));
		return new PacketWrapper(team.getName(), 4, members);
	}

	public String error;

	private Object packet = PacketAccessor.createPacket();

	private PacketWrapper(String name, int param, List<String> members) {
		if (param != 3 && param != 4)
			throw new IllegalArgumentException("Method must be join or leave for player constructor");
		setupDefaults(name, param);
		setupMembers(members);
	}

	public PacketWrapper(String name, String prefix, String suffix, int param, Collection<?> players) {
		setupDefaults(name, param);
		if (param == 0 || param == 2)
			try {
				if (PacketAccessor.isLegacyVersion()) {
					PacketAccessor.DISPLAY_NAME.set(packet, name);
					PacketAccessor.PREFIX.set(packet, prefix);
					PacketAccessor.SUFFIX.set(packet, suffix);
				} else {
					net.md_5.bungee.api.ChatColor color = ColorUtils.getLastColor(prefix);
					PacketAccessor.PREFIX.set(packet, toComponent(prefix));
					if (color != null) {
						String colorCode = null;
						if (color.toString().length() == 14)
							colorCode = ColorUtils.getNearestForLegacyColor(color).name();
						else
							colorCode = color.name();
						if (colorCode.equalsIgnoreCase("MAGIC"))
							colorCode = "OBFUSCATED";
						Enum<?> colorEnum = Enum.valueOf(typeEnumChatFormat, colorCode);
						PacketAccessor.TEAM_COLOR.set(packet, colorEnum);
						suffix = color.toString() + suffix;
					}
					PacketAccessor.SUFFIX.set(packet, toComponent(suffix));
					PacketAccessor.DISPLAY_NAME.set(packet, toComponent(name));
				}
				PacketAccessor.PACK_OPTION.set(packet, 1);
				if (PacketAccessor.VISIBILITY != null)
					PacketAccessor.VISIBILITY.set(packet, "always");
				if (param == 0)
					((Collection) PacketAccessor.MEMBERS.get(packet)).addAll(players);
			} catch (Exception e) {
				error = e.getMessage();
			}
	}

	private PacketWrapper(String name, int param) {
		if (param != 1)
			throw new IllegalArgumentException("Method must be remove team");
		setupDefaults(name, param);
	}

	private IChatBaseComponent toComponent(String string) {
		return IChatBaseComponent.ChatSerializer.a(ComponentSerializer.toString(new TextComponent(TextComponent.fromLegacyText(string))));
	}

	public void send() {
		PacketAccessor.sendPacket(UtilsScoreboard.getOnline(), packet);
	}

	public void send(Collection<? extends Player> players) {
		if (CoreModules.NAME_TAG.isDebugEnabled())
			LinkSpigotBungee.getInstance().sendMessage("To players %s", ColorUtils.joinPlayer('a', '2', players));
		PacketAccessor.sendPacket(players, packet);
	}

	public void send(Player player) {
		if (CoreModules.NAME_TAG.isDebugEnabled())
			LinkSpigotBungee.getInstance().sendMessage("To player &2%s", player.getName());
		PacketAccessor.sendPacket(player, packet);
	}

	private void setupDefaults(String name, int param) {
		try {
			PacketAccessor.TEAM_NAME.set(packet, name);
			PacketAccessor.PARAM_INT.set(packet, param);

			if (NameTagManager.DISABLE_PUSH_ALL_TAGS && PacketAccessor.PUSH != null)
				PacketAccessor.PUSH.set(packet, "never");
		} catch (Exception e) {
			error = e.getMessage();
		}
	}

	private void setupMembers(Collection<?> players) {
		try {
			players = players == null || players.isEmpty() ? new ArrayList<>() : players;
			((Collection) PacketAccessor.MEMBERS.get(packet)).addAll(players);
		} catch (Exception e) {
			error = e.getMessage();
		}
	}

}