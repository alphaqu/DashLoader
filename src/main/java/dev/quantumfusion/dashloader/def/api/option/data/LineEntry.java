package dev.quantumfusion.dashloader.def.api.option.data;

public class LineEntry {
	public int weight;
	public String color;

	public LineEntry() {
	}

	public LineEntry(int weight, String color) {
		this.weight = weight;
		this.color = color;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
}
