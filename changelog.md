# Fixes

- Sprite metadata is preserved through the cache, so the GUI nine-slice definition of item tooltips is no longer lost and tooltips render with their proper frame again
- `Piece` fragment budget: nested fragments were sized against the total remaining size instead of the remainder, which let them exceed their allowance by up to 41%
- `DashImage.export` copied sprite pixels out of the decompression buffer instead of pointing into memory that is released immediately after
- Mipmap generation converts between the internal and the texel color format instead of copying raw integers
- `AffineTransformation` equality and hash code now match vanilla again, they previously compared by identity after mixin merging
- Model cache: resolved sprites are bound to the atlas they were resolved against, so switching resource packs no longer leaves stale sprites behind that render black
- Model cache: identical block states share one model group again instead of every state getting its own group
- Compression scratch buffer is reused across writes and sized from the actual Zstd bound

# Features

- Glyph map of a font is computed once and reused for all cached glyphs
- Numeric config options can be typed directly in the config screen
- Cached language data via `ClientLanguageMixin`

# Internal

- Model cache (`CACHE_MODEL_LOADER`) is marked as work in progress and is disabled by default, enabling it manually still works
- Minimum fragment size per thread raised from 4 MB to 20 MB
- Removed the redundant resource reload hook from `MinecraftClient`