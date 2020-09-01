package fr.olympa.core.spigot.protocolsupport;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.utils.VersionNameComparator;
import fr.olympa.api.utils.spigot.ProtocolAPI;
import fr.olympa.core.spigot.OlympaCore;
import protocolsupport.ProtocolSupport;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolType;
import protocolsupport.api.ProtocolVersion;

public class ProtocolSupportHook implements IProtocolSupport {

	private ProtocolSupport protocolSupport;

	public ProtocolSupportHook(Plugin plugin) {
		protocolSupport = (ProtocolSupport) plugin.getServer().getPluginManager().getPlugin("ProtocolSupport");
		disable1_7();
	}

	public void disable(ProtocolVersion minProtocol) {
		for (ProtocolVersion protocol : ProtocolVersion.getAllBeforeI(minProtocol))
			if (protocol.isSupported())
				ProtocolSupportAPI.disableProtocolVersion(protocol);
	}

	@Override
	public void disable1_6() {
		if (protocolSupport != null)
			disable(ProtocolVersion.MINECRAFT_1_6_4);
	}

	@Override
	public void disable1_7() {
		if (protocolSupport != null)
			disable(ProtocolVersion.MINECRAFT_1_7_10);
	}

	@Override
	public void disable1_8() {
		if (protocolSupport != null)
			disable(ProtocolVersion.MINECRAFT_1_8);
	}

	@Override
	public String getBigVersion(String version) {
		Matcher matcher = Pattern.compile("\\d+.\\d+").matcher(version);
		if (matcher.find())
			return matcher.group();
		return null;
	}

	public ProtocolSupport getProtocolSupport() {
		return protocolSupport;
	}

	class ProtocolVersionComparator implements Comparator<ProtocolVersion> {
		@Override
		public int compare(ProtocolVersion o1, ProtocolVersion o2) {
			return o1.getId() - o2.getId();
		}
	}

	public List<ProtocolVersion> getProtocolSupported() {
		return protocolSupport == null ? null : getStreamProtocolSupported().collect(Collectors.toList());
	}

	public Stream<ProtocolVersion> getStreamProtocolSupported() {
		return protocolSupport == null ? null : ProtocolSupportAPI.getEnabledProtocolVersions().stream().sorted(new ProtocolVersionComparator());
	}

	@Override
	public String getRangeVersion() {
		List<String> proto = getStreamProtocolSupported().map(ProtocolVersion::getName).collect(Collectors.toList());
		String last = proto.get(0);
		String first;
		if (OlympaCore.getInstance().getViaVersionHook() == null)
			first = proto.get(proto.size() - 1);
		else
			first = OlympaCore.getInstance().getViaVersionHook().getHighVersion();
		return last + " à " + first;
	}

	@Override
	public String getVersionUnSupportedInRange() {
		List<ProtocolVersion> proto = getProtocolSupported();
		ProtocolVersion last = proto.get(0);
		ProtocolVersion first = proto.get(proto.size() - 1);
		List<String> protoAll = proto.stream().filter(pv -> pv.getId() > first.getId() && pv.getId() < last.getId() && !proto.contains(pv)).map(ProtocolVersion::getName).collect(Collectors.toList());
		return String.join(", ", protoAll);

	}

	@Override
	public String getVersionSupported() {
		List<ProtocolVersion> proto = getProtocolSupported();
		ProtocolVersion last = ProtocolVersion.getLatest(ProtocolType.PC);
		String lastMajorVersion = getBigVersion(last.getName());
		return proto.stream().map(p -> {
			String name = getBigVersion(p.getName());
			if (lastMajorVersion.startsWith(name))
				return p.getName();
			return name;
		}).distinct().sorted(new VersionNameComparator()).collect(Collectors.joining(", "));
	}

	@Override
	public ProtocolAPI getPlayerVersion(Player p) {
		if (protocolSupport != null)
			return ProtocolAPI.get(ProtocolSupportAPI.getProtocolVersion(p).getId());
		return ProtocolAPI.getDefaultProtocol();
	}
}
