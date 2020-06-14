package fr.olympa.core.bungee.api.config;

import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.config.Configuration;

public class BungeeConfigReloadEvent extends Event {
	
	final private Configuration config;
	
	public BungeeConfigReloadEvent(Configuration config) {
		super();
		this.config = config;
	}
	
	public Configuration getConfig() {
		return config;
	}
}
