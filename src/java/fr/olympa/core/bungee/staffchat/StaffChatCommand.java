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
		UUID uuid = proxiedPlayer.getUniqueId();
		if (args.length == 0) {
			System.out.println(StaffChatHandler.staffChat.size());
			if (StaffChatHandler.staffChat.contains(uuid)) {
				StaffChatHandler.staffChat.remove(uuid);
				sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode désactivé.");
			} else {
				StaffChatHandler.staffChat.add(uuid);
				sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode activé.");
			}
			System.out.println(StaffChatHandler.staffChat.size());
		} else if (args[0].equalsIgnoreCase("off")) {
			if (StaffChatHandler.staffChat.contains(uuid)) {
				StaffChatHandler.staffChat.remove(uuid);
				sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode désactivé.");
			} else {
				sendMessage(Prefix.DEFAULT_BAD + "StaffChat déjà désactivé.");
			}
		} else if (args[0].equalsIgnoreCase("on")) {
			if (StaffChatHandler.staffChat.contains(uuid)) {
				sendMessage(Prefix.DEFAULT_BAD + "StaffChat déjà activé.");
			} else {
				StaffChatHandler.staffChat.add(uuid);
				sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode activé.");
			}
		} else {
			StaffChatHandler.sendMessage(olympaPlayer, proxiedPlayer, buildText(0, args));
		}
	}

}
