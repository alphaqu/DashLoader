package dev.quantumfusion.dashloader.corehook.holder;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.data.image.shader.DashShader;
import dev.quantumfusion.dashloader.util.MissingDataException;
import dev.quantumfusion.taski.TaskUtil;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.render.Shader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DashShaderData {
	public final Map<String, DashShader> shaders;

	public DashShaderData(Map<String, DashShader> shaders) {
		this.shaders = shaders;
	}

	public DashShaderData(DashDataManager data, StepTask parent) {
		this.shaders = new HashMap<>();
		final Map<String, Shader> minecraftData = data.shaders.getMinecraftData();

		parent.run(new StepTask("Shaders"), (task) -> TaskUtil.forEach(task, minecraftData, (s, shader) -> {
					try {
						this.shaders.put(s, new DashShader(shader));
					} catch (MissingDataException e) {
						DashLoader.LOGGER.warn("Skipping shader {}", s);
					}
				})
		);

	}

	public Map<String, Shader> export() {
		Map<String, Shader> out = new ConcurrentHashMap<>();
		List<Runnable> runnables = new ArrayList<>();
		this.shaders.forEach((key, value) -> runnables.add(() -> out.put(key, value.export())));
		DashLoader.INSTANCE.thread.parallelRunnable(runnables);
		return out;
	}
}
