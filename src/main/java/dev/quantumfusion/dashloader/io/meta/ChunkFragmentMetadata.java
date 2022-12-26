package dev.quantumfusion.dashloader.io.meta;

import dev.quantumfusion.dashloader.io.fragment.Fragment;

public final class ChunkFragmentMetadata {
	public final FragmentInfo info;

	public ChunkFragmentMetadata(FragmentInfo info) {
		this.info = info;
	}

	public ChunkFragmentMetadata(Fragment fragment) {
		this.info = new FragmentInfo(fragment);
	}
}
