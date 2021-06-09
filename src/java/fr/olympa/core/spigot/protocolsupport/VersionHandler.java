package fr.olympa.core.spigot.protocolsupport;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.core.spigot.OlympaCore;
import protocolsupport.api.ProtocolVersion;

public class VersionHandler {

	private ProtocolSupportHook protocolSupport;
	private ViaVersionHook viaVersion;

	// TODO rendre l'api des version dans ProtocolSupportHook indépendant de ProtocolSupport pour l'utiliser avec ViaVersion sans ProtocolSupport

	public VersionHandler(OlympaCore plugin) {
		if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolSupport"))
			protocolSupport = new ProtocolSupportHook(plugin);
		if (plugin.getServer().getPluginManager().isPluginEnabled("ViaVersion"))
			viaVersion = new ViaVersionHook(plugin);
		String[] versions = getRangeVersionArray();
		if (versions != null) {
			plugin.setFirstVersion(versions[versions.length - 1]);
			plugin.setLastVersion(versions[0]);
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
		if (protocolSupport != null)
			protocol = protocolSupport.getPlayerVersion(player);
		else if (viaVersion != null)
			protocol = viaVersion.getPlayerVersion(player);
		else
			protocol = ProtocolAPI.getDefaultSpigotProtocol();
		return protocol;
	}

	public String[] getRangeVersionArray() {
		String first = "unknown";
		String last = "unknown";
		if (protocolSupport != null) {
			List<String> proto = protocolSupport.getStreamProtocolSupported().map(ProtocolVersion::getName).collect(Collectors.toList());
			first = proto.get(0);
			last = proto.get(proto.size() - 1);
		} else
			first = ProtocolAPI.getSpigotVersion();
		if (viaVersion != null)
			last = viaVersion.getHighVersion();
		else
			last = ProtocolAPI.getSpigotVersion();
		return new String[] { last, first };
	}
}
