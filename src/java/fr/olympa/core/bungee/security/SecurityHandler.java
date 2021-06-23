package fr.olympa.core.bungee.security;

import fr.olympa.core.bungee.antibot.AntiBotHandler;

public class SecurityHandler implements Cloneable {

	private static SecurityHandler INSTANCE = new SecurityHandler();

	public static SecurityHandler getInstance() {
		return INSTANCE;
	}

	public static void setInstance(SecurityHandler instance) {
		INSTANCE = instance;
	}

	public boolean pingBeforeJoin;
	public boolean checkCorrectEntredIp;
	public boolean checkCorrectEntredIpNumber;
	public boolean checkVpn;
	public boolean checkVpnOnMotd;
	public boolean allowCrack;
	public boolean allowPremium;
	public AntiBotHandler antibot = new AntiBotHandler();

	// Need to implement
	public boolean disableMotdForVpn;
	public boolean antiBotEnable;
	public boolean rejectVpn;
	public boolean rejectNewPlayerCrackOnly;
	public boolean rejectNewPlayerCrackPremium;
	public boolean rejectAllCrack;

	public SecurityHandler() {
		setDefault();
		//		LinkSpigotBungee.Provider.link.getTask().runTaskLater(() -> pingBeforeJoin = true, 20, TimeUnit.SECONDS);
	}

	public void setDefault() {
		pingBeforeJoin = false;
		checkCorrectEntredIp = true;
		checkCorrectEntredIpNumber = true;
		checkVpn = true;
		checkVpnOnMotd = true;
		allowCrack = true;
		allowPremium = true;
	}

	public void antiBotConfig() {
		pingBeforeJoin = true;
		checkCorrectEntredIp = true;
		checkCorrectEntredIpNumber = true;
		checkVpn = true;
		checkVpnOnMotd = false;
		allowCrack = true;
		allowPremium = true;
	}

	public boolean isPingBeforeJoin() {
		return pingBeforeJoin;
	}

	public boolean isCheckCorrectEntredIp() {
		return checkCorrectEntredIp;
	}

	public boolean isCheckCorrectEntredIpNumber() {
		return checkCorrectEntredIpNumber;
	}

	public boolean isCheckVpn() {
		return checkVpn;
	}

	public boolean isCheckVpnOnMotd() {
		return checkVpnOnMotd;
	}

	public boolean isAllowCrack() {
		return allowCrack;
	}

	public boolean isAllowPremium() {
		return allowPremium;
	}

	public void setPingBeforeJoin(boolean pingBeforeJoin) {
		this.pingBeforeJoin = pingBeforeJoin;
	}

	public void setCheckCorrectEntredIp(boolean checkCorrectEntredIp) {
		this.checkCorrectEntredIp = checkCorrectEntredIp;
	}

	public void setCheckCorrectEntredIpNumber(boolean checkCorrectEntredIpNumber) {
		this.checkCorrectEntredIpNumber = checkCorrectEntredIpNumber;
	}

	public void setCheckVpn(boolean checkVpn) {
		this.checkVpn = checkVpn;
	}

	public void setCheckVpnOnMotd(boolean checkVpnOnMotd) {
		this.checkVpnOnMotd = checkVpnOnMotd;
	}

	public void setAllowCrack(boolean allowCrack) {
		this.allowCrack = allowCrack;
	}

	public void setAllowPremium(boolean allowPremium) {
		this.allowPremium = allowPremium;
	}

	public boolean isDisableMotdForVpn() {
		return disableMotdForVpn;
	}

	public boolean isAntiBotEnable() {
		return antiBotEnable;
	}

	public boolean isRejectVpn() {
		return rejectVpn;
	}

	public boolean isRejectNewPlayerCrackOnly() {
		return rejectNewPlayerCrackOnly;
	}

	public boolean isRejectNewPlayerCrackPremium() {
		return rejectNewPlayerCrackPremium;
	}

	public boolean isRejectAllCrack() {
		return rejectAllCrack;
	}

	public AntiBotHandler getAntibot() {
		return antibot;
	}
}
