package dev.quantumfusion.dashloader.minecraft.shader;

import net.minecraft.client.render.VertexFormatElement;

public final class DashVertexFormatElement {
	public final VertexFormatElement.ComponentType dataType;
	public final VertexFormatElement.Type type;
	public final int uvIndex;
	public final int length;

	public DashVertexFormatElement(
			VertexFormatElement.ComponentType dataType, VertexFormatElement.Type type, int uvIndex, int length) {
		this.dataType = dataType;
		this.type = type;
		this.uvIndex = uvIndex;
		this.length = length;
	}

	public DashVertexFormatElement(VertexFormatElement vertexFormatElement) {
		this(vertexFormatElement.getComponentType(), vertexFormatElement.getType(), vertexFormatElement.getUvIndex(), vertexFormatElement.getByteLength());
	}

	public VertexFormatElement export() {
		return new VertexFormatElement(this.uvIndex, this.dataType, this.type, this.length);
	}
}
