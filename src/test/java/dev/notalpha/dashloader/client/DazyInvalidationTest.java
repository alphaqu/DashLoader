package dev.notalpha.dashloader.client;

import net.minecraft.client.render.model.ErrorCollectingSpriteGetter;
import net.minecraft.client.render.model.SimpleModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DazyInvalidationTest {
	private static final class Counting extends Dazy<String> {
		private final String value;
		int resolveCalls;

		Counting(String value) {
			this.value = value;
		}

		@Override
		protected String resolve(ErrorCollectingSpriteGetter spriteLoader) {
			this.resolveCalls++;
			return this.value;
		}
	}

	private static final ErrorCollectingSpriteGetter ATLAS = new ErrorCollectingSpriteGetter() {
		@Override
		public Sprite get(SpriteIdentifier id, SimpleModel model) {
			return null;
		}

		@Override
		public Sprite getMissing(String name, SimpleModel model) {
			return null;
		}
	};

	private static final ErrorCollectingSpriteGetter OTHER_ATLAS = new ErrorCollectingSpriteGetter() {
		@Override
		public Sprite get(SpriteIdentifier id, SimpleModel model) {
			return null;
		}

		@Override
		public Sprite getMissing(String name, SimpleModel model) {
			return null;
		}
	};

	@Test
	void memoizesWithinOneAtlas() {
		Counting dazy = new Counting("a");

		String first = dazy.get(ATLAS);
		String second = dazy.get(ATLAS);

		assertSame(first, second);
		assertEquals(1, dazy.resolveCalls);
	}

	@Test
	void reResolvesOnANewAtlas() {
		Counting dazy = new Counting("a");

		dazy.get(ATLAS);
		dazy.get(OTHER_ATLAS);

		assertEquals(2, dazy.resolveCalls);
	}

	@Test
	void reResolvesWhenReturningToTheFirstAtlas() {
		Counting dazy = new Counting("a");

		dazy.get(ATLAS);
		dazy.get(OTHER_ATLAS);
		dazy.get(ATLAS);

		assertEquals(3, dazy.resolveCalls);
	}

	@Test
	void valueFollowsTheCurrentAtlas() {
		Dazy<String> dazy = new Dazy<>() {
			@Override
			protected String resolve(ErrorCollectingSpriteGetter spriteLoader) {
				return spriteLoader == ATLAS ? "vanilla" : "faithful";
			}
		};

		assertEquals("vanilla", dazy.get(ATLAS));
		assertEquals("faithful", dazy.get(OTHER_ATLAS));
		assertEquals("vanilla", dazy.get(ATLAS));
	}
}