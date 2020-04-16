package fr.olympa.core.spigot.tps;

public class RamInfo {
	long memFree;
	long memUsed;
	long memTotal;

	public RamInfo() {
		Runtime r = Runtime.getRuntime();
		memUsed = (r.totalMemory() - r.freeMemory()) / 1000000;
		memFree = r.freeMemory() / 1000000;
		memTotal = r.totalMemory() / 1000000;
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

}
