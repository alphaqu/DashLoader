package dev.quantumfusion.dashloader.def.corehook.holder;

import dev.quantumfusion.dashloader.core.DashLoaderCore;
import dev.quantumfusion.dashloader.def.DashDataManager;
import dev.quantumfusion.dashloader.def.data.image.shader.DashShader;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.taski.TaskUtil;
import dev.quantumfusion.taski.builtin.StepTask;
import net.minecraft.client.render.Shader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class DashShaderData {
	public final Map<String, DashShader> shaders;

	public DashShaderData(Map<String, DashShader> shaders) {
		this.shaders = shaders;
	}

	public DashShaderData(DashDataManager data, StepTask parent) {
		this.shaders = new HashMap<>();
		final Map<String, Shader> minecraftData = data.shaders.getMinecraftData();

		parent.run(new StepTask("Shaders"), (task) -> TaskUtil.forEach(task, minecraftData, (s, shader) -> {
					this.shaders.put(s, new DashShader(shader));
				})
		);

	}

	public Map<String, Shader> export() {
		Map<String, Shader> out = new ConcurrentHashMap<>();
		List<Runnable> runnables = new ArrayList<>();
		shaders.forEach((key, value) -> runnables.add(() -> out.put(key, value.export())));
		DashLoaderCore.THREAD.parallelRunnable(runnables);
		return out;
	}
}
