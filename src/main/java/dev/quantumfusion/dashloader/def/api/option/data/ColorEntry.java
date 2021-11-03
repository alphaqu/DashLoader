package dev.quantumfusion.dashloader.def.api.option.data;

public class ColorEntry {
	public String tag;
	public String value;

	public ColorEntry(String tag, String value) {
		this.tag = tag;
		this.value = value;
	}

	public ColorEntry() {
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
