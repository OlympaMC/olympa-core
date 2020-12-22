package fr.olympa.api.utils;

import java.util.function.Consumer;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.apache.commons.lang.exception.ExceptionUtils;

public class ErrorLoggerHandler extends Handler {
	
	private Consumer<String> sendError;
	
	public ErrorLoggerHandler(Consumer<String> sendError) {
		this.sendError = sendError;
	}
	
	@Override
	public void publish(LogRecord record) {
		if (record.getThrown() != null) {
			sendError.accept(record.getLevel().getName() + " [" + record.getLoggerName() + "] " + record.getMessage().replaceAll("executing task \\d*", "executing task XXX") + "\n" + ExceptionUtils.getStackTrace(record.getThrown()));
		}
	}
	
	@Override
	public void flush() {}
	
	@Override
	public void close() throws SecurityException {}
	
}
