package net.oskarstrom.dashloader.def.data.serialize.mapping;

import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import net.minecraft.client.render.Shader;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.DashLoader;
import net.oskarstrom.dashloader.def.data.VanillaData;
import net.oskarstrom.dashloader.def.image.shader.DashShader;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class DashShaderData {
	@Serialize(order = 0)
	public final Map<String, DashShader> shaders;


	public DashShaderData(@Deserialize("shaders") Map<String, DashShader> shaders) {
		this.shaders = shaders;
	}

	public DashShaderData(VanillaData data, DashLoader.TaskHandler taskHandler) {
		taskHandler.setSubtasks(1);
		this.shaders = DashHelper.convertMap(data.getShaderData(), entry -> Pair.of(entry.getKey(), new DashShader(entry.getValue())));
		taskHandler.completedSubTask();
	}

	public Map<String, Shader> toUndash() {
		Map<String, Shader> out = new ConcurrentHashMap<>();
		List<Runnable> callables = new ArrayList<>();
		shaders.forEach((key, value) -> callables.add(() -> out.put(key, value.toUndash())));
		try {
			DashLoader.getInstance().getCoreManager().getThreadManager().executeRunnables(callables);
		} catch (ExecutionException | InterruptedException e) {
			e.printStackTrace();
		}
		return out;
	}
}
