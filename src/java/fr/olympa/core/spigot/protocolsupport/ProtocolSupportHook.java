package fr.olympa.core.spigot.protocolsupport;

import java.util.Collection;
import java.util.stream.Collectors;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.hook.ProtocolAction;
import protocolsupport.ProtocolSupport;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;

public class ProtocolSupportHook implements ProtocolAction {

	private ProtocolSupport protocolSupport;

	public ProtocolSupportHook(Plugin plugin) {
		protocolSupport = (ProtocolSupport) plugin.getServer().getPluginManager().getPlugin("ProtocolSupport");
		disable1_7();
	}

	public void disable(ProtocolVersion minProtocol) {
		for (ProtocolVersion protocol : ProtocolVersion.getAllBeforeI(minProtocol)) {
			if (protocol.isSupported()) {
				ProtocolSupportAPI.disableProtocolVersion(protocol);
			}
		}
	}

	@Override
	public void disable1_6() {
		if (protocolSupport != null) {
			disable(ProtocolVersion.MINECRAFT_1_6_4);
		}
	}

	@Override
	public void disable1_7() {
		if (protocolSupport != null) {
			disable(ProtocolVersion.MINECRAFT_1_7_10);
		}
	}

	@Override
	public void disable1_8() {
		if (protocolSupport != null) {
			disable(ProtocolVersion.MINECRAFT_1_8);
		}
	}

	public ProtocolSupport getProtocolSupport() {
		return protocolSupport;
	}

	public Collection<ProtocolVersion> getProtocolSupported() {
		if (protocolSupport != null) {
			return ProtocolSupportAPI.getEnabledProtocolVersions();
		}
		return null;
	}

	public String getVersionSupported() {
		Collection<ProtocolVersion> proto = getProtocolSupported();
		ProtocolVersion last = proto.iterator().next();
		String nameLast = last.getName();
		if (nameLast.length() >= 5) {
			nameLast = nameLast.substring(0, nameLast.length() - 3);
		}
		String lastName = nameLast;
		return proto.stream().map(p -> {
			String name = new String(p.getName());
			if (name.length() >= 5) {
				name = name.substring(0, name.length() - 3);
				if (!lastName.equals(name)) {
					return name;
				}
			}
			return p.getName();
		}).distinct().collect(Collectors.joining(", "));
	}
}
