package fr.tristiisch.olympa.api;

import org.bukkit.ChatColor;

import fr.tristiisch.olympa.api.utils.SpigotUtils;

public enum Prefix {

	DEFAULT("&6%serverName &7%symbole "),
	DEFAULT_BAD("&6%serverName &7%symbole &c", ChatColor.RED, ChatColor.DARK_RED),
	DEFAULT_GOOD("&6%serverName &7%symbole &a", ChatColor.GREEN, ChatColor.DARK_GREEN),
	BAD("&c✕ ", ChatColor.RED, ChatColor.DARK_RED),
	INFO("&6INFO &6%symbole &e", ChatColor.YELLOW, ChatColor.GOLD),
	USAGE("&6Usage &7%symbole &c", ChatColor.RED, ChatColor.DARK_RED);

	static {
		for (final Prefix prefix : Prefix.values()) {
			prefix.setPrefix(prefix.toStringWithoutFormat().replaceAll("%serverName", "Olympa").replaceAll("%symbole", "➤"));
		}
	}

	String prefix;
	ChatColor color;
	ChatColor color2;

	private Prefix(final String prefix) {
		this.prefix = prefix;
	}

	private Prefix(String prefix, ChatColor color) {
		this.prefix = prefix;
		this.color = color;
	}

	private Prefix(String prefix, ChatColor color, ChatColor color2) {
		this.prefix = prefix;
		this.color = color;
		this.color2 = color2;
	}

	public ChatColor getColor() {
		return this.color;
	}

	public ChatColor getColor2() {
		return this.color2;
	}

	private void setPrefix(final String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String toString() {
		return SpigotUtils.color(this.prefix);
	}

	public String toStringWithoutFormat() {
		return this.prefix;
	}
}
