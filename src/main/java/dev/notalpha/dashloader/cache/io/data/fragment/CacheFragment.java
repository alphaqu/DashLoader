package dev.notalpha.dashloader.cache.io.data.fragment;

import dev.notalpha.dashloader.cache.io.fragment.Fragment;

import java.util.ArrayList;
import java.util.List;

public class CacheFragment {
	public final List<StageFragment> stages;
	public final FragmentSlice info;

	public CacheFragment(List<StageFragment> stages, FragmentSlice info) {
		this.stages = stages;
		this.info = info;
	}

	public CacheFragment(Fragment fragment) {
		this.info = new FragmentSlice(fragment);
		this.stages = new ArrayList<>();
		for (Fragment inner : fragment.inner) {
			this.stages.add(new StageFragment(inner));
		}
	}
}
