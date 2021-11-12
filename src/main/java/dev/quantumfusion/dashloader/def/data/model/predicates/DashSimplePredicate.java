package dev.quantumfusion.dashloader.def.data.model.predicates;

import dev.quantumfusion.dashloader.core.api.DashDependencies;
import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.core.registry.RegistryWriter;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.def.data.blockstate.DashBlockState;
import dev.quantumfusion.dashloader.def.mixin.accessor.ModelLoaderAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.SimpleMultipartModelSelectorAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.SimpleMultipartModelSelector;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;


@Data
@DashObject(SimpleMultipartModelSelector.class)
@DashDependencies(DashIdentifierInterface.class)
public class DashSimplePredicate implements DashPredicate {
	public final String key;
	public final String valueString;
	public final int identifier;

	public DashSimplePredicate(String key, String valueString, int identifier) {
		this.key = key;
		this.valueString = valueString;
		this.identifier = identifier;
	}

	public DashSimplePredicate(SimpleMultipartModelSelector simpleMultipartModelSelector, RegistryWriter writer) {
		var access = ((SimpleMultipartModelSelectorAccessor) simpleMultipartModelSelector);
		this.identifier = writer.add(getStateManagerIdentifier(simpleMultipartModelSelector));
		this.key = access.getKey();
		this.valueString = access.getValueString();
	}

	@Override
	public Predicate<BlockState> export(RegistryReader handler) {
		return new SimpleMultipartModelSelector(key, valueString).getPredicate(getStateManager(handler.get(identifier)));
	}

	@NotNull
	public static Identifier getStateManagerIdentifier(MultipartModelSelector multipartModelSelector) {
		StateManager<Block, BlockState> stateManager = DashLoader.getData().getWriteContextData().stateManagers.get(multipartModelSelector);
		Identifier identifier;
		if (stateManager == ModelLoaderAccessor.getTheItemFrameThing()) identifier = DashBlockState.ITEM_FRAME;
		else identifier = Registry.BLOCK.getId(stateManager.getOwner());
		return identifier;
	}

	public static StateManager<Block, BlockState> getStateManager(Identifier identifier) {
		if (identifier.equals(DashBlockState.ITEM_FRAME)) {
			return ModelLoaderAccessor.getTheItemFrameThing();
		} else {
			return Registry.BLOCK.get(identifier).getStateManager();
		}
	}


}
