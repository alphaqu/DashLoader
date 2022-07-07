package dev.quantumfusion.dashloader.data.model.predicates;

import dev.quantumfusion.dashloader.api.DashDependencies;
import dev.quantumfusion.dashloader.api.DashObject;
import dev.quantumfusion.dashloader.data.DashIdentifierInterface;
import dev.quantumfusion.dashloader.data.blockstate.DashBlockState;
import dev.quantumfusion.dashloader.mixin.accessor.ModelLoaderAccessor;
import dev.quantumfusion.dashloader.mixin.accessor.SimpleMultipartModelSelectorAccessor;
import dev.quantumfusion.dashloader.registry.RegistryReader;
import dev.quantumfusion.dashloader.registry.RegistryWriter;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.SimpleMultipartModelSelector;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import static dev.quantumfusion.dashloader.DashLoader.DL;


@DashObject(SimpleMultipartModelSelector.class)
@DashDependencies(DashIdentifierInterface.class)
public final class DashSimplePredicate implements DashPredicate {
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
		return new SimpleMultipartModelSelector(this.key, this.valueString).getPredicate(getStateManager(handler.get(this.identifier)));
	}

	@NotNull
	public static Identifier getStateManagerIdentifier(MultipartModelSelector multipartModelSelector) {
		StateManager<Block, BlockState> stateManager = DL.getData().getWriteContextData().stateManagers.get(multipartModelSelector);
		Identifier identifier;
		if (stateManager == ModelLoaderAccessor.getTheItemFrameThing()) {
			identifier = DashBlockState.ITEM_FRAME;
		} else {
			identifier = Registry.BLOCK.getId(stateManager.getOwner());
		}
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
