package net.oskarstrom.dashloader.def.image.shader;

import com.google.common.collect.ImmutableMap;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.VertexFormat;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.mixin.accessor.VertexFormatAccessor;
import org.apache.commons.lang3.tuple.Pair;


import java.util.Map;

public class DashVertexFormat {
	@Serialize(order = 0)
	public Map<String, DashVertexFormatElement> elementMap;

	public DashVertexFormat(@Deserialize("elementMap") Map<String, DashVertexFormatElement> elementMap) {
		this.elementMap = elementMap;
	}

	public DashVertexFormat(VertexFormat vertexFormat) {
		this.elementMap = DashHelper.convertMap(((VertexFormatAccessor) vertexFormat).getElementMap(), entry -> Pair.of(entry.getKey(), new DashVertexFormatElement(entry.getValue())));
	}

	public VertexFormat toUndash() {
		return new VertexFormat(ImmutableMap.copyOf(DashHelper.convertMap(elementMap, entry -> Pair.of(entry.getKey(), entry.getValue().toUndash()))));
	}
}
