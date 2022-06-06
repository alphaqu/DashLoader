package dev.quantumfusion.dashloader.io;

public class SubCacheArea {
	public final String name;
	public int used;

	public SubCacheArea(String name) {
		this.name = name;
		this.used = 0;
	}

	public SubCacheArea(String name, int used) {
		this.name = name;
		this.used = used;
	}
}
