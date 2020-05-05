package fr.olympa.core.bungee.api.customevent;

import fr.olympa.api.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class OlympaGroupChangeEvent extends Event {

	final private OlympaPlayer olympaPlayer;
	final private ProxiedPlayer player;
	final private OlympaGroup groupChanged;
	final private long timestamp;
	final private ChangeType state;

	public OlympaGroupChangeEvent(ProxiedPlayer player, OlympaPlayer olympaPlayer, OlympaGroup groupChanged, long timestamp, ChangeType state) {
		super();
		this.player = player;
		this.olympaPlayer = olympaPlayer;
		this.groupChanged = groupChanged;
		this.state = state;
		this.timestamp = timestamp;
	}

	public OlympaGroup getGroupChanged() {
		return groupChanged;
	}

	@SuppressWarnings("unchecked")
	public <T extends OlympaPlayer> T getOlympaPlayer() {
		return (T) olympaPlayer;
	}

	public ProxiedPlayer getPlayer() {
		return player;
	}

	public ChangeType getState() {
		return state;
	}

	public long getTimestamp() {
		return timestamp;
	}
}