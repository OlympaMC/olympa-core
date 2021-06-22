package fr.olympa.core.bungee.motd;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.common.chat.ColorUtils;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.PlayerInfo;

public class PlayerInfoBuilder {

	String prefix = "&e--------------&6 Olympa &e--------------";
	String suffix = "&e-----------------------------------";

	List<String> stringBuilder;

	public PlayerInfoBuilder() {
		stringBuilder = new ArrayList<>();
		append(prefix);
	}

	public PlayerInfoBuilder append(String s) {
		stringBuilder.add(ColorUtils.color(s) + "§r");
		return this;
	}

	public PlayerInfoBuilder append() {
		stringBuilder.add("");
		return this;
	}

	public PlayerInfo[] build() {
		if (!stringBuilder.get(stringBuilder.size() - 1).equals(suffix))
			append(suffix);
		return stringBuilder.stream().map(s -> new ServerPing.PlayerInfo(s, UUID.randomUUID())).toArray(PlayerInfo[]::new);
	}
}
