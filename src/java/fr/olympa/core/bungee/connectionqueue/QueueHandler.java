package fr.olympa.core.bungee.connectionqueue;

import java.util.LinkedHashSet;

public class QueueHandler {

	// 150 = 0.15 sec
	public static int TIME_BETWEEN_2 = 150;
	private static LinkedHashSet<String> queue = new LinkedHashSet<>();

	public static int add(String playerName) {
		queue.add(playerName);
		System.out.println("§bTaille de la fille d'attente: " + queue.size());
		QueueTask.start();
		return queue.size();
	}

	public static boolean remove(String playerName) {
		return queue.remove(playerName);
	}

	public static boolean isNext(String playerName) {
		return playerName.equals(getNext());
	}

	public static boolean isInQueue(String playerName) {
		QueueTask.start();
		return queue.contains(playerName);
	}

	public static String getNext() {
		return (String) queue.toArray()[queue.size() - 1];
	}

	public static int getQueueSize() {
		return queue.size();
	}
}
