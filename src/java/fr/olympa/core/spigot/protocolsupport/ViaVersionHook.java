package fr.olympa.core.spigot.protocolsupport;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.spigot.utils.ProtocolAPI;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.Via;
import us.myles.ViaVersion.api.ViaAPI;

public class ViaVersionHook {

	private ViaVersionPlugin viaVersion;

	public ViaVersionHook(Plugin plugin) {
		viaVersion = (ViaVersionPlugin) plugin.getServer().getPluginManager().getPlugin("ViaVersion");
	}

	public String getBigVersion(String version) {
		Matcher matcher = Pattern.compile("\\d+.\\d+").matcher(version);
		if (matcher.find())
			return matcher.group();
		return null;
	}

	public ViaVersionPlugin getViaVersion() {
		return viaVersion;
	}

	public String getHighVersion() {
		if (viaVersion == null)
			return null;
		ViaAPI<?> api = Via.getAPI();
		SortedSet<Integer> versions = api.getSupportedVersions();
		Iterator<Integer> iterator = versions.iterator();
		Integer protocol = null;
		String version = null;
		while (iterator.hasNext())
			protocol = iterator.next();
		if (protocol != null) {
			version = ProtocolAPI.getAll(protocol).stream().map(ProtocolAPI::getName).collect(Collectors.joining(", "));
			if (version == null)
				return "unknown";
		}

		return version;
	}

	public ProtocolAPI getPlayerVersion(Player player) {
		if (viaVersion == null)
			return null;
		return ProtocolAPI.get(Via.getAPI().getPlayerVersion(player));
	}

	public String getVersionSupported() {
		if (viaVersion == null)
			return null;
		ViaAPI<?> api = Via.getAPI();
		return api.getSupportedVersions().stream().map(ver -> ProtocolAPI.getAll(ver).stream().map(ProtocolAPI::getName).collect(Collectors.joining(", "))).distinct().collect(Collectors.joining(", "));
	}
}
