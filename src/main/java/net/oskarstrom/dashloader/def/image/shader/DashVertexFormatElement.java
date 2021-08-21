package net.oskarstrom.dashloader.def.image.shader;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.VertexFormatElement;

public class DashVertexFormatElement {
	@Serialize(order = 0)
	public final VertexFormatElement.DataType dataType;
	@Serialize(order = 1)
	public final VertexFormatElement.Type type;
	@Serialize(order = 2)
	public final int textureIndex;
	@Serialize(order = 3)
	public final int length;

	public DashVertexFormatElement(@Deserialize("dataType") VertexFormatElement.DataType dataType,
								   @Deserialize("type") VertexFormatElement.Type type,
								   @Deserialize("textureIndex") int textureIndex,
								   @Deserialize("length") int length) {
		this.dataType = dataType;
		this.type = type;
		this.textureIndex = textureIndex;
		this.length = length;

	}

	public DashVertexFormatElement(VertexFormatElement vertexFormatElement) {
		dataType = vertexFormatElement.getDataType();
		type = vertexFormatElement.getType();
		textureIndex = vertexFormatElement.getTextureIndex();
		length = vertexFormatElement.getLength();
	}

	public VertexFormatElement toUndash() {
		return new VertexFormatElement(textureIndex, dataType, type, length);
	}
}
