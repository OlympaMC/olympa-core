package fr.olympa.core.spigot.versionhook;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.spigot.version.PluginHandleVersion;
import protocolsupport.ProtocolSupport;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;

public class ProtocolSupportHook implements PluginHandleVersion {

	private ProtocolSupport protocolSupport;

	protected ProtocolSupportHook(Plugin plugin) throws NoClassDefFoundError {
		protocolSupport = (ProtocolSupport) plugin.getServer().getPluginManager().getPlugin("ProtocolSupport");
		disable1_7();
	}

	@Deprecated(forRemoval = true, since = "20/05/2020")
	public void disable1_6() {
		disableAllUnderI(ProtocolAPI.V1_16_4);
	}

	@Deprecated(forRemoval = true, since = "20/05/2020")
	public void disable1_7() {
		disableAllUnderI(ProtocolAPI.V1_7_10);
	}

	@Deprecated(forRemoval = true, since = "20/05/2020")
	public void disable1_8() {
		disableAllUnderI(ProtocolAPI.V1_8_9);
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
	public Entry<String, String> getRangeVersionArray() {
		List<String> proto = getStreamProtocolSupported().map(ProtocolVersion::getName).collect(Collectors.toList());
		String first = proto.get(proto.size() - 1);
		String last = proto.get(0);
		return new AbstractMap.SimpleEntry<>(first, last);
	}

	@Override
	public List<Integer> getEnabledProtocols() {
		return getStreamProtocolSupported().map(ProtocolVersion::getId).collect(Collectors.toList());
	}

	@Override
	public List<Integer> getDisabledProtocols() {
		return Arrays.stream(ProtocolVersion.getAllSupported()).filter(protocol -> !ProtocolSupportAPI.isProtocolVersionEnabled(protocol)).map(ProtocolVersion::getId).collect(Collectors.toList());
	}

	@Override
	public int getPlayerProtocol(Player player) {
		return ProtocolSupportAPI.getProtocolVersion(player).getId();
	}

	@Override
	public boolean disable(ProtocolAPI... versions) {
		List<ProtocolAPI> disabledProtocols = new ArrayList<>();
		for (ProtocolAPI ver : versions)
			if (disable(ver))
				disabledProtocols.add(ver);
		LinkSpigotBungee.getInstance().sendMessage("Disabling %s support by ProtocolSupport.", disabledProtocols.stream().map(ProtocolAPI::getCompleteName).distinct().collect(Collectors.joining(", ")));
		return !disabledProtocols.isEmpty();
	}

	@Override
	public boolean disableAllUnderI(ProtocolAPI version) {
		return disable(ProtocolVersion.getAllBeforeI(getProtocolVersion(version)));
	}

	@Override
	public boolean disableAllUpperI(ProtocolAPI version) {
		return disable(ProtocolVersion.getAllAfterI(getProtocolVersion(version)));
	}

	@Override
	public boolean disable(ProtocolAPI version) {
		ProtocolVersion protocol = getProtocolVersion(version);
		boolean isSomeDisble = disable(protocol);
		if (isSomeDisble)
			LinkSpigotBungee.getInstance().sendMessage("Disabling %s support by ProtocolSupport.", protocol.getName());
		return isSomeDisble;
	}

	public boolean disable(ProtocolVersion protocol) {
		if (protocol != null && protocol.isSupported() && ProtocolSupportAPI.isProtocolVersionEnabled(protocol)) {
			ProtocolSupportAPI.disableProtocolVersion(protocol);
			return true;
		}
		return false;
	}

	public boolean disable(ProtocolVersion... protocols) {
		return disable(Arrays.asList(protocols));
	}

	public boolean disable(Iterable<ProtocolVersion> protocols) {
		List<ProtocolVersion> disabledProtocols = new ArrayList<>();
		for (ProtocolVersion protocol : protocols)
			if (disable(protocol))
				disabledProtocols.add(protocol);
		LinkSpigotBungee.getInstance().sendMessage("Disabling %s support by ProtocolSupport.", disabledProtocols.stream().map(ProtocolVersion::getName).collect(Collectors.joining(", ")));
		return !disabledProtocols.isEmpty();
	}

	public ProtocolVersion getProtocolVersion(ProtocolAPI version) {
		return Arrays.stream(ProtocolVersion.values()).filter(pVersion -> pVersion.getId() == version.getProtocolNumber()).findFirst().orElse(null);
	}
}
