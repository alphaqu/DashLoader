package net.oskarstrom.dashloader.def.datatest;

import net.oskarstrom.dashloader.def.TestUtils;
import net.oskarstrom.dashloader.def.blockstate.DashBlockState;
import net.oskarstrom.dashloader.def.blockstate.property.DashBooleanProperty;
import net.oskarstrom.dashloader.def.blockstate.property.DashDirectionProperty;
import net.oskarstrom.dashloader.def.blockstate.property.DashEnumProperty;
import net.oskarstrom.dashloader.def.blockstate.property.DashIntProperty;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashBooleanValue;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashDirectionValue;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashEnumValue;
import net.oskarstrom.dashloader.def.blockstate.property.value.DashIntValue;
import net.oskarstrom.dashloader.def.common.DashIdentifier;
import net.oskarstrom.dashloader.def.common.DashModelIdentifier;
import net.oskarstrom.dashloader.def.font.*;
import net.oskarstrom.dashloader.def.image.DashSprite;
import net.oskarstrom.dashloader.def.model.components.DashBakedQuad;
import net.oskarstrom.dashloader.def.model.predicates.DashAndPredicate;
import net.oskarstrom.dashloader.def.model.predicates.DashOrPredicate;
import net.oskarstrom.dashloader.def.model.predicates.DashSimplePredicate;
import net.oskarstrom.dashloader.def.model.predicates.DashStaticPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@DisplayName("RegistryData Serialization Test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RegistryDataTest {


	@Test
	public void testBlockStateRegistryData() {
		TestUtils.testCreation(DashBlockState.class);
	}

	@Test
	public void testFontRegistryData() {
		TestUtils.testCreation(DashBitmapFont.class, DashBlankFont.class, DashTrueTypeFont.class, DashUnicodeFont.class);
	}

	@Test
	public void testIdentifierRegistryData() {
		TestUtils.testCreation(DashIdentifier.class, DashModelIdentifier.class);
	}

	@Test
	public void testPropertyRegistryData() {
		TestUtils.testCreation(DashBooleanProperty.class, DashDirectionProperty.class, DashEnumProperty.class, DashIntProperty.class);
	}

	@Test
	public void testPropertyValueRegistryData() {
		TestUtils.testCreation(DashBooleanValue.class, DashDirectionValue.class, DashEnumValue.class, DashIntValue.class);
	}

	@Test
	public void testSpriteRegistryData() {
		TestUtils.testCreation(DashSprite.class);
	}

	@Test
	public void testPredicateRegistryData() {
		TestUtils.testCreation(DashAndPredicate.class, DashOrPredicate.class, DashSimplePredicate.class, DashStaticPredicate.class);
	}

	@Test
	public void testRegistryBakedQuadData() {
		TestUtils.testCreation(DashBakedQuad.class);
	}


}
