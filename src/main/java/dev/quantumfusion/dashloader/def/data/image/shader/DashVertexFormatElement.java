package dev.quantumfusion.dashloader.def.data.image.shader;

import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.VertexFormatElement;

@Data
public record DashVertexFormatElement(
		VertexFormatElement.DataType dataType, VertexFormatElement.Type type, int textureIndex, int length) {

	public DashVertexFormatElement(VertexFormatElement vertexFormatElement) {
		this(vertexFormatElement.getDataType(), vertexFormatElement.getType(), vertexFormatElement.getTextureIndex(), vertexFormatElement.getLength());
	}

	public VertexFormatElement toUndash() {
		return new VertexFormatElement(textureIndex, dataType, type, length);
	}
}
