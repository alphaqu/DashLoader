package dev.quantumfusion.dashloader.io.meta;

import dev.quantumfusion.dashloader.io.fragment.Fragment;
import dev.quantumfusion.hyphen.io.ByteBufferIO;

import java.util.ArrayList;
import java.util.List;

public class StageFragmentMetadata {
	public final List<ChunkFragmentMetadata> chunks;
	public final FragmentInfo info;

	public StageFragmentMetadata(List<ChunkFragmentMetadata> chunks, FragmentInfo info) {
		this.chunks = chunks;
		this.info = info;
	}

	public StageFragmentMetadata(Fragment fragment) {
		this.info = new FragmentInfo(fragment);
		this.chunks = new ArrayList<>();
		for (Fragment inner : fragment.inner) {
			this.chunks.add(new ChunkFragmentMetadata(inner));
		}
	}
}
