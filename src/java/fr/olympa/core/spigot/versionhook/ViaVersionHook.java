package fr.olympa.core.spigot.versionhook;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.ViaVersionPlugin;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.bukkit.platform.BukkitViaAPI;

import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.spigot.version.PluginHandleVersion;

public class ViaVersionHook implements PluginHandleVersion {

	private ViaVersionPlugin viaVersionPlugin;
	private BukkitViaAPI api;
	private ViaManagerImpl versionHandler;

	public ViaVersionHook(Plugin plugin) throws NoClassDefFoundError {
		viaVersionPlugin = (ViaVersionPlugin) plugin.getServer().getPluginManager().getPlugin("ViaVersion");
		api = (BukkitViaAPI) viaVersionPlugin.getApi();
		versionHandler = (ViaManagerImpl) Via.getManager();
	}

	public ViaVersionPlugin getViaVersion() {
		return viaVersionPlugin;
	}

	public BukkitViaAPI getApi() {
		return api;
	}

	@Override
	public List<Integer> getEnabledProtocols() {
		if (viaVersionPlugin == null)
			throw new UnsupportedOperationException("ViaVersion is not detected.");
		return new ArrayList<>(api.getSupportedVersions());
	}

	@Override
	public List<Integer> getDisabledProtocols() {
		if (viaVersionPlugin == null)
			throw new UnsupportedOperationException("ViaVersion is not detected.");
		return api.getFullSupportedVersions().stream().filter(version -> !api.getSupportedVersions().contains(version)).collect(Collectors.toList());
	}

	public String getHighVersion() {
		if (viaVersionPlugin == null)
			return null;
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

	@Override
	public int getPlayerProtocol(Player player) {
		if (viaVersionPlugin == null)
			throw new UnsupportedOperationException("ViaVersion is not detected.");
		return api.getPlayerVersion(player);
	}

	@Override
	public String getVersionsSupported() {
		if (viaVersionPlugin == null)
			throw new UnsupportedOperationException("ViaVersion is not detected.");
		return api.getSupportedVersions().stream().map(ver -> ProtocolAPI.getName(ver)).distinct().collect(Collectors.joining(", "));
	}

	@Override
	public Entry<String, String> getRangeVersionArray() {
		if (viaVersionPlugin == null)
			return null;
		SortedSet<Integer> versions = api.getSupportedVersions();
		if (versions.isEmpty())
			return null;
		Iterator<Integer> it = versions.iterator();
		String first = ProtocolAPI.getName(it.next());
		if (versions.size() == 1)
			return new SimpleEntry<>(first, first);
		int last = -1;
		while (it.hasNext())
			last = it.next();
		return new SimpleEntry<>(first, ProtocolAPI.getName(last));
	}

	@Override
	public boolean disableAllUpperI(ProtocolAPI version) {
		if (viaVersionPlugin == null)
			return false;
		throw new UnsupportedOperationException("Can't disable versions on ViaVersion yet. Maybe contribute to find a solution ?");
	}

	@Override
	public boolean disableAllUnderI(ProtocolAPI version) {
		if (viaVersionPlugin == null)
			return false;
		throw new UnsupportedOperationException("Can't disable versions on ViaVersion yet. Maybe contribute to find a solution ?");
		/*
		List<ProtocolAPI> toDisable = ProtocolAPI.getAllUnderI(version.getProtocolNumber());
		
		ServerProtocolVersion lastProtocolVersion = versionHandler.getServerProtocolVersion();
		Set<Integer> newSupportedVersion = lastProtocolVersion.supportedVersions().stream().filter(protocolId -> !toDisable.stream().anyMatch(protocol -> protocol.getProtocolNumber() != protocolId)).collect(Collectors.toSet());
		ServerProtocolVersion serverProtocolVersion;
		serverProtocolVersion = new ServerProtocolVersionRange(version.getUpperProtocol().getProtocolNumber(), lastProtocolVersion.highestSupportedVersion(),
				(IntSortedSet) new IntArrayList(newSupportedVersion));
		versionHandler.getServerProtocolVersion().highestSupportedVersion();
		versionHandler.setServerProtocol(serverProtocolVersion);
		*/

	}

	@Override
	public boolean disable(ProtocolAPI... versions) {
		if (viaVersionPlugin == null)
			return false;
		throw new UnsupportedOperationException("Can't disable versions on ViaVersion yet. Maybe contribute to find a solution ?");
	}

	@Override
	public boolean disable(ProtocolAPI versions) {
		if (viaVersionPlugin == null)
			return false;
		throw new UnsupportedOperationException("Can't disable versions on ViaVersion yet. Maybe contribute to find a solution ?");
	}
}
