package fr.olympa.core.spigot.scoreboards.packets;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import fr.olympa.core.spigot.scoreboards.NametagManager;
import fr.olympa.core.spigot.scoreboards.utils.UtilsScoreboard;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PacketWrapper {

	private static Constructor<?> chatComponentText;
	private static Class<? extends Enum> typeEnumChatFormat;

	static {
		try {
			if (!PacketAccessor.isLegacyVersion()) {
				String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

				Class<?> typeChatComponentText = Class.forName("net.minecraft.server." + version + ".ChatComponentText");
				chatComponentText = typeChatComponentText.getConstructor(String.class);
				typeEnumChatFormat = (Class<? extends Enum>) Class.forName("net.minecraft.server." + version + ".EnumChatFormat");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String error;

	private Object packet = PacketAccessor.createPacket();

	public PacketWrapper(String name, int param, List<String> members) {
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
					String colorCode = null;
					String color = ChatColor.getLastColors(prefix);
					PacketAccessor.PREFIX.set(packet, chatComponentText.newInstance(prefix));
					if (!color.isEmpty()) {
						colorCode = color.substring(color.length() - 1);
						String chatColor = ChatColor.getByChar(colorCode).name();

						if (chatColor.equalsIgnoreCase("MAGIC"))
							chatColor = "OBFUSCATED";

						Enum<?> colorEnum = Enum.valueOf(typeEnumChatFormat, chatColor);
						PacketAccessor.TEAM_COLOR.set(packet, colorEnum);
					}
					if (colorCode != null)
						suffix = ChatColor.getByChar(colorCode) + suffix;
					PacketAccessor.SUFFIX.set(packet, chatComponentText.newInstance(suffix));
					PacketAccessor.DISPLAY_NAME.set(packet, chatComponentText.newInstance(name));
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

	public void send() {
		PacketAccessor.sendPacket(UtilsScoreboard.getOnline(), packet);
	}

	public void send(Collection<? extends Player> players) {
		PacketAccessor.sendPacket(players, packet);
	}

	public void send(Player player) {
		PacketAccessor.sendPacket(player, packet);
	}

	private void setupDefaults(String name, int param) {
		try {
			PacketAccessor.TEAM_NAME.set(packet, name);
			PacketAccessor.PARAM_INT.set(packet, param);

			if (NametagManager.DISABLE_PUSH_ALL_TAGS && PacketAccessor.PUSH != null)
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