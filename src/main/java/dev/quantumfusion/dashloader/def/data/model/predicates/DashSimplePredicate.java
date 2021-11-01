package dev.quantumfusion.dashloader.def.data.model.predicates;

import com.google.common.base.Splitter;
import dev.quantumfusion.dashloader.core.api.annotation.DashObject;
import dev.quantumfusion.dashloader.core.common.IntIntList;
import dev.quantumfusion.dashloader.core.registry.DashRegistryReader;
import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.data.blockstate.property.value.DashPropertyValueManager;
import dev.quantumfusion.dashloader.def.mixin.accessor.SimpleMultipartModelSelectorAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.json.SimpleMultipartModelSelector;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Data
@DashObject(SimpleMultipartModelSelector.class)
public class DashSimplePredicate implements DashPredicate {
	private static final Splitter VALUE_SPLITTER = Splitter.on('|').omitEmptyStrings();

	public final IntIntList properties;
	public final boolean negate;

	public DashSimplePredicate(IntIntList properties,
			boolean negate) {
		this.properties = properties;
		this.negate = negate;
	}


	public DashSimplePredicate(SimpleMultipartModelSelector simpleMultipartModelSelector, DashRegistryWriter writer) {
		StateManager<Block, BlockState> stateManager = DashLoader.getData().getWriteContextData().stateManagers.get(simpleMultipartModelSelector);
		SimpleMultipartModelSelectorAccessor access = ((SimpleMultipartModelSelectorAccessor) simpleMultipartModelSelector);
		Property<?> stateManagerProperty = stateManager.getProperty(access.getKey());
		properties = new IntIntList(new ArrayList<>());
		String string = access.getValueString();
		negate = !string.isEmpty() && string.charAt(0) == '!';
		if (negate) {
			string = string.substring(1);
		}
		List<String> list = VALUE_SPLITTER.splitToList(string);
		if (list.size() == 1) {
			Pair<Integer, Integer> predicateProperty = createPredicateInfo(stateManager, stateManagerProperty, string, writer);
			properties.put(predicateProperty.getLeft(), predicateProperty.getRight());
		} else {
			List<Pair<Integer, Integer>> predicateProperties = list.stream().map((stringx) -> createPredicateInfo(stateManager, stateManagerProperty, stringx, writer)).collect(Collectors.toList());
			predicateProperties.forEach(pair -> properties.put(pair.getLeft(), pair.getRight()));
		}

	}


	private Pair<Integer, Integer> createPredicateInfo(StateManager<Block, BlockState> stateFactory, Property<?> property, String valueString, DashRegistryWriter writer) {
		Optional<?> optional = property.parse(valueString);
		if (optional.isEmpty()) {
			throw new RuntimeException(String.format("Unknown value '%s' '%s'", valueString, stateFactory.getOwner().toString()));
		} else {
			return Pair.of(writer.add(property), writer.add(DashPropertyValueManager.prepare((Comparable<?>) optional.get())));
		}
	}

	@Override
	public Predicate<BlockState> export(DashRegistryReader handler) {
		List<Map.Entry<? extends Property<?>, ? extends Comparable<?>>> out = new ArrayList<>();
		properties.forEach((key, value) -> out.add(Pair.of(handler.get(key), handler.get(value))));
		Predicate<BlockState> outPredicate;
		if (out.size() == 1) {
			final Map.Entry<? extends Property<?>, ? extends Comparable<?>> entry = out.get(0);
			outPredicate = createPredicate(entry);
		} else {
			List<Predicate<BlockState>> list2 = out.stream().map(this::createPredicate).collect(Collectors.toList());
			outPredicate = (blockState) -> list2.stream().anyMatch((predicate) -> predicate.test(blockState));

		}
		return negate ? outPredicate.negate() : outPredicate;
	}


	private Predicate<BlockState> createPredicate(Map.Entry<? extends Property<?>, ? extends Comparable<?>> entry) {
		return (blockState) -> blockState.get(entry.getKey()).equals(entry.getValue());
	}

}
