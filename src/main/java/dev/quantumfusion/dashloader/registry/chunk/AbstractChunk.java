package dev.quantumfusion.dashloader.registry.chunk;

import dev.quantumfusion.dashloader.Dashable;

public abstract class AbstractChunk<R, D extends Dashable<R>> {
	public final byte pos;
	public final String name;

	protected AbstractChunk(byte pos, String name) {
		this.pos = pos;
		this.name = name;
	}
}
