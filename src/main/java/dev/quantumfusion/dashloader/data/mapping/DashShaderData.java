package dev.quantumfusion.dashloader.data.mapping;

import dev.quantumfusion.dashloader.DashDataManager;
import dev.quantumfusion.dashloader.DashLoader;
import dev.quantumfusion.dashloader.data.image.shader.DashShader;
import dev.quantumfusion.dashloader.util.MissingDataException;
import dev.quantumfusion.taski.TaskUtil;
import dev.quantumfusion.taski.builtin.StepTask;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.render.Shader;
import static dev.quantumfusion.dashloader.DashLoader.DL;

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
						DashLoader.LOG.warn("Skipping shader {}", s);
					}
				})
		);

	}

	public Map<String, Shader> export() {
		Map<String, Shader> out = new ConcurrentHashMap<>();
		List<Runnable> runnables = new ArrayList<>();
		this.shaders.forEach((key, value) -> runnables.add(() -> out.put(key, value.export())));
		DL.thread.parallelRunnable(runnables);
		return out;
	}
}
