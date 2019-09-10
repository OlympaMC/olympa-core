package fr.tristiisch.olympa.core.permission.groups.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import fr.tristiisch.olympa.api.objects.OlympaGroup;
import fr.tristiisch.olympa.api.objects.OlympaPlayer;

public class AsyncOlympaPlayerChangeGroupEvent extends PlayerEvent {

	public enum ChangeType {
		ADD,
		REMOVE,
		SET;
	}

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	final ChangeType olympaGroupChangeType;

	final private OlympaPlayer olympaPlayer;
	final private OlympaGroup groupChange;

	public AsyncOlympaPlayerChangeGroupEvent(Player who, ChangeType olympaGroupChangeType, OlympaPlayer olympaPlayer, OlympaGroup groupChange) {
		super(who);
		this.olympaGroupChangeType = olympaGroupChangeType;
		this.olympaPlayer = olympaPlayer;
		this.groupChange = groupChange;
	}

	public ChangeType getChangeType() {
		return this.olympaGroupChangeType;
	}

	public OlympaGroup getGroupChange() {
		return this.groupChange;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public OlympaPlayer getOlympaPlayer() {
		return this.olympaPlayer;
	}
}
