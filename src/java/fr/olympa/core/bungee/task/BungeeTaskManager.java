package fr.olympa.core.bungee.task;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;

public class BungeeTaskManager {

	private Plugin plugin;
	private HashMap<String, Integer> taskList = new HashMap<>();

	public BungeeTaskManager(Plugin plugin) {
		this.plugin = plugin;
	}

	public void addTask(String name, int id) {
		this.taskList.put(name, id);
	}

	public void cancelAllTask() {
		TaskScheduler scheduler = this.getScheduler();
		for (int taskId : this.taskList.values()) {
			scheduler.cancel(taskId);
		}
		scheduler.cancel(this.plugin);
	}

	public void cancelTaskById(int id) {
		this.getScheduler().cancel(id);
	}

	public boolean cancelTaskByName(String taskName) {
		if (this.taskExist(taskName)) {
			int taskId = this.getTaskId(taskName);
			this.taskList.remove(taskName);
			Bukkit.getScheduler().cancelTask(taskId);
			return true;
		}
		return false;
	}

	public void checkIfExist(String taskName) {
		if (this.taskExist(taskName)) {
			this.cancelTaskByName(taskName);
		}
	}

	private TaskScheduler getScheduler() {
		return ProxyServer.getInstance().getScheduler();
	}

	/*public Runnable getTask(int id) {
		BukkitTask task = null;
		if (id > 0) {
			for (BukkitTask pendingTask : getScheduler().) {
				if (pendingTask.getTaskId() == id) {
					return task;
				}
			}
		}
		return null;
	}

	public BukkitTask getTask(String taskName) {
		return this.getTask(this.getTaskId(taskName));
	}*/

	public int getTaskId(String taskName) {
		if (this.taskExist(taskName)) {
			return this.taskList.get(taskName);
		}
		return 0;
	}

	public String getTaskName(String string) {
		String taskName;
		for (taskName = string + "_" + new Random().nextInt(99999); this.taskExist(taskName); taskName = string + "_" + new Random().nextInt(99999)) {
		}
		return taskName;
	}

	public String getTaskNameById(int id) {
		for (Map.Entry<String, Integer> entry : this.taskList.entrySet()) {
			if (entry.getValue() == id) {
				return entry.getKey();
			}
		}
		return null;
	}

	public void removeTaskByName(String taskName) {
		this.taskList.remove(taskName);
	}

	public ScheduledTask runTask(Runnable runnable) {
		return this.getScheduler().schedule(this.plugin, runnable, 0, TimeUnit.NANOSECONDS);
	}

	public ScheduledTask runTaskAsynchronously(Runnable runnable) {
		return this.getScheduler().runAsync(this.plugin, runnable);
	}

	public ScheduledTask runTaskAsynchronously(String taskName, Runnable runnable) {
		Integer oldTask = this.taskList.get(taskName);
		if (oldTask != null) {
			this.getScheduler().cancel(oldTask);
		}
		ScheduledTask bukkitTask = this.runTaskAsynchronously(runnable);
		this.addTask(taskName, bukkitTask.getId());
		return bukkitTask;
	}

	public ScheduledTask runTaskLater(Runnable runnable, long delay, TimeUnit unit) {
		return this.getScheduler().schedule(this.plugin, runnable, delay, unit);
	}

	public ScheduledTask runTaskLater(String taskName, Runnable task, long delay, TimeUnit unit) {
		Integer oldTask = this.taskList.get(taskName);
		if (oldTask != null) {
			this.getScheduler().cancel(oldTask);
		}
		ScheduledTask bukkitTask = this.getScheduler().schedule(this.plugin, task, delay, unit);
		int id = bukkitTask.getId();
		this.addTask(taskName, id);
		this.runTaskLater(() -> {
			if (this.taskList.get(taskName) != null && this.taskList.get(taskName) == id) {
				this.taskList.remove(taskName);
			}
		}, delay, unit);
		return bukkitTask;
	}

	public ScheduledTask scheduleSyncRepeatingTask(String taskName, Runnable runnable, long delay, long period, TimeUnit unit) {
		this.cancelTaskByName(taskName);
		ScheduledTask task = this.getScheduler().schedule(this.plugin, runnable, delay, period, unit);
		this.taskList.put(taskName, task.getId());
		return task;
	}

	public boolean taskExist(String taskName) {
		return this.taskList.containsKey(taskName);
	}
}
