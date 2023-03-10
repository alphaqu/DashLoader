# Fixes
- End Portal shaders being broken because uniforms apparently never saved their array data.
- Angry "Could not enforce maximum cache" error on first dashloader run
- Resource pack order not being recognised by the hash
- Use mutable collections in shader fields.

# Performance improvements
- Make DashVertexFormatElement use builtin constants if possible

# Internal changes
- Add support for IntBuffer, FloatBuffer and Enum in ObjectDumper.
- Fixed ObjectDumper giving bad results on null map values.
- Cleanup DashShaderStage
- Cleanup some old code
- Remove useless comment / unused class