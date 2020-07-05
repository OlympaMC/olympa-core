package fr.olympa.core.bungee.connectionqueue;

import java.util.LinkedList;

public class QueueHandler {

	// 150 = 0.15 sec
	public static int TIME_BETWEEN_2 = 150;
	private static LinkedList<String> queue = new LinkedList<>();

	public static int add(String playerName) {
		queue.add(playerName);
		System.out.println("§bTaille de la flle d'attente: " + queue.size());
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
		return queue.getFirst();
	}

	public static int getQueueSize() {
		return queue.size();
	}
}
