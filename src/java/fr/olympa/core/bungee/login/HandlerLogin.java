package fr.olympa.core.bungee.login;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class HandlerLogin {
	
	public static List<String> command = new ArrayList<>();
	public static Cache<String, Integer> timesFails = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
	
}
