package fr.tristiisch.olympa.core.ban.objects;

import java.util.Arrays;

public enum EmeraldBanType {

	BAN(1, "Ban"),
	BANIP(2, "BanIP"),
	MUTE(3, "Mute"),
	KICK(4, "Kick");
	
	final int i;
	final String s;
	
	private EmeraldBanType(int i, String s){
		this.i = i;
		this.s = s;
	}
	
	public int getInteger(){
		return i;
	}
	
	public String getName(){
		return s;
	}
	
	public static EmeraldBanType getByID(int i) {
		return Arrays.stream(EmeraldBanType.values()).filter(id -> id.getInteger() == i).findFirst().orElse(null);
	}
}
