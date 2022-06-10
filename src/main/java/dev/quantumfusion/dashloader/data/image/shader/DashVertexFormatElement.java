package dev.quantumfusion.dashloader.data.image.shader;

import net.minecraft.client.render.VertexFormatElement;

public record DashVertexFormatElement(
		VertexFormatElement.ComponentType dataType, VertexFormatElement.Type type, int uvIndex, int length) {

	public DashVertexFormatElement(VertexFormatElement vertexFormatElement) {
		this(vertexFormatElement.getComponentType(), vertexFormatElement.getType(), vertexFormatElement.getUvIndex(), vertexFormatElement.getByteLength());
	}

	public VertexFormatElement export() {
		return new VertexFormatElement(this.uvIndex, this.dataType, this.type, this.length);
	}
}
