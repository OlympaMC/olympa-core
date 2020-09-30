package fr.olympa.api.bungee.task;

import java.util.concurrent.TimeUnit;

import fr.olympa.api.task.OlympaTask;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.api.scheduler.TaskScheduler;

public class BungeeTaskManager implements OlympaTask {

	protected Plugin plugin;

	public BungeeTaskManager(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void cancelTaskById(int id) {
		getScheduler().cancel(id);
		removeTaskById(id);
	}

	@Override
	public void checkIfExist(String taskName) {
		if (taskExist(taskName))
			cancelTaskByName(taskName);
	}

	@Override
	public ScheduledTask getTask(int id) {
		ScheduledTask task = null;
		if (id > 0)
			for (int taskId : taskList.values())
				if (taskId == id)
					return task;
		return null;
	}

	@Override
	public Object getTask(String taskName) {
		return getTask(getTaskIdByName(taskName));
	}

	@Override
	public int runTask(Runnable runnable) {
		return getScheduler().schedule(plugin, runnable, 0, TimeUnit.SECONDS).getId();
	}

	@Override
	public int runTaskAsynchronously(Runnable runnable) {
		return getScheduler().runAsync(plugin, runnable).getId();
	}

	@Override
	public int runTaskAsynchronously(String taskName, Runnable runnable) {
		Integer oldTaskId = taskList.get(taskName);
		if (oldTaskId != null)
			getScheduler().cancel(oldTaskId);
		int taskId = this.runTaskAsynchronously(runnable);
		taskList.put(taskName, taskId);
		return taskId;
	}

	@Override
	public int runTaskLater(Runnable runnable, long delay, TimeUnit timeUnit) {
		return runTaskLaterAndGet(runnable, delay, timeUnit).getId();
	}

	public ScheduledTask runTaskLaterAndGet(Runnable runnable, long delay, TimeUnit timeUnit) {
		return getScheduler().schedule(plugin, runnable, delay, timeUnit);
	}

	@Override
	public int runTaskLater(String taskName, Runnable runnable, long delay, TimeUnit timeUnit) {
		Integer oldTaskId = taskList.get(taskName);
		if (oldTaskId != null)
			getScheduler().cancel(oldTaskId);
		ScheduledTask schTask = getScheduler().schedule(plugin, runnable, delay, timeUnit);
		int id = schTask.getId();
		taskList.put(taskName, id);
		this.runTaskLater(() -> {
			if (taskList.get(taskName) != null && taskList.get(taskName) == id)
				taskList.remove(taskName);
		}, delay, timeUnit);
		return schTask.getId();
	}

	@Override
	public int scheduleSyncRepeatingTask(Runnable runnable, long delay, long refresh, TimeUnit timeUnit) {
		return getScheduler().schedule(plugin, runnable, delay, refresh, timeUnit).getId();
	}

	@Override
	public int scheduleSyncRepeatingTask(String taskName, Runnable runnable, long delay, long refresh, TimeUnit timeUnit) {
		cancelTaskByName(taskName);
		int taskId = getScheduler().schedule(plugin, runnable, delay, refresh, timeUnit).getId();
		taskList.put(taskName, taskId);
		return taskId;
	}

	private TaskScheduler getScheduler() {
		return plugin.getProxy().getScheduler();
	}

}
