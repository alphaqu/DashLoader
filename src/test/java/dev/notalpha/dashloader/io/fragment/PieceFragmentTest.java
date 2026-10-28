package dev.notalpha.dashloader.io.fragment;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PieceFragmentTest {
	private static final int LEAF_SIZE = 100;
	private static final int MAX_LEAF_SIZE = LEAF_SIZE + 10;
	private static final int[][] SHAPE = {{4, 10}, {3, 7}, {5, 13}, {2, 4}};

	private static SimplePiece buildTree() {
		SimplePiece[] stages = new SimplePiece[SHAPE.length];
		for (int s = 0; s < SHAPE.length; s++) {
			SimplePiece[] chunks = new SimplePiece[SHAPE[s].length];
			for (int c = 0; c < SHAPE[s].length; c++) {
				SizePiece[] leaves = new SizePiece[SHAPE[s][c]];
				for (int e = 0; e < SHAPE[s][c]; e++) {
					leaves[e] = new SizePiece(LEAF_SIZE + ((s * 31 + c * 7 + e) % 11));
				}
				chunks[c] = new SimplePiece(leaves);
			}
			stages[s] = new SimplePiece(chunks);
		}
		return new SimplePiece(new SimplePiece[]{new SimplePiece(stages)});
	}

	private static int totalLeaves() {
		int sum = 0;
		for (int[] stage : SHAPE) {
			for (int n : stage) {
				sum += n;
			}
		}
		return sum;
	}

	private static int countEntries(Fragment fragment, int depth) {
		if (depth == 3) {
			return fragment.endIndex - fragment.startIndex;
		}
		int sum = 0;
		for (Fragment inner : fragment.inner) {
			sum += countEntries(inner, depth + 1);
		}
		return sum;
	}

	@Test
	void fragmentsCoverEveryLeafExactlyOnce() {
		for (int fragmentCount : new int[]{1, 2, 3, 4, 8, 16}) {
			SimplePiece piece = buildTree();
			long expectedSize = piece.size;
			long remaining = expectedSize;
			int covered = 0;

			for (int i = 0; i < fragmentCount; i++) {
				long budget = remaining / (fragmentCount - i);
				if (i == fragmentCount - 1) {
					budget = Long.MAX_VALUE;
				}
				Fragment fragment = piece.fragment(budget);
				remaining -= fragment.size;
				covered += countEntries(fragment, 0);

				if (i < fragmentCount - 1) {
					assertTrue(fragment.size <= budget + MAX_LEAF_SIZE,
							"fragmentCount=" + fragmentCount + ": Fragment " + i + " ist " + fragment.size
									+ " B gross, erlaubt waeren " + (budget + MAX_LEAF_SIZE) + " B");
				}
			}

			assertEquals(0, remaining,
					"fragmentCount=" + fragmentCount + ": nach allen Fragmenten ist noch Rest uebrig");
			assertEquals(totalLeaves(), covered,
					"fragmentCount=" + fragmentCount + ": Blaetter nicht genau einmal abgedeckt");
		}
	}

	@Test
	void singleFragmentTakesEverything() {
		SimplePiece piece = buildTree();
		long expectedSize = piece.size;

		Fragment fragment = piece.fragment(Long.MAX_VALUE);

		assertEquals(expectedSize, fragment.size);
		assertEquals(totalLeaves(), countEntries(fragment, 0));
	}
}
