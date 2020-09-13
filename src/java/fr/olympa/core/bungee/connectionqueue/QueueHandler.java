package fr.olympa.core.bungee.connectionqueue;

import java.util.LinkedList;

public class QueueHandler {

	// 150 = 0.15 sec
	public static int TIME_BETWEEN_2 = 300;
	private static LinkedList<String> queue = new LinkedList<>();

	public static int add(String playerName) {
		if (queue.contains(playerName))
			return -1;
		queue.add(playerName);
		System.out.println("§bTaille de la flle d'attente: " + queue.size());
		QueueTask.start();
		return getTimeToW8(queue.size(), playerName);
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
		return queue.isEmpty() ? null : queue.getFirst();
	}

	public static int getQueueSize() {
		return queue.size();
	}

	private static int getTimeToW8(int pos, String playerName) {
		return pos * TIME_BETWEEN_2;
	}

	public static int getTimeToW8(String playerName) {
		return getTimeToW8(queue.indexOf(playerName) + 1, playerName);
	}
}
