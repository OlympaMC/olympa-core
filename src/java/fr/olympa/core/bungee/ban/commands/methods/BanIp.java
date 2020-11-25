package fr.olympa.core.bungee.ban.commands.methods;

@Deprecated(forRemoval = true)
public class BanIp {

	//	public static void addBanIP(UUID author, CommandSender sender, String targetip, String[] args, OlympaPlayer olympaPlayer) throws SQLException {
	//		java.util.regex.Matcher matcher1 = SanctionUtils.matchDuration(args[1]);
	//		java.util.regex.Matcher matcher2 = SanctionUtils.matchUnit(args[1]);
	//		Configuration config = OlympaBungee.getInstance().getConfig();
	//		// Si la command contient un temps et une unité valide
	//		if (matcher1.find() && matcher2.find()) {
	//			// Si la command contient un motif
	//			if (args.length > 2) {
	//				String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
	//				String time = matcher1.group();
	//				String unit = matcher2.group();
	//				String ip = args[0];
	//				long timestamp = SanctionUtils.toTimeStamp(Integer.parseInt(time), unit);
	//				long seconds = timestamp - Utils.getCurrentTimeInSeconds();
	//
	//				if (seconds <= SanctionManager.minTimeBan) {
	//					sender.sendMessage(ColorUtils.color(config.getString("ban.cantbypasstime").replace("%sanction", "banip").replace("%duration", Utils.timeToDuration(SanctionManager.minTimeBan))));
	//					return;
	//				} else if (seconds >= SanctionManager.maxTimeBan) {
	//					sender.sendMessage(ColorUtils.color(config.getString("ban.cantbypasstime").replace("%sanction", "banip").replace("%duration", Utils.timeToDuration(SanctionManager.maxTimeBan))));
	//					return;
	//				}
	//				if (SanctionManager.addAndApply(OlympaSanctionType.BANIP, author, ip, reason, timestamp)) {
	//					sender.sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color("Une erreur est survenu avec le type de la sanction, ou avec la cible."));
	//					return;
	//				}
	//			}
	//		} else {
	//			if (olympaPlayer != null && BanCommand.permToBandef.hasPermission(olympaPlayer)) {
	//				sender.sendMessage(ColorUtils.color(config.getString("ban.cantbypasstime").replace("%sanction", "banip").replace("%duration", Utils.timeToDuration(SanctionManager.maxTimeBan))));
	//				return;
	//			}
	//			String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
	//			String ip = args[0];
	//			if (SanctionManager.addAndApply(OlympaSanctionType.BANIP, author, ip, reason, 0)) {
	//				sender.sendMessage(Prefix.DEFAULT_BAD + ColorUtils.color("Une erreur est survenu avec le type de la sanction, ou avec la cible."));
	//				return;
	//			}
	//		}
	//	}
}
