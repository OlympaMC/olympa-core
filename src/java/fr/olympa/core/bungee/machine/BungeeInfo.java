package fr.olympa.core.bungee.machine;

import java.io.File;
import java.util.stream.Collectors;

import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.machine.MachineMessage;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;

public class BungeeInfo extends MachineMessage {

	public BungeeInfo(boolean isConsole) {
		super(isConsole);
	}

	@Override
	public TxtComponentBuilder getInfoMessage() {
		if (main.isSpigot())
			throw new UnsupportedOperationException("Unable to get Bungee Info on not Bungee Environment");
		TxtComponentBuilder textBuilder = super.getInfoMessage();
		try {
			textBuilder.extra(new TxtComponentBuilder("&3Plugins Olympa: &b"));
			for (TxtComponentBuilder txt : ((OlympaBungee) main).getProxy().getPluginManager().getPlugins().stream().filter(f -> f.getDescription().getName().startsWith("Olympa"))
					.map(ff -> {
						String fileInfo = Utils.tsToShortDur(new File(ff.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).lastModified() / 1000L);
						return new TxtComponentBuilder("&6%s ", ff.getDescription().getName().substring(6)).onHoverText("&eDernière MAJ %s", fileInfo).console(isConsole);
					})
					.collect(Collectors.toList()))
				textBuilder.extra(txt);
			textBuilder.extra("\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return textBuilder;
	}
}
