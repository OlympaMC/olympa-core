package fr.olympa.core.bungee.connectionqueue;

import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class QueueTask implements Runnable {

	private static ScheduledTask task;

	public static boolean isRunning() {
		return task != null;
	}

	public static void start() {
		if (!isRunning())
			// 1 joueur par 0.15 seconde
			task = OlympaBungee.getInstance().getTask().runTaskLater(new QueueTask(), QueueHandler.TIME_BETWEEN_2 / 500);
	}

	@Override
	public void run() {
		if (QueueHandler.getQueueSize() == 0) {
			task = null;
			return;
		}
		String next = QueueHandler.getNext();
		QueueHandler.remove(next);
		task = null;
		start();
	}

}
