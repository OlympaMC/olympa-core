package fr.olympa.core.spigot.tps;

import java.util.List;
import java.util.StringJoiner;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.TPS;
import fr.olympa.core.spigot.OlympaCore;

public class TpsCommand extends OlympaCommand {

	public TpsCommand(Plugin plugin) {
		super(plugin, "tps+", "tps");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		double[] tps = TPS.getTPSs();
		double average = (tps[0] + tps[1] + tps[2]) / 3;
		MachineInfo machine = new MachineInfo();
		OlympaCore core = OlympaCore.getInstance();
		StringJoiner sb = new StringJoiner("\n");
		sb.add("&aServeur <servername>");
		sb.add("&aTPS: &2" + tps[0]);
		sb.add("&aMoyenne: &2" + average);
		sb.add("&aRAM: &2" + machine.getMemUsed() + "/" + machine.getMemTotal() + "&amo");
		sb.add("&aCPU: &2" + machine.getCPUUsage() + "&a (" + machine.getCores() + " cores)");
		sb.add("&aServeur &2" + core.getServer().getName() + "&a est allumé depuis &2" + core.getUptime() + "&a.");
		sendMessage(Prefix.DEFAULT, sb.toString());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
