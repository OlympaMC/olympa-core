package fr.olympa.core.bungee.connectionqueue;

import java.util.LinkedList;

import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.antibot.AntiBotHandler;

public class QueueHandler {

	public static int TIME_BETWEEN_2 = 300;// 300 = 0.30 sec en milisecondes
	public static int NUMBER_BEFORE_CANCEL = 200; // 1 minute de queue
	public static int NUMBER_BEFORE_START_ANTIBOT = 50; // 15 secondes de queue
	private static LinkedList<String> queue = new LinkedList<>();

	public static int add(String playerName) {
		if (queue.contains(playerName))
			return -1;
		queue.add(playerName);
		System.out.println("§bTaille de la flle d'attente: " + queue.size());
		QueueTask.start();
		if (hasTooManyInQueue()) {
			AntiBotHandler.setEnable(true, null);
			return -2;
		} else if (isNeededToEnableAntiBot())
			AntiBotHandler.setEnable(true, null);
		else
			AntiBotHandler.setEnable(false, null);
		return getTimeToW8(queue.size());
	}

	public static boolean remove(String playerName) {
		return queue.remove(playerName);
	}

	public static boolean hasTooManyInQueue() {
		return queue.size() >= NUMBER_BEFORE_CANCEL;
	}

	public static boolean isNeededToEnableAntiBot() {
		return queue.size() >= NUMBER_BEFORE_START_ANTIBOT;
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

	public static int getQueueTime() {
		return queue.size() * TIME_BETWEEN_2;
	}

	public static int getMaxQueueTime() {
		return NUMBER_BEFORE_CANCEL * TIME_BETWEEN_2;
	}

	public static int getStartBotTime() {
		return NUMBER_BEFORE_START_ANTIBOT * TIME_BETWEEN_2;
	}

	public static String getMaxQueueTimeString() {
		return Utils.timeToDuration(getMaxQueueTime() / 1000L);
	}

	public static String getQueueTimeString() {
		return Utils.timeToDuration(getQueueTime() / 1000L);
	}

	public static String getStartBotTimeString() {
		return Utils.timeToDuration(getStartBotTime() / 1000L);
	}

	public static String getTimeToW8String(int pos) {
		return Utils.timeToDuration(getTimeToW8(pos) / 1000L);
	}

	public static String getTimeToW8String(String playerName) {
		return getTimeToW8String(queue.indexOf(playerName) + 1);
	}

	private static int getTimeToW8(int pos) {
		return pos * TIME_BETWEEN_2;
	}

	public static int getTimeToW8(String playerName) {
		return getTimeToW8(queue.indexOf(playerName) + 1);
	}
}
