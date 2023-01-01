package dev.notalpha.dashloader.io.data.fragment;

import com.github.luben.zstd.Zstd;
import dev.notalpha.dashloader.api.Dashable;
import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.io.fragment.Fragment;
import dev.notalpha.dashloader.util.IOHelper;
import dev.quantumfusion.hyphen.HyphenSerializer;
import dev.quantumfusion.hyphen.io.ByteBufferIO;
import dev.quantumfusion.taski.builtin.StepTask;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class FragmentSlice {
	public final int rangeStart;
	public final int rangeEnd;
	public final long fileSize;

	public FragmentSlice(int rangeStart, int rangeEnd, long fileSize) {
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
		this.fileSize = fileSize;
	}

	public FragmentSlice(Fragment fragment) {
		this.rangeStart = fragment.startIndex;
		this.rangeEnd = fragment.endIndex;
		this.fileSize = fragment.size;
	}
}
