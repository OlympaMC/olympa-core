package fr.olympa.core.spigot.protocolsupport;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.spigot.utils.SpigotInfoFork;
import fr.olympa.core.spigot.OlympaCore;
import protocolsupport.api.ProtocolVersion;

public class VersionHandler {

	private ProtocolSupportHook protocolSupport;
	private ViaVersionHook viaVersion;

	// TODO rendre l'api des version dans ProtocolSupportHook indépendant de ProtocolSupport pour l'utiliser avec ViaVersion sans ProtocolSupport

	public VersionHandler(OlympaCore plugin) {
		try {
			if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolSupport"))
				protocolSupport = new ProtocolSupportHook(plugin);
		} catch (NoClassDefFoundError e) {
			plugin.getLogger().severe("Impossible de récupérer la class principal de ProtocolSupport. Vérifie sa version, et si le package n'a pas été modifié.");
			e.printStackTrace();
		}
		try {
			if (plugin.getServer().getPluginManager().isPluginEnabled("ViaVersion"))
				viaVersion = new ViaVersionHook(plugin);
		} catch (NoClassDefFoundError e) {
			plugin.getLogger().severe("Impossible de récupérer la class principal de ViaVersion. Vérifie sa version, et si le package n'a pas été modifié.");
			e.printStackTrace();
		}
		Entry<String, String> versions = getRangeVersionArray();
		if (versions != null) {
			plugin.setFirstVersion(versions.getKey());
			plugin.setLastVersion(versions.getValue());
		}
	}

	public ProtocolSupportHook getProtocolSupport() {
		return protocolSupport;
	}

	public ViaVersionHook getViaVersion() {
		return viaVersion;
	}

	public ProtocolAPI getVersion(Player player) {
		ProtocolAPI protocol;
		if (SpigotInfoFork.isPaper())
			protocol = ProtocolAPI.get(player.getProtocolVersion());
		else if (viaVersion != null)
			protocol = viaVersion.getPlayerVersion(player);
		else if (protocolSupport != null)
			protocol = protocolSupport.getPlayerVersion(player);
		else
			protocol = ProtocolAPI.getDefaultSpigotProtocol();
		return protocol;
	}

	public Entry<String, String> getRangeVersionArray() {
		String first = "unknown";
		String last = "unknown";
		if (protocolSupport != null) {
			List<String> proto = protocolSupport.getStreamProtocolSupported().map(ProtocolVersion::getName).collect(Collectors.toList());
			first = proto.get(0);
			last = proto.get(proto.size() - 1);
		} else
			first = ProtocolAPI.getNativeSpigotVersion();
		if (viaVersion != null)
			last = viaVersion.getHighVersion();
		else
			last = ProtocolAPI.getNativeSpigotVersion();
		return new AbstractMap.SimpleEntry<>(first, last);
	}
}
