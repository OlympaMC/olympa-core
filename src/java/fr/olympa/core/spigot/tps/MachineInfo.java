package fr.olympa.core.spigot.tps;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

public class MachineInfo {

	private long memFree, memUsed, memTotal;
	private double cpuUsage, memUsage;
	private int cores;

	public MachineInfo() {
		Runtime r = Runtime.getRuntime();
		memUsage = (r.totalMemory() - r.freeMemory()) / r.totalMemory() * 100;
		memUsed = (r.totalMemory() - r.freeMemory()) / 1048576;
		memFree = r.freeMemory() / 1048576;
		memTotal = r.totalMemory() / 1048576;

		OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
		cpuUsage = osMXBean.getSystemLoadAverage();
		cores = osMXBean.getAvailableProcessors();

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

	}

	public int getCores() {
		return cores;
	}

	public double getCPUUsage() {
		return cpuUsage;
	}

	public long getMemFree() {
		return memFree;
	}

	public long getMemTotal() {
		return memTotal;
	}

	public double getMemUsage() {
		return memUsage;
	}

	public long getMemUsed() {
		return memUsed;
	}

}
