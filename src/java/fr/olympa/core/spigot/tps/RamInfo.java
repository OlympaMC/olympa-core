package fr.olympa.core.spigot.tps;

public class RamInfo {
	long memFree;
	long memUsed;
	long memTotal;
	
	public RamInfo() {
		Runtime r = Runtime.getRuntime();
		this.memUsed = (r.totalMemory() - r.freeMemory()) / 1048576;
		this.memFree = r.freeMemory() / 1048576;
		this.memTotal = r.totalMemory() / 1048576;
	}
	
	public long getMemFree() {
		return this.memFree;
	}
	
	public long getMemTotal() {
		return this.memTotal;
	}
	
	public long getMemUsed() {
		return this.memUsed;
	}

}
