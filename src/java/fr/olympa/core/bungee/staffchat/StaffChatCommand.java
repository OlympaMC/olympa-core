package fr.olympa.core.bungee.staffchat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import fr.olympa.api.bungee.command.BungeeCommand;
import fr.olympa.api.common.permission.list.OlympaCorePermissionsBungee;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public class StaffChatCommand extends BungeeCommand implements TabExecutor {

	public StaffChatCommand(Plugin plugin) {
		super(plugin, "staffchat", OlympaCorePermissionsBungee.STAFF_CHAT, "sc");
		usageString = "<on|off|message>";
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (proxiedPlayer != null) {
			UUID uuid = proxiedPlayer.getUniqueId();
			if (args.length == 0) {
				if (StaffChatHandler.getStaffchat().remove(uuid))
					sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode désactivé.");
				else {
					StaffChatHandler.getStaffchat().add(uuid);
					sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode activé.");
				}
				return;
			} else if (args.length == 1)
				if (args[0].equalsIgnoreCase("off")) {
					if (StaffChatHandler.getStaffchat().remove(uuid))
						sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode désactivé.");
					else
						sendMessage(Prefix.DEFAULT_BAD + "StaffChat déjà désactivé.");
					return;
				} else if (args[0].equalsIgnoreCase("on")) {

					if (StaffChatHandler.getStaffchat().contains(uuid))
						sendMessage(Prefix.DEFAULT_BAD + "StaffChat déjà activé.");
					else {
						StaffChatHandler.getStaffchat().add(uuid);
						sendMessage(Prefix.DEFAULT_GOOD + "StaffChat mode activé.");
					}
					return;
				}
			StaffChatHandler.sendMessage(olympaPlayer, sender, buildText(0, args));
		} else if (args.length == 0)
			sendError("Un message doit être spécifié.");
		else
			StaffChatHandler.sendMessage(null, sender, buildText(0, args));
	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		if (args.length == 0) {

			List<String> reasons = Arrays.asList("on", "off");
			return Utils.startWords(args[2], reasons);
		}
		return new ArrayList<>();
	}

}
