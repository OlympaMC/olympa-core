package fr.olympa.core.spigot.tps;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

public class MachineInfo {

	private long memFree, memUsed, memeTotal;
	private double cpuUsage, memUsage;
	private int cores, threads;

	public MachineInfo() {
		Runtime r = Runtime.getRuntime();
		memUsed = r.totalMemory() / 1048576L;
		memFree = r.freeMemory() / 1048576L;
		memeTotal = r.maxMemory() / 1048576L;
		memUsage = memUsed / memeTotal * 100d;

		OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
		cpuUsage = osMXBean.getSystemLoadAverage();
		cores = osMXBean.getAvailableProcessors();

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		threads = threadMXBean.getThreadCount();

	}

	public int getCores() {
		return cores;
	}

	public String getCPUUsage() {
		return Math.round(cpuUsage) + "%";
	}

	public long getMemFree() {
		return memFree;
	}

	public long getMemTotal() {
		return memeTotal;
	}

	public String getMemUsage() {
		return Math.round(memUsage) + "%";
	}

	public String getMemUse() {
		return memUsed + "/" + memeTotal + "Mo";
	}

	public long getMemUsed() {
		return memUsed;
	}

	public int getThreads() {
		return threads;
	}

}