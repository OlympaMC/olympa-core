package fr.olympa.core.bungee.connectionqueue;

import java.util.LinkedHashSet;

public class QueueHandler {

	// 150 = 0.15 sec
	public static int TIME_BETWEEN_2 = 1500;
	private static LinkedHashSet<String> queue = new LinkedHashSet<>();

	public static int add(String playerName) {
		boolean notAlreadyIn = queue.add(playerName);
		if (notAlreadyIn && !QueueTask.isRunning()) {
			QueueTask.start();
			return queue.size();
		}
		return -1;
	}

	public static boolean remove(String playerName) {
		return queue.remove(playerName);
	}

	public static boolean isNext(String playerName) {
		return playerName.equals(getNext());
	}

	public static boolean isInQueue(String playerName) {
		return queue.contains(playerName);
	}

	public static String getNext() {
		return (String) queue.toArray()[0];
	}

	public static int getQueueSize() {
		return queue.size();
	}
}
