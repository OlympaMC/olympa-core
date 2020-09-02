package fr.olympa.api.utils;

import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.function.Function;

public class ErrorOutputStream extends PrintStream {
	
	private StringBuffer pending = new StringBuffer();
	private Object task = null;
	
	private Consumer<String> sendError;
	private Function<Runnable, Object> launchTask;
	
	public ErrorOutputStream(PrintStream wrap, Consumer<String> sendError, Function<Runnable, Object> launchOneSecondTask) {
		super(wrap);
		this.sendError = sendError;
		this.launchTask = launchOneSecondTask;
	}
	
	@Override
	public void println(String x) {
		super.println(x);
		pending.append(x).append('\n');
		if (task == null) task = launchTask.apply(this::execute);
	}
	
	@Override
	public void println(Object x) {
		super.println(x);
		pending.append(x).append('\n');
		if (task == null) task = launchTask.apply(this::execute);
	}
	
	protected void execute() {
		task = null;
		sendError.accept(pending.toString());
		pending = new StringBuffer();
	}
	
}
