# Fixes

- Sprite metadata is preserved through the cache, so the GUI nine-slice definition of item tooltips is no longer lost and tooltips render with their proper frame again
- `Piece` fragment budget: nested fragments were sized against the total remaining size instead of the remainder, which let them exceed their allowance by up to 41%
- `DashImage.export` copied sprite pixels out of the decompression buffer instead of pointing into memory that is released immediately after
- Mipmap generation converts between the internal and the texel color format instead of copying raw integers
- `AffineTransformation` equality and hash code now match vanilla again, they previously compared by identity after mixin merging
- Model cache: resolved sprites are bound to the atlas they were resolved against, so switching resource packs no longer leaves stale sprites behind that render black
- Model cache: identical block states share one model group again instead of every state getting its own group
- Compression scratch buffer is reused across writes and sized from the actual Zstd bound

- Sprite content and stitcher capture are thread safe, parallel sprite loading could corrupt the collected data
- Fragment and compression buffers are freed after use instead of leaking native memory with the worker threads
- Compressed cache files are unmapped after reading, deleting a cache no longer fails on Windows
- The vanilla load time in the stats no longer includes the time needed to save the cache
- Cache hash includes size and modification time of zip resource packs
- A failing cache load of any kind (also errors) falls back to a new cache instead of crashing
- A save keeps writing to its own cache directory when a reload changes the hash meanwhile
- Duplicate DashObjects are dropped, the model module no longer returns a null mapping
- Removed `AffineTransformationMixin` and its option, vanilla already compares by matrix

# Features

- Glyph map of a font is computed once and reused for all cached glyphs
- Numeric config options can be typed directly in the config screen
- Cached language data via `ClientLanguageMixin`

# Internal

- Model cache (`CACHE_MODEL_LOADER`) is marked as work in progress and is disabled by default, enabling it manually still works
- Minimum fragment size per thread raised from 4 MB to 20 MB
- Removed the redundant resource reload hook from `MinecraftClient`