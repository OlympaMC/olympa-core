package fr.olympa.core.spigot.chat;

import org.bukkit.entity.Player;

public class FakeMsg {

	String format;
	String playerName;
	String msg;

	public FakeMsg(String format, String playerName, String msg) {
		this.format = format;
		this.playerName = playerName;
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void send(Player player) {
		String finalMsg = format.replaceFirst("%s", playerName);
		finalMsg = finalMsg.replaceFirst("%s", msg);
		player.sendMessage(finalMsg);
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
}
