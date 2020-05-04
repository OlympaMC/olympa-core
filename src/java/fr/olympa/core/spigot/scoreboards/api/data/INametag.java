package fr.olympa.core.spigot.scoreboards.api.data;

public interface INametag {
	String getPrefix();

	int getSortPriority();

	String getSuffix();

	boolean isPlayerTag();
}