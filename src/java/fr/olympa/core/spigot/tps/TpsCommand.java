package fr.olympa.core.spigot.tps;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.TPS;
import fr.olympa.api.utils.TPSUtils;
import fr.olympa.core.bungee.api.ProtocolAPI;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.protocolsupport.ProtocolSupportHook;

public class TpsCommand extends OlympaCommand {

	public TpsCommand(Plugin plugin) {
		super(plugin, "tps+", "tps");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		double[] tps = TPS.getDoubleTPS();
		float average = TPS.getAverage(tps);
		float[] tpsFloat = TPS.round(tps);
		MachineInfo machine = new MachineInfo();
		OlympaCore core = OlympaCore.getInstance();
		StringJoiner sb = new StringJoiner("\n");
		sb.add("&aServeur <servername>");
		sb.add("&aOuvert depuis &2" + core.getUptime() + "&a.");
		sb.add("&aStatus: " + core.getStatus().getNameColored() + "&a.");
		sb.add("&aTPS: 1m &2" + TPSUtils.getColor(tpsFloat[0]) + "&a 5m&2" + TPSUtils.getColor(tpsFloat[1]) + "&a 15m&2" + TPSUtils.getColor(tpsFloat[2]) + "&a.");
		sb.add("&aMoyenne: &2" + average + "&a.");
		sb.add("&aRAM: &2" + machine.getMemUsage() + "%&a (" + machine.getMemUsed() + "/" + machine.getMemTotal() + "Mo).");
		sb.add("&aCPU: &2" + machine.getCPUUsage() + "%&a (" + machine.getCores() + " cores).");
		sb.add("&aVersion du serveur: &2" + core.getServerVersion() + "&a.");
		ProtocolSupportHook protocolSupport = (ProtocolSupportHook) core.getProtocolSupport();
		if (protocolSupport != null) {
			sb.add("&aVersions supportés: &2" + protocolSupport.getVersionSupported() + "&a.");
		} else {
			Set<String> versions = ProtocolAPI.getVersionSupported();
			if (!versions.isEmpty()) {
				sb.add("&aVersions supportés: &2" + String.join(", ", versions) + "&a.");
			} else {
				sb.add("&aVersions supportés: &c" + "erreur" + "&a.");
			}
		}
		sendMessage(Prefix.DEFAULT, sb.toString());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
