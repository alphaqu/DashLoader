package dev.notalpha.dashloader.client.model.predicates;

import dev.notalpha.dashloader.api.DashObject;
import dev.notalpha.dashloader.api.collection.ObjectObjectList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import net.minecraft.client.renderer.block.model.multipart.KeyValueCondition;

import java.util.HashMap;
import java.util.Map;

public final class DashSimplePredicate implements DashObject<KeyValueCondition, KeyValueCondition> {
public final ObjectObjectList<String, String> tests;

public DashSimplePredicate(ObjectObjectList<String, String> tests) {
this.tests = tests;
}

public DashSimplePredicate(KeyValueCondition simpleMultipartModelSelector) {
this.tests = new ObjectObjectList<>();
simpleMultipartModelSelector.tests().forEach((key, value) -> this.tests.put(key, value.toString()));
}

@Override
public KeyValueCondition export(RegistryReader handler) {
Map<String, KeyValueCondition.Terms> out = new HashMap<>(this.tests.list().size());
this.tests.forEach((key, value) -> out.put(
		key,
		KeyValueCondition.Terms.parse(value).result().orElseThrow(() -> new IllegalStateException("Invalid key-value condition term: " + value))
));
return new KeyValueCondition(out);
}

@Override
public boolean equals(Object o) {
if (this == o) return true;
if (o == null || getClass() != o.getClass()) return false;

DashSimplePredicate that = (DashSimplePredicate) o;

return tests.equals(that.tests);
}

@Override
public int hashCode() {
return tests.hashCode();
}
}
