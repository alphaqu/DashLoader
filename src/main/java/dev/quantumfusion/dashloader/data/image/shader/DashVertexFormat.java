package dev.quantumfusion.dashloader.data.image.shader;

import com.google.common.collect.ImmutableMap;
import dev.quantumfusion.dashloader.mixin.accessor.VertexFormatAccessor;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;

import java.util.HashMap;
import java.util.Map;

public final class DashVertexFormat {
	private final Map<String, DashVertexFormatElement> elementMap;

	public DashVertexFormat(Map<String, DashVertexFormatElement> elementMap) {
		this.elementMap = elementMap;
	}

	public DashVertexFormat(VertexFormat vertexFormat) {
		var elementMap = ((VertexFormatAccessor) vertexFormat).getElementMap();
		var outElementMap = new HashMap<String, DashVertexFormatElement>();
		elementMap.forEach((s, vertexFormatElement) -> outElementMap.put(s, new DashVertexFormatElement(vertexFormatElement)));
		this.elementMap = outElementMap;
	}

	public VertexFormat export() {
		var outElementMap = new HashMap<String, VertexFormatElement>();
		this.elementMap.forEach((s, dashVertexFormatElement) -> outElementMap.put(s, dashVertexFormatElement.export()));
		return new VertexFormat(ImmutableMap.copyOf(outElementMap));
	}

	public Map<String, DashVertexFormatElement> elementMap() {
		return this.elementMap;
	}
}
