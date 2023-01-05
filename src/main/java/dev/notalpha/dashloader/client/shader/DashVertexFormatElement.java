package dev.notalpha.dashloader.client.shader;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.registry.RegistryReader;
import net.minecraft.client.render.VertexFormatElement;

public class DashVertexFormatElement implements DashObject<VertexFormatElement> {
	public final VertexFormatElement.ComponentType componentType;
	public final VertexFormatElement.Type type;
	public final int uvIndex;
	public final int componentCount;

	public DashVertexFormatElement(VertexFormatElement.ComponentType componentType, VertexFormatElement.Type type, int uvIndex, int componentCount) {
		this.componentType = componentType;
		this.type = type;
		this.uvIndex = uvIndex;
		this.componentCount = componentCount;
	}

	public DashVertexFormatElement(VertexFormatElement element) {
		this.componentType = element.getComponentType();
		this.type = element.getType();
		this.uvIndex = element.getUvIndex();
		this.componentCount = element.getComponentCount();
	}


	@Override
	public VertexFormatElement export(RegistryReader reader) {
		return new VertexFormatElement(this.uvIndex, this.componentType, this.type, this.componentCount);
	}
}
