package dev.quantumfusion.dashloader.mixin.option.state;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import dev.quantumfusion.dashloader.util.mixins.StateDuck;
import net.minecraft.state.State;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = State.class, priority = 69420)
public abstract class StateMixin<O, S> implements StateDuck<O, S> {

	@Shadow
	@Final
	private ImmutableMap<Property<?>, Comparable<?>> entries;

	@Shadow
	@Final
	protected O owner;

	@Shadow
	private Table<Property<?>, Comparable<?>, S> withTable;
	// comparable to value
	private Property<?>[] propertiesMap;
	private Comparable<?>[][] valuesMap;
	private Object[][] fastWithTable;

	/**
	 * @author notequalalpha
	 */
	@Overwrite
	public <T extends Comparable<T>, V extends T> S with(Property<T> property, V value) {
		var comparable = this.entries.get(property);
		if (comparable == null) {
			throw new IllegalArgumentException("Cannot set property " + property + " as it does not exist in " + this.owner);
		} else if (comparable == value) {
			return (S) this;
		} else {
			if (this.fastWithTable == null) {
				return this.withTable.get(property, value);
			}
			for (int i = 0, propertiesMapLength = this.propertiesMap.length; i < propertiesMapLength; i++) {
				Property<?> prop = this.propertiesMap[i];
				if (property.equals(prop)) {
					Comparable<?>[] comparables = this.valuesMap[i];
					for (int j = 0, comparablesLength = comparables.length; j < comparablesLength; j++) {
						Comparable<?> comp = comparables[j];
						if (value.equals(comp)) {
							return (S) this.fastWithTable[i][j];
						}
					}
				}
			}
			throw new IllegalArgumentException("Cannot set property " + property + " to " + value + " on " + this.owner + ", it is not an allowed value");
		}
	}

	@Override
	public void setPropertiesMap(Property<?>[] propertiesMap) {
		this.propertiesMap = propertiesMap;
	}

	@Override
	public void setValuesMap(Comparable<?>[][] valuesMap) {
		this.valuesMap = valuesMap;
	}

	@Override
	public void setFastWithTable(Object[][] fastWithTable) {
		this.fastWithTable = fastWithTable;
	}
}
