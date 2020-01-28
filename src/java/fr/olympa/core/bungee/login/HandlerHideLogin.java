package fr.olympa.core.bungee.login;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

public class HandlerHideLogin implements Filter {

	public static List<String> command = new ArrayList<>();

	@Override
	public boolean isLoggable(LogRecord record) {
		System.out.println("DEBUG commands: " + String.join(", ", command) + " MSG " + record.getMessage() + " name " + record.getLoggerName());
		return !command.stream().anyMatch(cmd -> record.getMessage().contains("executed command: /" + cmd));
	}

}
