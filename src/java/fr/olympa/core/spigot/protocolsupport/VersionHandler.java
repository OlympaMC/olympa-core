package fr.olympa.core.spigot.protocolsupport;

import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.utils.spigot.ProtocolAPI;
import fr.olympa.core.spigot.OlympaCore;
import protocolsupport.api.ProtocolVersion;

public class VersionHandler {

	private ProtocolSupportHook protocolSupport;
	private ViaVersionHook viaVersion;

	// TODO rendre l'api des version dans ProtocolSupportHook indépendant de ProtocolSupport pour l'utiliser avec ViaVersion sans ProtocolSupport

	public VersionHandler(OlympaCore plugin) {
		protocolSupport = new ProtocolSupportHook(plugin);
		viaVersion = new ViaVersionHook(plugin);
		String[] versions = getRangeVersionArray();
		if (versions != null) {
			plugin.setFirstVersion(versions[0]);
			plugin.setLastVersion(versions[versions.length - 1]);
		}
	}

	public ProtocolSupportHook getProtocolSupport() {
		return protocolSupport;
	}

	public ViaVersionHook getViaVersion() {
		return viaVersion;
	}

	public String[] getRangeVersionArray() {
		String first = "unknown";
		String last = "unknown";
		if (protocolSupport != null) {
			List<String> proto = protocolSupport.getStreamProtocolSupported().map(ProtocolVersion::getName).collect(Collectors.toList());
			first = proto.get(0);
			last = proto.get(proto.size() - 1);
		} else
			first = ProtocolAPI.getVersion();
		if (viaVersion != null)
			last = viaVersion.getHighVersion();
		return new String[] { first, last };
	}
}
