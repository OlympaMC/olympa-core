package fr.olympa.core.spigot.tps;

import java.util.List;

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
		RamInfo ram = new RamInfo();
		OlympaCore core = OlympaCore.getInstance();
		StringBuilder sb = new StringBuilder();
		sb.append("&aTPS: &2" + tps[0] + "&a.\n");
		sb.append("&aMoyenne: &2" + average + "&a\n");
		sb.append("&aRAM: &2" + ram.getMemUsed() + "/" + ram.getMemTotal() + "&a Mo.\n");
		sb.append("&aServeur &2" + core.getServer().getName() + "&a est allumé depuis &2" + core.getUptime() + "&a.\n");
		sendMessage(Prefix.DEFAULT, sb.toString());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
