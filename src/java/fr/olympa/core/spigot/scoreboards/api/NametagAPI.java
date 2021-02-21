package fr.olympa.core.spigot.scoreboards.api;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;

import fr.olympa.api.module.OlympaModule.ModuleApi;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.scoreboard.tab.Nametag;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.scoreboards.NametagManager;
import fr.olympa.core.spigot.scoreboards.packets.PacketWrapper;

/**
 * Implements the INametagAPI interface. There only exists one instance of this
 * class.
 */
public final class NametagAPI implements INametagApi, ModuleApi<OlympaCore> {

	public NametagManager manager;
	private NametagHandler defaultHandler;
	private List<Entry<EventPriority, NametagHandler>> handlers = new ArrayList<>();

	@Override
	public boolean enable(OlympaCore plugin) {
		manager = new NametagManager();
		handlers = new ArrayList<>();
		defaultHandler = (nametag, op, to) -> {
			String prefix = op.getGroupPrefix();
			nametag.appendPrefix(prefix.substring(0, prefix.length() - 1));
		};
		addNametagHandler(EventPriority.LOW, defaultHandler);
		return true;
	}

	@Override
	public boolean disable(OlympaCore plugin) {
		if (defaultHandler != null) {
			removeNametagHandler(defaultHandler);
			defaultHandler = null;
		}
		return disable();
	}

	@Override
	public boolean isEnabled() {
		return manager != null;
	}

	@Override
	public boolean setToPlugin(OlympaCore plugin) {
		plugin.setNameTagApi(this);
		return true;
	}

	private boolean disable() {
		manager = null;
		if (handlers != null)
			handlers.clear();
		return true;
	}

	@Override
	public void addNametagHandler(EventPriority priority, NametagHandler handler) {
		handlers.add(new AbstractMap.SimpleEntry<>(priority, handler));
		handlers.sort((o1, o2) -> Integer.compare(o1.getKey().getSlot(), o2.getKey().getSlot()));
		OlympaCore.getInstance().sendMessage("+1 nametag handler, priorité §6%s", priority.name());
	}

	@Override
	public void removeNametagHandler(NametagHandler handler) {
		handlers.removeIf(f -> f.getValue().equals(handler));
		OlympaCore.getInstance().sendMessage("&c-1 nametag handler §4%s", handler.getClass().getSimpleName());
	}

	@Override
	public void callNametagUpdate(OlympaPlayer player) {
		callNametagUpdate(player, AccountProvider.getAll());
	}

	@Override
	public void callNametagUpdate(OlympaPlayer player, Collection<? extends OlympaPlayer> toPlayers) {
		if (manager == null) {
			OlympaCore.getInstance().sendMessage("&cModule NameTag &4désactiver &8> &cImpossible de mettre à jour le nameTag de &4%s&7.", player.getName());
			return;
		}
		if (player.getPlayer() == null || !player.getPlayer().isOnline()) {
			OlympaCore.getInstance().sendMessage("§cTentative de mise à jour du nametag du joueur hors-ligne §4%s", player.getName());
			return;
		}
		Map<Nametag, List<Player>> nametags = new HashMap<>();
		for (OlympaPlayer to : toPlayers) {
			if (to.getPlayer() == null || !to.getPlayer().isOnline())
				continue;
			Nametag tag = new Nametag();
			for (Entry<EventPriority, NametagHandler> handlerEntry : handlers)
				try {
					NametagHandler handler = handlerEntry.getValue();
					handler.updateNameTag(tag, player, to);
				} catch (Exception ex) {
					OlympaCore.getInstance().sendMessage("§cUne erreur est survenue lors de la mise à jour du nametag de %s", player.getName());
					ex.printStackTrace();
				}
			List<Player> similarTags = nametags.get(tag);
			if (similarTags == null) {
				similarTags = new ArrayList<>();
				nametags.put(tag, similarTags);
			}
			similarTags.add(to.getPlayer());
		}
		nametags.forEach((tag, players) -> manager.changeFakeNametag(player.getName(), tag, player.getGroup().getIndex(), players));
	}

	public boolean testCompat() {
		PacketWrapper wrapper = new PacketWrapper("TEST", "&f", "", 0, new ArrayList<>());
		wrapper.send();
		if (wrapper.error == null)
			return true;
		new Exception("[WARNING] NameTag -> invalid Protocol of Team, error with reflection. NameTag won't work").printStackTrace();
		disable();
		return false;
	}

}