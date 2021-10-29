package dev.quantumfusion.dashloader.def.data.image.shader;

import com.google.common.collect.ImmutableMap;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.render.VertexFormat;
import net.oskarstrom.dashloader.core.util.DashHelper;
import dev.quantumfusion.dashloader.def.mixin.accessor.VertexFormatAccessor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

@Data
public record DashVertexFormat(Map<String, DashVertexFormatElement> elementMap) {

	public DashVertexFormat(VertexFormat vertexFormat) {
		this(DashHelper.convertMap(((VertexFormatAccessor) vertexFormat).getElementMap(), entry -> Pair.of(entry.getKey(), new DashVertexFormatElement(entry.getValue()))));
	}

	public VertexFormat toUndash() {
		return new VertexFormat(ImmutableMap.copyOf(DashHelper.convertMap(elementMap, entry -> Pair.of(entry.getKey(), entry.getValue().toUndash()))));
	}
}
