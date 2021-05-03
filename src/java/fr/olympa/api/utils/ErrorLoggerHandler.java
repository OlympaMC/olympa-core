package fr.olympa.api.utils;

import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.commons.lang.exception.ExceptionUtils;

import fr.olympa.core.spigot.OlympaCore;

public class ErrorLoggerHandler extends Handler {
	
	private Consumer<String> sendError;
	
	public ErrorLoggerHandler(Consumer<String> sendError) {
		this.sendError = sendError;
	}
	
	@Override
	public void publish(LogRecord record) {
		if (record.getThrown() != null) {
			String stackTrace = ExceptionUtils.getStackTrace(record.getThrown());
			try {
				sendError.accept(record.getLevel().getName() + " [" + record.getLoggerName() + "] " + record.getMessage().replaceAll("executing task \\d+", "executing task XXX").replaceAll("Task \\d+ for", "Task XXX for") + "\n" + stackTrace);
			}catch (Exception ex) {
				OlympaCore.getInstance().sendMessage("§cUne erreur est survenue durant le passage d'une erreur au bungee via redis:\n%s", stackTrace);
			}
		}
	}
	
	@Override
	public void flush() {}
	
	@Override
	public void close() throws SecurityException {}
	
}
