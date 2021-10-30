package dev.quantumfusion.dashloader.def.data.image.shader;

import com.google.common.collect.ImmutableMap;
import dev.quantumfusion.dashloader.def.mixin.accessor.VertexFormatAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;

import java.util.HashMap;
import java.util.Map;

@Data
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
		elementMap.forEach((s, dashVertexFormatElement) -> outElementMap.put(s, dashVertexFormatElement.export()));
		return new VertexFormat(ImmutableMap.copyOf(outElementMap));
	}

	public Map<String, DashVertexFormatElement> elementMap() {
		return elementMap;
	}
}
