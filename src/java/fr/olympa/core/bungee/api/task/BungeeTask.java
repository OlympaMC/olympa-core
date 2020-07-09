package fr.olympa.core.bungee.api.task;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;

public class BungeeTask {
	
	protected Plugin plugin;
	private HashMap<String, ScheduledTask> taskList = new HashMap<>();
	
	public BungeeTask(Plugin plugin) {
		this.plugin = plugin;
	}

	public void cancelAllTask() {
		TaskScheduler scheduler = getScheduler();
		for (ScheduledTask task : taskList.values())
			task.cancel();
		taskList.clear();
		scheduler.cancel(plugin);
	}

	public void cancelTaskById(int id) {
		getScheduler().cancel(id);
		removeTaskById(id);
	}

	public boolean cancelTaskByName(String taskName) {
		if (taskExist(taskName)) {
			taskList.get(taskName).cancel();
			removeTaskByName(taskName);
			return true;
		}
		return false;
	}

	public void checkIfExist(String taskName) {
		if (taskExist(taskName))
			cancelTaskByName(taskName);
	}
	
	public ScheduledTask getTask(int id) {
		ScheduledTask task = null;
		if (id > 0)
			for (ScheduledTask pendingTask : taskList.values())
				if (pendingTask.getId() == id)
					return task;
		return null;
	}
	
	public ScheduledTask getTask(String taskName) {
		return taskList.get(taskName);
	}
	
	public String getTaskName(String string) {
		String taskName;
		for (taskName = string + "_" + new Random().nextInt(99999); taskExist(taskName); taskName = string + "_" + new Random().nextInt(99999)) {
		}
		return taskName;
	}
	
	public String getTaskNameById(int id) {
		for (Entry<String, ScheduledTask> entry : taskList.entrySet())
			if (entry.getValue().getId() == id)
				return entry.getKey();
		return null;
	}
	
	private void removeTaskByName(String taskName) {
		taskList.remove(taskName);
	}

	private void removeTaskById(int id) {
		taskList.entrySet().removeIf(entry -> entry.getValue().getId() == id);
	}

	public ScheduledTask runTask(Runnable runnable) {
		return getScheduler().schedule(plugin, runnable, 0, TimeUnit.SECONDS);
	}

	public ScheduledTask runTaskAsynchronously(Runnable runnable) {
		return getScheduler().runAsync(plugin, runnable);
	}

	public ScheduledTask runTaskAsynchronously(String taskName, Runnable runnable) {
		ScheduledTask oldTask = taskList.get(taskName);
		if (oldTask != null)
			oldTask.cancel();
		ScheduledTask schTask = this.runTaskAsynchronously(runnable);
		taskList.put(taskName, schTask);
		return schTask;
	}

	public ScheduledTask runTaskLater(String taskName, Runnable task, long tick) {
		return runTaskLater(taskName, task, tick * 50, TimeUnit.MILLISECONDS);
	}

	public ScheduledTask runTaskLater(Runnable runnable, long tick) {
		return getScheduler().schedule(plugin, runnable, tick * 50, TimeUnit.MILLISECONDS);
	}
	
	public ScheduledTask runTaskLater(Runnable runnable, long delay, TimeUnit timeUnit) {
		return getScheduler().schedule(plugin, runnable, delay, timeUnit);
	}

	public ScheduledTask runTaskLater(String taskName, Runnable runnable, long delay, TimeUnit timeUnit) {
		ScheduledTask oldTask = taskList.get(taskName);
		if (oldTask != null)
			this.getTask(oldTask.getId()).cancel();
		ScheduledTask schTask = getScheduler().schedule(plugin, runnable, delay, timeUnit);
		int id = schTask.getId();
		taskList.put(taskName, schTask);
		this.runTaskLater(() -> {
			if (taskList.get(taskName) != null && taskList.get(taskName).getId() == id)
				taskList.remove(taskName);
		}, delay);
		return schTask;
	}
	
	public ScheduledTask scheduleSyncRepeatingTask(String taskName, Runnable runnable, long delay, long refresh) {
		return getScheduler().schedule(plugin, runnable, delay * 50, refresh * 50, TimeUnit.MILLISECONDS);
	}

	public ScheduledTask scheduleSyncRepeatingTask(String taskName, Runnable runnable, long delay, long refresh, TimeUnit timeUnit) {
		cancelTaskByName(taskName);
		ScheduledTask task = getScheduler().schedule(plugin, runnable, delay, refresh, timeUnit);
		taskList.put(taskName, task);
		return task;
	}

	public boolean taskExist(String taskName) {
		return taskList.containsKey(taskName);
	}

	private TaskScheduler getScheduler() {
		return plugin.getProxy().getScheduler();
	}

}
