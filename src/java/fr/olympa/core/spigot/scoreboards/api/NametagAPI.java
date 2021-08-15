package fr.olympa.core.spigot.scoreboards.api;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;

import fr.olympa.api.common.module.OlympaModule.ModuleApi;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.scoreboard.tab.INametagApi;
import fr.olympa.api.spigot.scoreboard.tab.Nametag;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.scoreboards.NameTagManager;
import fr.olympa.core.spigot.scoreboards.packets.PacketWrapper;
import fr.olympa.core.spigot.versionhook.VersionHook;

/**
 * Implements the INametagAPI interface. There only exists one instance of this
 * class.
 */
public final class NametagAPI implements INametagApi, ModuleApi<OlympaCore> {

	public NameTagManager manager;
	private NametagHandler defaultHandler;
	private List<Entry<EventPriority, NametagHandler>> handlers = new ArrayList<>();

	@Override
	public boolean enable(OlympaCore plugin) {
		manager = new NameTagManager();
		handlers = new ArrayList<>();
		defaultHandler = new NametagHandler() {
			@Override
			public void updateNameTag(Nametag nametag, OlympaPlayer op, OlympaPlayer to) {
				nametag.appendPrefix(op.getGroupPrefix().stripTrailing());
			}

			@Override
			public boolean needsDatas() {
				return false;
			}
			
			@Override
			public Integer getPriority(OlympaPlayer player) {
				return player.getGroup().getIndex();
			}
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
	public void callNametagUpdate(OlympaPlayer player, Collection<? extends OlympaPlayer> toPlayers, boolean withDatas) {
		if (manager == null) {
			OlympaCore.getInstance().sendMessage("&cModule NameTag &4désactiver &8> &cImpossible de mettre à jour le nameTag de &4%s&7.", player.getName());
			return;
		}
		if (player.getPlayer() == null || !((Player) player.getPlayer()).isOnline()) {
			OlympaCore.getInstance().sendMessage("§cTentative de mise à jour du nametag du joueur hors-ligne §4%s", player.getName());
			return;
		}
		Map<Nametag, List<Player>> nametags = new HashMap<>();
		int priority = 0;
		for (OlympaPlayer to : toPlayers) {
			if (to.getPlayer() == null || !((Player) to.getPlayer()).isOnline())
				continue;
			Nametag tag = new Nametag();
			for (Entry<EventPriority, NametagHandler> handlerEntry : handlers)
				try {
					NametagHandler handler = handlerEntry.getValue();
					if (!handler.needsDatas() || withDatas) {
						handler.updateNameTag(tag, player, to);
						Integer priorityNullable = handler.getPriority(player);
						if (priorityNullable != null) priority = priorityNullable.intValue();
					}
				} catch (Exception ex) {
					OlympaCore.getInstance().sendMessage("§cUne erreur est survenue lors de la mise à jour du nametag de %s", player.getName());
					ex.printStackTrace();
				}
			List<Player> similarTags = nametags.get(tag);
			if (similarTags == null) {
				similarTags = new ArrayList<>();
				nametags.put(tag, similarTags);
			}
			similarTags.add((Player) to.getPlayer());
		}
		VersionHook versionHandler = OlympaCore.getInstance().getVersionHandler();
		nametags.forEach((tag, players) -> {
			Map<Boolean, List<Player>> ps = players.stream().collect(Collectors.partitioningBy(p -> versionHandler.isPlayerVersionUnder(p, ProtocolAPI.V1_16)));
			List<Player> playersUnder1_16 = ps.get(false);
			List<Player> playersUpper1_16 = ps.get(true);
			if (!playersUpper1_16.isEmpty())
				manager.changeFakeNametag(player.getName(), tag, player.getGroup().getIndex(), playersUpper1_16);
			if (!playersUnder1_16.isEmpty())
				manager.changeFakeNametag(player.getName(), tag.removeRBG(), player.getGroup().getIndex(), playersUnder1_16);
		});
		int finalPriority = priority;
		nametags.forEach((tag, players) -> manager.changeFakeNametag(player.getName(), tag, finalPriority, players));
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