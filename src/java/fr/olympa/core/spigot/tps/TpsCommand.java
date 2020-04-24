package fr.olympa.core.spigot.tps;

import java.util.List;
import java.util.StringJoiner;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.ProtocolAPI;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.TPS;
import fr.olympa.api.utils.TPSUtils;
import fr.olympa.core.spigot.OlympaCore;
import fr.olympa.core.spigot.protocolsupport.ProtocolSupportHook;

public class TpsCommand extends OlympaCommand {

	public TpsCommand(Plugin plugin) {
		super(plugin, "tps");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		double[] tps = TPS.getDoubleTPS();
		float average = TPS.getAverage(tps);
		// float[] tpsFloat = TPS.round(tps);
		MachineInfo machine = new MachineInfo();
		OlympaCore core = OlympaCore.getInstance();
		StringJoiner sb = new StringJoiner("\n");
		sb.add("&6Serveur <servername>");
		sb.add("&6Ouvert depuis &c" + core.getUptime() + "&6.");
		sb.add("&6Status: " + core.getStatus().getNameColored() + "&6.");
		sb.add("&6TPS: &c1m " + TPSUtils.getColor(tps[0]) + "&c 5m " + TPSUtils.getColor(tps[1]) + "&c 15m " + TPSUtils.getColor(tps[2]) + "&6.");
		sb.add("&6Moyenne: &c" + String.valueOf(average).replace(".0", "") + "&6.");
		sb.add("&6RAM: &c" + machine.getMemUsage() + "&6 (" + machine.getMemUse() + ").");
		sb.add("&6CPU: &c" + machine.getCPUUsage() + "&6 (" + machine.getCores() + " cores).");
		sb.add("&6Threads: &c" + machine.getThreads() + "&6.");
		sb.add("&6Version du serveur: &c" + Bukkit.getBukkitVersion() + "&6.");
		ProtocolSupportHook protocolSupport = (ProtocolSupportHook) core.getProtocolSupport();
		if (protocolSupport != null) {
			sb.add("&6Versions supportés: &c" + protocolSupport.getRangeVersion() + "&6.");
		} else {
			List<String> versions = ProtocolAPI.getVersionSupported();
			if (!versions.isEmpty()) {
				sb.add("&6Versions supportés: &c" + String.join(", ", versions) + "&6.");
			} else {
				sb.add("&6Versions supportés: &c" + "erreur" + "&6.");
			}
		}
		for (World world : OlympaCore.getInstance().getServer().getWorlds()) {
			Chunk[] chunks = world.getLoadedChunks();
			List<Entity> entities = world.getEntities();
			List<LivingEntity> livingEntities = world.getLivingEntities();
			sb.add("&6Monde &c" + world.getName() + "&6: &c" + chunks.length + "&6 chunks &c" + livingEntities.size() + "/" + entities.size() + "&6 entités" + "&6.");
		}

		sendMessage(Prefix.DEFAULT, sb.toString());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
