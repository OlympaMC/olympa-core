package fr.olympa.core.bungee.connectionqueue;

import java.util.concurrent.TimeUnit;

import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class QueueTask implements Runnable {

	private static ScheduledTask task;

	public static boolean isRunning() {
		return task != null;
	}

	public static void start() {
		if (!isRunning())
			s();
	}

	public static void s() {
		task = OlympaBungee.getInstance().getTask().runTaskLater(new QueueTask(), QueueHandler.TIME_BETWEEN_2, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		if (QueueHandler.getQueueSize() == 0) {
			task = null;
			return;
		}
		String next = QueueHandler.getNext();
		if (next == null)
			return;
		QueueHandler.remove(next);
		s();
	}

}
