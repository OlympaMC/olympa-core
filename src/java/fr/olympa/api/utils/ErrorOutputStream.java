package fr.olympa.api.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Function;

import fr.olympa.core.spigot.redis.RedisSpigotSend;

public class ErrorOutputStream extends OutputStream {
	
	private StringBuffer pending = new StringBuffer();
	private Object task = null;
	
	private Consumer<String> sendError;
	private Function<Runnable, Object> launchTask;
	
	private PrintStream wrap;
	
	public ErrorOutputStream(PrintStream wrap, Consumer<String> sendError, Function<Runnable, Object> launchOneSecondTask) {
		this.wrap = wrap;
		this.sendError = sendError;
		this.launchTask = launchOneSecondTask;
	}
	
	@Override
	public void write(int b) throws IOException {
		wrap.write(b);
		if (!RedisSpigotSend.errorsEnabled) return;
		
		pending.append(b);
		if (task == null) task = launchTask.apply(this::execute);
	}
	
	@Override
	public void write(byte[] buf, int off, int len) throws IOException {
		wrap.write(buf, off, len);
		if (!RedisSpigotSend.errorsEnabled) return;
		
		pending.append(new String(buf, off, len));
		if (task == null) task = launchTask.apply(this::execute);
	}
	
	protected void execute() {
		task = null;
		sendError.accept(pending.toString());
		pending = new StringBuffer();
	}
	
}
