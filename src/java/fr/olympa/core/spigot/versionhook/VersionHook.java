package fr.olympa.core.spigot.versionhook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.common.sort.Sorting;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.spigot.utils.SpigotInfoFork;
import fr.olympa.api.spigot.version.PluginHandleVersion;
import fr.olympa.api.spigot.version.VersionHandler;
import fr.olympa.core.spigot.OlympaCore;

public class VersionHook implements VersionHandler {

	private PluginHandleVersion protocolSupport;
	private PluginHandleVersion viaVersion;

	public VersionHook(OlympaCore plugin) {
		plugin.setVersionHandler(this);
		try {
			if (plugin.getServer().getPluginManager().isPluginEnabled("ProtocolSupport"))
				protocolSupport = new ProtocolSupportHook(plugin);
		} catch (NoClassDefFoundError e) {
			plugin.getLogger().severe("Impossible de récupérer la class principal de ProtocolSupport. Vérifie sa version, et si le package n'a pas été modifié.");
			e.printStackTrace();
		} catch (Exception e) {
			plugin.getLogger().severe("Une erreur est survenu avec ProtocolSupport. Vérifie sa version.");
			e.printStackTrace();
		}
		try {
			if (plugin.getServer().getPluginManager().isPluginEnabled("ViaVersion"))
				viaVersion = new ViaVersionHook(plugin);
		} catch (NoClassDefFoundError e) {
			plugin.getLogger().severe("Impossible de récupérer la class principal de ViaVersion. Vérifie sa version, et si le package n'a pas été modifié.");
			e.printStackTrace();
		} catch (Exception e) {
			plugin.getLogger().severe("Une erreur est survenu avec ProtocolSupport. Vérifie sa version.");
			e.printStackTrace();
		}
		List<ProtocolAPI> versions = getProtocolSupported();
		if (versions != null && !versions.isEmpty()) {
			ProtocolAPI first = versions.get(0);
			plugin.setFirstVersion(first.getName());
			if (versions.size() > 1)
				plugin.setFirstVersion(versions.get(versions.size() - 1).getName());
			else
				plugin.setLastVersion(first.getName());
		}
		plugin.setProtocols(getProtocolSupported());
	}

	public PluginHandleVersion getProtocolSupport() {
		return protocolSupport;
	}

	public PluginHandleVersion getViaVersion() {
		return viaVersion;
	}

	@Override
	public boolean isPlayerVersionUnder(Player player, ProtocolAPI version) {
		int protocolPlayer = getPlayerProtocol(player);
		return version.getProtocolNumber() > protocolPlayer;
	}

	@Override
	public boolean isPlayerVersionSameOrUpper(Player player, ProtocolAPI version) {
		int protocolPlayer = getPlayerProtocol(player);
		return version.getProtocolNumber() <= protocolPlayer;
	}

	@Override
	public String getVersion(Player player) {
		return ProtocolAPI.getName(getPlayerProtocol(player));
	}

	@Override
	public int getPlayerProtocol(Player player) {
		if (viaVersion != null)
			return viaVersion.getPlayerProtocol(player);
		if (protocolSupport != null)
			return protocolSupport.getPlayerProtocol(player);
		if (SpigotInfoFork.isPaper())
			return player.getProtocolVersion();
		return ProtocolAPI.getNativeSpigotProtocolAPI().getProtocolNumber();
	}

	@Override
	public List<ProtocolAPI> getProtocolSupported() {
		List<ProtocolAPI> versions = new ArrayList<>();
		if (protocolSupport != null)
			versions.addAll(protocolSupport.getProtocols());
		if (viaVersion != null)
			versions.addAll(viaVersion.getProtocols());
		if (versions.isEmpty())
			return ProtocolAPI.getNativeSpigotProtocolAPI().getSameProtocol();
		return versions.stream().distinct().sorted(new Sorting<>(ProtocolAPI::ordinal)).collect(Collectors.toList());
	}

	@Override
	public String getVersions() {
		List<ProtocolAPI> versions = new ArrayList<>();
		if (protocolSupport != null)
			versions.addAll(protocolSupport.getProtocols());
		if (viaVersion != null)
			versions.addAll(viaVersion.getProtocols());
		if (versions.isEmpty())
			return ProtocolAPI.getNativeSpigotProtocolAPI().getCompleteName();
		return ProtocolAPI.getRange(versions);
	}

	@Override
	public String getVersionsDisabled() {
		return ProtocolAPI.getRange(getVersionsSupportedDisable());
	}

	@Override
	public List<ProtocolAPI> getVersionsSupportedDisable() {
		List<ProtocolAPI> versions = new ArrayList<>();
		getProtocolsDisabled().forEach(protocolNb -> versions.addAll(ProtocolAPI.getAll(protocolNb)));
		return versions.stream().sorted(new Sorting<>(ProtocolAPI::ordinal)).collect(Collectors.toList());
	}

	@Override
	public Entry<String, String> getRangeVersionArray() {
		return ProtocolAPI.getRangeEntry(getProtocolSupported());
	}

	public List<Integer> getProtocolsEnabled() {
		return getProtocolSupported().stream().map(ProtocolAPI::getProtocolNumber).collect(Collectors.toList());
	}

	public List<Integer> getProtocolsDisabled() {
		List<Integer> versions = new ArrayList<>();
		if (protocolSupport != null)
			versions.addAll(protocolSupport.getDisabledProtocols());
		if (viaVersion != null)
			versions.addAll(viaVersion.getDisabledProtocols());
		return versions.stream().distinct().sorted().collect(Collectors.toList());
	}

	@Override
	public boolean disable(ProtocolAPI[] versions) {
		boolean b = false;
		boolean b2 = false;
		if (protocolSupport != null)
			b = protocolSupport.disable(versions);
		if (viaVersion != null)
			b2 = viaVersion.disable(versions);
		return b || b2;
	}

	@Override
	public boolean disable(ProtocolAPI versions) {
		boolean b = false;
		boolean b2 = false;
		if (protocolSupport != null)
			b = protocolSupport.disable(versions);
		if (viaVersion != null)
			b2 = viaVersion.disable(versions);
		return b || b2;
	}

	@Override
	public boolean disableAllUnderI(ProtocolAPI version) {
		boolean b = false;
		boolean b2 = false;
		if (protocolSupport != null)
			b = protocolSupport.disableAllUnderI(version);
		if (viaVersion != null)
			b2 = viaVersion.disableAllUnderI(version);
		return b || b2;
	}

	@Override
	public boolean disableAllUpperI(ProtocolAPI version) {
		boolean b = false;
		boolean b2 = false;
		if (protocolSupport != null)
			b = protocolSupport.disableAllUpperI(version);
		if (viaVersion != null)
			b2 = viaVersion.disableAllUpperI(version);
		return b || b2;
	}
}
