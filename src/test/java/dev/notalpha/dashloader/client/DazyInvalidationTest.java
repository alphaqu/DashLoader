package dev.notalpha.dashloader.client;

import net.minecraft.client.resources.model.sprite.SpriteGetter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DazyInvalidationTest {
	private static final SpriteGetter ATLAS = newGetter();
	private static final SpriteGetter OTHER_ATLAS = newGetter();

	private static SpriteGetter newGetter() {
		return (SpriteGetter) Proxy.newProxyInstance(
				DazyInvalidationTest.class.getClassLoader(),
				new Class<?>[]{SpriteGetter.class},
				(proxy, method, args) -> switch (method.getName()) {
					case "hashCode" -> System.identityHashCode(proxy);
					case "equals" -> proxy == args[0];
					default -> null;
				});
	}

	private static final class Counting extends Dazy<String> {
		int resolveCalls;

		@Override
		protected String resolve(SpriteGetter spriteLoader) {
			this.resolveCalls++;
			return spriteLoader == ATLAS ? "vanilla" : "faithful";
		}
	}

	@Test
	void memoizesWithinOneAtlas() {
		Counting dazy = new Counting();

		String first = dazy.get(ATLAS);
		String second = dazy.get(ATLAS);

		assertSame(first, second);
		assertEquals(1, dazy.resolveCalls);
	}

	@Test
	void reResolvesOnANewAtlas() {
		Counting dazy = new Counting();

		dazy.get(ATLAS);
		dazy.get(OTHER_ATLAS);

		assertEquals(2, dazy.resolveCalls);
	}

	@Test
	void reResolvesWhenReturningToTheFirstAtlas() {
		Counting dazy = new Counting();

		dazy.get(ATLAS);
		dazy.get(OTHER_ATLAS);
		dazy.get(ATLAS);

		assertEquals(3, dazy.resolveCalls);
	}

	@Test
	void valueFollowsTheCurrentAtlas() {
		Counting dazy = new Counting();

		assertEquals("vanilla", dazy.get(ATLAS));
		assertEquals("faithful", dazy.get(OTHER_ATLAS));
		assertEquals("vanilla", dazy.get(ATLAS));
	}
}