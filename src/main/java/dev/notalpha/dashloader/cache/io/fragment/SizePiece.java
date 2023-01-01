package dev.notalpha.dashloader.cache.io.fragment;

public class SizePiece extends Piece {
	public SizePiece(long size) {
		super(size);
	}

	@Override
	public Piece[] getInner() {
		return null;
	}

	@Override
	public String toString() {
		return "";
	}
}
