package fr.olympa.core.bungee.ban.commands.methods;

public class BanPlayer {
	
	//	/**
	//	 * Ajoute un ban targetUUID ou targetname ne doit pas être null.
	//	 *
	//	 * @param author     is a UUID of author of ban or String (If the author is
	//	 *                   Console, author = "Console")
	//	 * @param targetname Name of player to ban. case insensitive
	//	 */
	//	@SuppressWarnings("deprecation")
	//	public static void addBanPlayer(UUID author, CommandSender sender, String targetname, UUID targetUUID, String[] args, OlympaPlayer olympaPlayer) {
	//		// /ban <pseudo> <time unit> <reason>
	//		// args[0] = target
	//		// args[1] = time + unit
	//		// args[2] & + = reason
	//		
	//		Configuration config = OlympaBungee.getInstance().getConfig();
	//		long currentTime = Utils.getCurrentTimeInSeconds();
	//		ProxiedPlayer player = null;
	//		if (sender instanceof ProxiedPlayer)
	//			player = (ProxiedPlayer) sender;
	//		ProxiedPlayer target = null;
	//		OlympaPlayer olympaTarget = null;
	//		if (targetUUID != null)
	//			target = ProxyServer.getInstance().getPlayer(targetUUID);
	//		else if (targetname != null)
	//			target = ProxyServer.getInstance().getPlayer(targetname);
	//		else
	//			throw new NullPointerException("The uuid or name must be specified");
	//		
	//		try {
	//			if (target != null)
	//				olympaTarget = AccountProvider.get(target.getUniqueId());
	//			else if (targetUUID != null)
	//				olympaTarget = AccountProvider.getFromDatabase(targetUUID);
	//			else if (targetname != null)
	//				olympaTarget = AccountProvider.getFromDatabase(targetname);
	//		} catch (SQLException e) {
	//			sender.sendMessage(config.getString("ban.errordb"));
	//			e.printStackTrace();
	//			return;
	//		}
	//		
	//		// Si le joueur est déjà banni
	//		OlympaSanction alreadyban = BanMySQL.getSanctionActive(olympaTarget.getUniqueId(), OlympaSanctionType.BAN);
	//		if (alreadyban != null) {
	//			// Sinon annuler le ban
	//			TextComponent msg = new TextComponent(config.getString("ban.alreadyban").replace("%player%", olympaTarget.getName()));
	//			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, alreadyban.toBaseComplement()));
	//			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + alreadyban.getId()));
	//			sender.sendMessage(msg);
	//			return;
	//		}
	//		Matcher matcher1 = SanctionUtils.matchDuration(args[1]);
	//		Matcher matcher2 = SanctionUtils.matchUnit(args[1]);
	//		// Si la command contient un temps et une unité valide
	//		if (matcher1.find() && matcher2.find()) {
	//			if (args.length <= 2) {
	//				sender.sendMessage(config.getString("ban.usageban"));
	//				return;
	//			}
	//			// Si la command contient un motif
	//			String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
	//			String time = matcher1.group();
	//			String unit = matcher2.group();
	//			long expire = SanctionUtils.toTimeStamp(Integer.parseInt(time), unit);
	//			long seconds = expire - currentTime;
	//			
	//			if (OlympaCorePermissions.BAN_BYPASS_BAN.hasPermission(olympaTarget)) {
	//				sender.sendMessage(ColorUtils.color(config.getString("ban.cantbanstaffmembers")));
	//				return;
	//			}
	//			if (seconds <= SanctionManager.minTimeBan) {
	//				sender.sendMessage(ColorUtils.color(config.getString("ban.cantbypasstime").replace("%sanction", "ban").replace("%duration", Utils.timeToDuration(SanctionManager.minTimeBan))));
	//				return;
	//			} else if (seconds >= SanctionManager.maxTimeBan) {
	//				sender.sendMessage(ColorUtils.color(config.getString("ban.cantbypasstime").replace("%sanction", "ban").replace("%duration", Utils.timeToDuration(SanctionManager.maxTimeBan))));
	//				return;
	//			}
	//			String expireString = Utils.timestampToDuration(expire);
	//			OlympaSanction ban;
	//			try {
	//				ban = SanctionManager.add(OlympaSanctionType.BAN, author, olympaTarget.getUniqueId(), reason, expire);
	//			} catch (SQLException e) {
	//				e.printStackTrace();
	//				sender.sendMessage(ColorUtils.color(config.getString("ban.errordb")));
	//				return;
	//			}
	//			// Si Target est connecté
	//			if (target != null) {
	//				// Envoyer un message à Target lors de la déconnexion
	//				target.disconnect(SpigotUtils.connectScreen(config.getString("ban.tempbandisconnect"))
	//						.replace("%reason%", ban.getReason())
	//						.replace("%time%", expireString)
	//						.replace("%id%", String.valueOf(ban.getId())));
	//				
	//				// Envoyer un message à tous les joueurs du même serveur spigot
	//				ProxyServer.getInstance().broadcast(config.getString("ban.tempbanannounce")
	//						.replace("%player%", olympaTarget.getName())
	//						.replace("%time%", expireString)
	//						.replace("%reason%", reason));
	//				
	//			}
	//			// Envoye un message à l'auteur (+ staff)
	//			TextComponent msg = new TextComponent(config.getString("ban.tempbanannouncetostaff")
	//					.replace("%player%", olympaTarget.getName())
	//					.replace("%time%", expireString)
	//					.replace("%reason%", reason)
	//					.replace("%author%", SpigotUtils.getName(author)));
	//			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
	//			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));
	//			
	//			OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
	//			ProxyServer.getInstance().getConsole().sendMessage(msg.toPlainText());
	//			
	//			// Sinon: ban def
	//		} else {
	//			if (olympaPlayer != null && BanIpCommand.permToBandef.hasPermission(olympaPlayer)) {
	//				sender.sendMessage(Prefix.DEFAULT_BAD + "Tu as pas la permission de ban définitivement, tu peux ban maximum 1 an.");
	//				return;
	//			}
	//			String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
	//			
	//			OlympaSanction ban;
	//			try {
	//				ban = SanctionManager.add(OlympaSanctionType.BAN, author, olympaTarget.getUniqueId(), reason, 0);
	//			} catch (SQLException e) {
	//				e.printStackTrace();
	//				sender.sendMessage(config.getString("ban.errordb"));
	//				return;
	//			}
	//			
	//			// Si Target est connecté
	//			if (target != null) {
	//				// Envoyer un message à Target lors de la déconnexion
	//				target.disconnect(SpigotUtils.connectScreen(config.getString("ban.bandisconnect").replace("%reason%", reason).replace("%id%", String.valueOf(ban.getId()))));
	//				
	//				// Envoyer un message à tous les joueurs du même serveur spigot
	//				Bukkit.broadcastMessage(ColorUtils.color(config.getString("ban.banannounce").replace("%player%", olympaTarget.getName()).replace("%reason%", reason)));
	//				
	//			}
	//			// Envoye un message à l'auteur (+ staff)
	//			TextComponent msg = new TextComponent(config.getString("ban.banannouncetostaff")
	//					.replace("%player%", olympaTarget.getName())
	//					.replace("%reason%", reason)
	//					.replace("%author%", SpigotUtils.getName(author)));
	//			msg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ban.toBaseComplement()));
	//			msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/banhist " + ban.getId()));
	//			
	//			OlympaCorePermissions.BAN_SEEBANMSG.sendMessage(msg);
	//			ProxyServer.getInstance().getConsole().sendMessage(msg);
	//		}
	//	}
}
