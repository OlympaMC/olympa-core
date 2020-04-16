package fr.olympa.core.bungee.staffchat;

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
		if (args.length == 0) {
			if (StaffChatHandler.staffChat.contains(proxiedPlayer)) {
				StaffChatHandler.staffChat.remove(proxiedPlayer);
				sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode désactivé.");
			} else {
				StaffChatHandler.staffChat.add(proxiedPlayer);
				sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode activé.");
			}
		} else if (args[0].equalsIgnoreCase("off")) {
			if (StaffChatHandler.staffChat.contains(proxiedPlayer)) {
				StaffChatHandler.staffChat.remove(proxiedPlayer);
				sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode désactivé.");
			} else {
				sendMessage(Prefix.DEFAULT_BAD + "StaffChat déjà désactivé.");
			}
		} else if (args[0].equalsIgnoreCase("on")) {
			if (StaffChatHandler.staffChat.contains(proxiedPlayer)) {
				sendMessage(Prefix.DEFAULT_BAD + "StaffChat déjà activé.");
			} else {
				StaffChatHandler.staffChat.add(proxiedPlayer);
				sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode activé.");
			}
		} else {
			StaffChatHandler.sendMessage(olympaPlayer, proxiedPlayer, buildText(0, args));
		}
	}

}
