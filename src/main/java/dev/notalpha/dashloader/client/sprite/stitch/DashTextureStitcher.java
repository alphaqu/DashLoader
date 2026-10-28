package dev.notalpha.dashloader.client.sprite.stitch;

import dev.notalpha.dashloader.DashLoader;
import dev.notalpha.dashloader.api.collection.IntObjectList;
import dev.notalpha.dashloader.api.registry.RegistryReader;
import dev.notalpha.dashloader.api.registry.RegistryWriter;
import dev.notalpha.dashloader.mixin.accessor.StitcherAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class DashTextureStitcher<T extends Stitcher.Entry> extends Stitcher<T> {
	@Nullable
	private ExportedData<T> data;
	private int remainingSlots;
	private final int padding;

	public DashTextureStitcher(int maxWidth, int maxHeight, int mipLevel, int anisotropy, @Nullable ExportedData<T> data) {
		super(maxWidth, maxHeight, mipLevel, anisotropy);
		this.data = data;
		this.remainingSlots = data == null ? 0 : data.slots.size();
		this.padding = 1 << mipLevel << Mth.clamp(anisotropy - 1, 0, 4);
	}

	@Override
	public int getWidth() {
		if (this.data == null) {
			return super.getWidth();
		}
		return data.width;
	}

	@Override
	public int getHeight() {
		if (this.data == null) {
			return super.getHeight();
		}
		return data.height;
	}

	@Override
	public void registerSprite(T info) {
		if (data == null) {
			super.registerSprite(info);
			return;
		}

		// If it starts recaching, doRecache will re-add the entries to the list.
		var id = info.name();
		var slot = data.slots.get(id);
		if (slot == null) {
			DashLoader.LOG.warn("Sprite {} was not cached last time.", id);

			doFallback();
			// This was never added to the slot, so it would not get added to super.
			this.registerSprite(info);
			return;
		}

		if (slot.contents != null) {
			DashLoader.LOG.warn("Sprite {} was added twice??", id);
		}

		remainingSlots -= 1;
		slot.contents = info;

		if (slot.width != info.width() || slot.height != info.height()) {
			DashLoader.LOG.warn("Sprite {} had changed dimensions since last launch, falling back.", id);
			doFallback();
		}
	}

	public void doFallback() {
		if (data != null) {
			DashLoader.LOG.error("Using fallback on texture stitcher.");
			var slots = data.slots;
			data = null;
			slots.forEach((identifier, tDashTextureSlot) -> {
				if (tDashTextureSlot.contents != null) {
					this.registerSprite(tDashTextureSlot.contents);
				}
			});
		} else {
			DashLoader.LOG.error("Tried to fallback stitcher twice.");
		}
	}

	@Override
	public void stitch() {
		if (data != null && remainingSlots != 0) {
			DashLoader.LOG.warn("Remaining slots did not match the cached amount, Falling back.");
			data.slots.forEach((identifier, tDashTextureSlot) -> {
				if (tDashTextureSlot.contents == null) {
					DashLoader.LOG.error("Sprite {} was not requested", identifier);
				}
			});
			doFallback();
		}

		if (data == null) {
			super.stitch();
		}
	}

	@Override
	public void gatherSprites(SpriteLoader<T> consumer) {
		if (data == null) {
			super.gatherSprites(consumer);
		} else {
			data.slots.forEach((identifier, dashTextureSlot) -> consumer.load(dashTextureSlot.contents, dashTextureSlot.x, dashTextureSlot.y, this.padding));
		}
	}

	public static class Data<T extends Stitcher.Entry> {
		public final IntObjectList<DashTextureSlot<T>> slots;
		public final int width;
		public final int height;
		public final int maxWidth;
		public final int maxHeight;
		public final int mipLevel;
		public final int padding;

		public Data(IntObjectList<DashTextureSlot<T>> slots, int width, int height, int maxWidth, int maxHeight, int mipLevel, int padding) {
			this.slots = slots;
			this.width = width;
			this.height = height;
			this.maxWidth = maxWidth;
			this.maxHeight = maxHeight;
			this.mipLevel = mipLevel;
			this.padding = padding;
		}

		public Data(RegistryWriter factory, Stitcher<T> stitcher) {
			this.slots = new IntObjectList<>();
			stitcher.gatherSprites((info, x, y, padding) -> this.slots.put(factory.add(info.name()), new DashTextureSlot<>(x, y, info.width(), info.height())));
			this.width = stitcher.getWidth();
			this.height = stitcher.getHeight();
			StitcherAccessor access = (StitcherAccessor) stitcher;
			this.maxWidth = access.getMaxWidth();
			this.maxHeight = access.getMaxHeight();
			this.mipLevel = access.getMipLevel();
			this.padding = access.getPadding();
		}

		public ExportedData<T> export(RegistryReader reader) {
			var output = new HashMap<Identifier, DashTextureSlot<T>>();
			this.slots.forEach((key, value) -> output.put(reader.get(key), value));

			return new ExportedData<>(
					output,
					width,
					height,
					maxWidth,
					maxHeight,
					mipLevel,
					padding
			);
		}
	}

	public static class ExportedData<T extends Stitcher.Entry> {
		public final Map<Identifier, DashTextureSlot<T>> slots;
		public final int width;
		public final int height;
		public final int maxWidth;
		public final int maxHeight;
		public final int mipLevel;
		public final int padding;

		public ExportedData(Map<Identifier, DashTextureSlot<T>> slots, int width, int height, int maxWidth, int maxHeight, int mipLevel, int padding) {
			this.slots = slots;
			this.width = width;
			this.height = height;
			this.maxWidth = maxWidth;
			this.maxHeight = maxHeight;
			this.mipLevel = mipLevel;
			this.padding = padding;
		}

		/**
		 * Checks whether cached packing is reusable with current stitch parameters
		 * (mip/anisotropy come from video settings and change padding).
		 */
		public boolean matches(int maxWidth, int maxHeight, int mipLevel, int anisotropy) {
			return this.maxWidth == maxWidth
					&& this.maxHeight == maxHeight
					&& this.mipLevel == mipLevel
					&& this.padding == (1 << mipLevel << Mth.clamp(anisotropy - 1, 0, 4));
		}
	}
}
