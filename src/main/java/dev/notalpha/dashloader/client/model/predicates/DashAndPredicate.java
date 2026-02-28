package dev.notalpha.dashloader.client.model.predicates;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import net.minecraft.client.renderer.block.model.multipart.CombinedCondition;
import net.minecraft.client.renderer.block.model.multipart.Condition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DashAndPredicate implements DashObject<CombinedCondition, CombinedCondition> {
public final int[] selectors;

public DashAndPredicate(int[] selectors) {
this.selectors = selectors;
}

public DashAndPredicate(CombinedCondition selector, RegistryWriter writer) {
	this.selectors = new int[selector.terms().size()];
	for (int i = 0; i < selector.terms().size(); i++) {
		this.selectors[i] = writer.add(selector.terms().get(i));
	}
}

@Override
public CombinedCondition export(RegistryReader handler) {
final List<Condition> selectors = new ArrayList<>(this.selectors.length);
for (int accessSelector : this.selectors) {
selectors.add(handler.get(accessSelector));
}

return new CombinedCondition(CombinedCondition.Operation.AND, selectors);
}

@Override
public boolean equals(Object o) {
if (this == o) return true;
if (o == null || getClass() != o.getClass()) return false;

DashAndPredicate that = (DashAndPredicate) o;

return Arrays.equals(selectors, that.selectors);
}

@Override
public int hashCode() {
return Arrays.hashCode(selectors);
}
}
