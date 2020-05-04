package fr.olympa.core.spigot.scoreboards.packets;

enum PacketData {

	v1_7("e", "c", "d", "a", "f", "g", "b", "NA", "NA", "NA"),
	cauldron("field_149317_e", "field_149319_c", "field_149316_d", "field_149320_a",
			"field_149314_f", "field_149315_g", "field_149318_b", "NA", "NA", "NA"),
	v1_8("g", "c", "d", "a", "h", "i", "b", "NA", "NA", "e"),
	v1_9("h", "c", "d", "a", "i", "j", "b", "NA", "f", "e"),
	v1_10("h", "c", "d", "a", "i", "j", "b", "NA", "f", "e"),
	v1_11("h", "c", "d", "a", "i", "j", "b", "NA", "f", "e"),
	v1_12("h", "c", "d", "a", "i", "j", "b", "NA", "f", "e"),
	v1_13("h", "c", "d", "a", "i", "j", "b", "g", "f", "e"),
	v1_14("h", "c", "d", "a", "i", "j", "b", "g", "f", "e"),
	v1_15("h", "c", "d", "a", "i", "j", "b", "g", "f", "e");

	private String members;
	private String prefix;
	private String suffix;
	private String teamName;
	private String paramInt;
	private String packOption;
	private String displayName;
	private String color;
	private String push;
	private String visibility;

	private PacketData(String members, String prefix, String suffix, String teamName, String paramInt, String packOption, String displayName, String color, String push, String visibility) {
		this.members = members;
		this.prefix = prefix;
		this.suffix = suffix;
		this.teamName = teamName;
		this.paramInt = paramInt;
		this.packOption = packOption;
		this.displayName = displayName;
		this.color = color;
		this.push = push;
		this.visibility = visibility;
	}

	public String getColor() {
		return color;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getMembers() {
		return members;
	}

	public String getPackOption() {
		return packOption;
	}

	public String getParamInt() {
		return paramInt;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getPush() {
		return push;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getTeamName() {
		return teamName;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setMembers(String members) {
		this.members = members;
	}

	public void setPackOption(String packOption) {
		this.packOption = packOption;
	}

	public void setParamInt(String paramInt) {
		this.paramInt = paramInt;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setPush(String push) {
		this.push = push;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

}
