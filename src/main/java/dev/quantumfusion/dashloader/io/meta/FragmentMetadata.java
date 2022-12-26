package dev.quantumfusion.dashloader.io.meta;

import dev.quantumfusion.dashloader.io.fragment.Fragment;

import java.util.ArrayList;
import java.util.List;

public class FragmentMetadata {
	public final List<StageFragmentMetadata> stages;
	public final FragmentInfo info;

	public FragmentMetadata(List<StageFragmentMetadata> stages, FragmentInfo info) {
		this.stages = stages;
		this.info = info;
	}

	public FragmentMetadata(Fragment fragment) {
		this.info = new FragmentInfo(fragment);
		this.stages = new ArrayList<>();
		for (Fragment inner : fragment.inner) {
			this.stages.add(new StageFragmentMetadata(inner));
		}
	}
}
