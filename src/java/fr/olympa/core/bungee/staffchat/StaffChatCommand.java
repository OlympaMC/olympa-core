package fr.olympa.core.bungee.staffchat;

import java.util.UUID;

import fr.olympa.api.permission.OlympaCorePermissions;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.bungee.api.command.BungeeCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;

public class StaffChatCommand extends BungeeCommand {
	
	public StaffChatCommand(Plugin plugin) {
		super(plugin, "staffchat", OlympaCorePermissions.STAFF_CHAT, "sc");
		usageString = "<on|off|message>";
	}
	
	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (proxiedPlayer != null) {
			UUID uuid = proxiedPlayer.getUniqueId();
			if (args.length == 0) {
				if (StaffChatHandler.getStaffchat().remove(uuid)) {
					sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode désactivé.");
				}else {
					StaffChatHandler.getStaffchat().add(uuid);
					sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode activé.");
				}
			}else if (args[0].equalsIgnoreCase("off")) {
				if (StaffChatHandler.getStaffchat().remove(uuid)) {
					sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode désactivé.");
				}else
					sendMessage(Prefix.DEFAULT_BAD + "StaffChat déjà désactivé.");
			}else if (args[0].equalsIgnoreCase("on")) {
				if (StaffChatHandler.getStaffchat().contains(uuid)) {
					sendMessage(Prefix.DEFAULT_BAD + "StaffChat déjà activé.");
				}else {
					StaffChatHandler.getStaffchat().add(uuid);
					sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode activé.");
				}
			}else StaffChatHandler.sendMessage(getOlympaPlayer(), sender, buildText(0, args));
		}else {
			if (args.length == 0) {
				sendError("Un message doit être spécifié.");
			}else StaffChatHandler.sendMessage(getOlympaPlayer(), sender, buildText(0, args));
		}
	}
	
}
