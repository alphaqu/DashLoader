package dev.quantumfusion.dashloader.data.image.shader;

import net.minecraft.client.render.VertexFormatElement;

public final class DashVertexFormatElement {
	public final VertexFormatElement.DataType dataType;
	public final VertexFormatElement.Type type;
	public final int textureIndex;
	public final int length;
	public final int byteLength;

	public DashVertexFormatElement(VertexFormatElement.DataType dataType, VertexFormatElement.Type type, int textureIndex, int length, int byteLength) {
		this.dataType = dataType;
		this.type = type;
		this.textureIndex = textureIndex;
		this.length = length;
		this.byteLength = byteLength;
	}


	public VertexFormatElement export() {
		return new VertexFormatElement(this.textureIndex, this.dataType, this.type, this.length);
	}
}
