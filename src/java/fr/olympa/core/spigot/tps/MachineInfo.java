package fr.olympa.core.spigot.tps;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

public class MachineInfo {
	
	private long memFree, memUsed, memTotal;
	private double cpuUsage;
	private int cores;

	public MachineInfo() {
		Runtime r = Runtime.getRuntime();
		memUsed = (r.totalMemory() - r.freeMemory()) / 1048576;
		memFree = r.freeMemory() / 1048576;
		memTotal = r.totalMemory() / 1048576;
		
		OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
		cpuUsage = osMXBean.getSystemLoadAverage();
		cores = osMXBean.getAvailableProcessors();

		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

	}

	public long getMemFree() {
		return memFree;
	}

	public long getMemTotal() {
		return memTotal;
	}

	public long getMemUsed() {
		return memUsed;
	}

	public double getCPUUsage() {
		return cpuUsage;
	}
	
	public int getCores() {
		return cores;
	}
	
}
