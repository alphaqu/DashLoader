package dev.quantumfusion.dashloader.io.serializer;// package dev.quantumfusion.dashloader.io.serializer;
//
//import dev.quantumfusion.dashloader.DashLoaderHandlers;
//import dev.quantumfusion.hyphen.io.UnsafeIO;
//import dev.quantumfusion.zstdcomp.zstd.ZstdCompressor;
//import dev.quantumfusion.zstdcomp.zstd.ZstdFrameCompressor;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.concurrent.Callable;
//
//public final class FragmentationCompressor {
//	private static final int HEADER_SIZE = 4;
//	private static final int FRAGMENT_HEADER_SIZE = 8;
//
//	public static void main(String[] args) {
//		compress(null, 9004, 5);
//	}
//
//	public static long compress(UnsafeIO in, int inSize, int fragments) {
//		int fragmentSize = inSize / fragments;
//
//		// Compress
//		ArrayList<Callable<Result>> jobs = new ArrayList<>();
//		for (int i = 0; i < fragments; i++) {
//			int inStart = (fragmentSize * i);
//			int inEnd = i == fragments - 1 ? inSize : (fragmentSize * (i + 1));
//			jobs.add(() -> {
//				int maxLength = ZstdCompressor.maxCompressedLength(inEnd - inStart);
//				UnsafeIO out = UnsafeIO.create(maxLength);
//				int compress = ZstdFrameCompressor.compress(
//						null,
//						in.address() + inStart,
//						in.address() + inEnd,
//						null,
//						out.address(),
//						out.address() + maxLength,
//						3
//				);
//
//				return new Result(out, compress, inStart, inEnd);
//			});
//			System.out.println(inStart + "-" + inEnd);
//		}
//
//		Collection<Result> results = DashLoaderCore.THREAD.parallelCallable(jobs);
//
//		// Stitching
//		int outputSize = HEADER_SIZE;
//		for (Result result : results) {
//			outputSize += result.length + FRAGMENT_HEADER_SIZE;
//		}
//
//		UnsafeIO out = UnsafeIO.create(outputSize);
//		out.putInt(fragments);
//
//		for (Result result : results) {
//
//		}
//
//		return 0;
//
//	}
//
//	private record Result(UnsafeIO io, int length, int start, int end) {
//	}
//}