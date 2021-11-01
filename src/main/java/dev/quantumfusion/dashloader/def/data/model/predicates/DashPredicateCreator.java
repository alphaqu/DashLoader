package dev.quantumfusion.dashloader.def.data.model.predicates;

import dev.quantumfusion.dashloader.core.registry.DashRegistryWriter;
import dev.quantumfusion.dashloader.def.util.BooleanSelector;
import net.minecraft.client.render.model.json.AndMultipartModelSelector;
import net.minecraft.client.render.model.json.MultipartModelSelector;
import net.minecraft.client.render.model.json.OrMultipartModelSelector;
import net.minecraft.client.render.model.json.SimpleMultipartModelSelector;

public class DashPredicateCreator {

	public static DashPredicate create(MultipartModelSelector selector, DashRegistryWriter writer) {
		if (selector == MultipartModelSelector.TRUE) return new DashStaticPredicate(true);
		else if (selector == MultipartModelSelector.FALSE) return new DashStaticPredicate(false);
		else if (selector instanceof AndMultipartModelSelector s) return new DashAndPredicate(s, writer);
		else if (selector instanceof OrMultipartModelSelector s) return new DashOrPredicate(s, writer);
		else if (selector instanceof SimpleMultipartModelSelector s) return new DashSimplePredicate(s, writer);
		else if (selector instanceof BooleanSelector s) return new DashStaticPredicate(s.selector);
		else throw new RuntimeException("someone is having fun with lambda selectors again");
	}
}
