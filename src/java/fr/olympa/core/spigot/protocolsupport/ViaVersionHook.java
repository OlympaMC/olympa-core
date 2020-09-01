package fr.olympa.core.spigot.protocolsupport;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.utils.spigot.ProtocolAPI;
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
		ViaAPI<?> api = Via.getAPI();
		SortedSet<Integer> versions = api.getSupportedVersions();
		Iterator<Integer> iterator = versions.iterator();
		Integer version = null;
		while (iterator.hasNext())
			version = iterator.next();
		return ProtocolAPI.getAll(version).stream().map(p -> p.getName()).collect(Collectors.joining(", "));
	}

	public String getVersionSupported() {
		ViaAPI<?> api = Via.getAPI();
		return api.getSupportedVersions().stream().map(ver -> ProtocolAPI.getAll(ver).stream().map(p -> p.name()).collect(Collectors.joining(", "))).distinct().collect(Collectors.joining(", "));
	}
}
