package dev.quantumfusion.dashloader.def.data.model;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.state.StateManager;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class ModelVariables {
	public Pair<List<MultipartModelSelector>, StateManager<Block, BlockState>> selector;

	public ModelVariables(Pair<List<MultipartModelSelector>, StateManager<Block, BlockState>> selector) {
		this.selector = selector;
	}
}
