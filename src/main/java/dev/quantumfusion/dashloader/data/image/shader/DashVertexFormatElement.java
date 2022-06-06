package dev.quantumfusion.dashloader.data.image.shader;

import net.minecraft.client.render.VertexFormatElement;

public record DashVertexFormatElement(
		VertexFormatElement.DataType dataType, VertexFormatElement.Type type, int textureIndex, int length) {

	public DashVertexFormatElement(VertexFormatElement vertexFormatElement) {
		this(vertexFormatElement.getDataType(), vertexFormatElement.getType(), vertexFormatElement.getTextureIndex(), vertexFormatElement.getLength());
	}

	public VertexFormatElement export() {
		return new VertexFormatElement(this.textureIndex, this.dataType, this.type, this.length);
	}
}
