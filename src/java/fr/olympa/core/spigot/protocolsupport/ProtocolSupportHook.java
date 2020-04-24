package fr.olympa.core.spigot.protocolsupport;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.hook.ProtocolAction;
import protocolsupport.ProtocolSupport;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolType;
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

	public String getBigVersion(String version) {
		Matcher matcher = Pattern.compile("\\d+.\\d+").matcher(version);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
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

	public String getRangeVersion() {
		ProtocolVersion last = ProtocolVersion.getLatest(ProtocolType.PC);
		ProtocolVersion[] all = ProtocolVersion.getAllBeforeE(last);
		ProtocolVersion old = all[all.length - 1];
		String oldBigVersion = getBigVersion(old.getName());
		return oldBigVersion + " à " + last.getName();
	}

	public String getVersionSupported() {
		Collection<ProtocolVersion> proto = getProtocolSupported();
		ProtocolVersion last = ProtocolVersion.getLatest(ProtocolType.PC);
		String lastMajorVersion = getBigVersion(last.getName());
		return proto.stream().map(p -> {
			String name = getBigVersion(p.getName());
			if (lastMajorVersion.startsWith(name)) {
				return p.getName();
			}
			return name;
		}).distinct().collect(Collectors.joining(", "));
	}
}
