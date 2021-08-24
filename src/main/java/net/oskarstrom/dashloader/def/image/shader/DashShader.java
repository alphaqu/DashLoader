package net.oskarstrom.dashloader.def.image.shader;

import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.platform.GlStateManager;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeNullable;
import io.activej.serializer.annotations.SerializeSubclasses;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.oskarstrom.dashloader.core.util.DashHelper;
import net.oskarstrom.dashloader.def.mixin.accessor.ShaderAccessor;
import net.oskarstrom.dashloader.def.util.UnsafeHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashShader {
	@Serialize(order = 0)
	@SerializeNullable(path = {1})
	@SerializeSubclasses(path = {1}, value = {Integer.class, String.class})
	public final Map<String, Object> samplers;

	@Serialize(order = 1)
	public final String name;

	@Serialize(order = 2)
	public final DashGlBlendState blendState;

	@Serialize(order = 3)
	public final List<String> attributeNames;

	@Serialize(order = 4)
	public final DashProgram vertexShader;

	@Serialize(order = 5)
	public final DashProgram fragmentShader;

	@Serialize(order = 6)
	public final VertexFormatsHelper.Value format;

	@Serialize(order = 7)
	@SerializeNullable
	public final DashGlUniform modelViewMat;

	@Serialize(order = 8)
	@SerializeNullable
	public final DashGlUniform projectionMat;

	@Serialize(order = 9)
	@SerializeNullable
	public final DashGlUniform textureMat;

	@Serialize(order = 10)
	@SerializeNullable
	public final DashGlUniform screenSize;

	@Serialize(order = 11)
	@SerializeNullable
	public final DashGlUniform colorModulator;

	@Serialize(order = 12)
	@SerializeNullable
	public final DashGlUniform light0Direction;

	@Serialize(order = 13)
	@SerializeNullable
	public final DashGlUniform light1Direction;

	@Serialize(order = 14)
	@SerializeNullable
	public final DashGlUniform fogStart;

	@Serialize(order = 15)
	@SerializeNullable
	public final DashGlUniform fogEnd;

	@Serialize(order = 16)
	@SerializeNullable
	public final DashGlUniform fogColor;


	@Serialize(order = 17)
	@SerializeNullable
	public final DashGlUniform lineWidth;

	@Serialize(order = 18)
	@SerializeNullable
	public final DashGlUniform gameTime;

	@Serialize(order = 19)
	@SerializeNullable
	public final DashGlUniform chunkOffset;


	@Serialize(order = 20)
	public final List<String> samplerNames;


	Shader toApply;


	public DashShader(@Deserialize("samplers") Map<String, Object> samplers,
					  @Deserialize("name") String name,
					  @Deserialize("blendState") DashGlBlendState blendState,
					  @Deserialize("attributeNames") List<String> attributeNames,
					  @Deserialize("vertexShader") DashProgram vertexShader,
					  @Deserialize("fragmentShader") DashProgram fragmentShader,
					  @Deserialize("format") VertexFormatsHelper.Value format,
					  @Deserialize("modelViewMat") DashGlUniform modelViewMat,
					  @Deserialize("projectionMat") DashGlUniform projectionMat,
					  @Deserialize("textureMat") DashGlUniform textureMat,
					  @Deserialize("screenSize") DashGlUniform screenSize,
					  @Deserialize("colorModulator") DashGlUniform colorModulator,
					  @Deserialize("light0Direction") DashGlUniform light0Direction,
					  @Deserialize("light1Direction") DashGlUniform light1Direction,
					  @Deserialize("fogStart") DashGlUniform fogStart,
					  @Deserialize("fogEnd") DashGlUniform fogEnd,
					  @Deserialize("fogColor") DashGlUniform fogColor,
					  @Deserialize("lineWidth") DashGlUniform lineWidth,
					  @Deserialize("gameTime") DashGlUniform gameTime,
					  @Deserialize("chunkOffset") DashGlUniform chunkOffset,
					  @Deserialize("samplerNames") List<String> samplerNames) {
		this.samplers = samplers;
		this.name = name;
		this.blendState = blendState;
		this.attributeNames = attributeNames;
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		this.format = format;
		this.modelViewMat = modelViewMat;
		this.projectionMat = projectionMat;
		this.textureMat = textureMat;
		this.screenSize = screenSize;
		this.colorModulator = colorModulator;
		this.light0Direction = light0Direction;
		this.light1Direction = light1Direction;
		this.fogStart = fogStart;
		this.fogEnd = fogEnd;
		this.fogColor = fogColor;
		this.lineWidth = lineWidth;
		this.gameTime = gameTime;
		this.chunkOffset = chunkOffset;
		this.samplerNames = samplerNames;
	}

	public DashShader(Shader shader) {
		ShaderAccessor shaderAccess = (ShaderAccessor) shader;
		this.samplers = shaderAccess.getSamplers();
		this.name = shader.getName();
		this.blendState = new DashGlBlendState(shaderAccess.getBlendState());
		this.attributeNames = shaderAccess.getAttributeNames();
		this.vertexShader = new DashProgram(shader.getVertexShader());
		this.fragmentShader = new DashProgram(shader.getFragmentShader());
		this.format = VertexFormatsHelper.getEnum(shader.getFormat());
		this.modelViewMat = DashHelper.nullable(shader.modelViewMat, DashGlUniform::new);
		this.projectionMat = DashHelper.nullable(shader.projectionMat, DashGlUniform::new);
		this.textureMat = DashHelper.nullable(shader.textureMat, DashGlUniform::new);
		this.screenSize = DashHelper.nullable(shader.screenSize, DashGlUniform::new);
		this.colorModulator = DashHelper.nullable(shader.colorModulator, DashGlUniform::new);
		this.light0Direction = DashHelper.nullable(shader.light0Direction, DashGlUniform::new);
		this.light1Direction = DashHelper.nullable(shader.light1Direction, DashGlUniform::new);
		this.fogStart = DashHelper.nullable(shader.fogStart, DashGlUniform::new);
		this.fogEnd = DashHelper.nullable(shader.fogEnd, DashGlUniform::new);
		this.fogColor = DashHelper.nullable(shader.fogColor, DashGlUniform::new);
		this.lineWidth = DashHelper.nullable(shader.lineWidth, DashGlUniform::new);
		this.gameTime = DashHelper.nullable(shader.gameTime, DashGlUniform::new);
		this.chunkOffset = DashHelper.nullable(shader.chunkOffset, DashGlUniform::new);
		this.samplerNames = shaderAccess.getSamplerNames();
	}


	public Shader toUndash() {
		toApply = UnsafeHelper.allocateInstance(Shader.class);
		ShaderAccessor shaderAccess = (ShaderAccessor) toApply;
		//object init
		shaderAccess.setLoadedSamplerIds(new ArrayList<>());
		shaderAccess.setLoadedUniformIds(new ArrayList<>());
		shaderAccess.setLoadedUniforms(new HashMap<>());
		final ArrayList<GlUniform> uniforms = new ArrayList<>();
		shaderAccess.setUniforms(uniforms);
		List<Integer> loadedAttributeIds = new ArrayList<>();
		shaderAccess.setLoadedAttributeIds(loadedAttributeIds);

		shaderAccess.setSamplerNames(samplerNames);

		//<init> top
		shaderAccess.setName(this.name);
		final VertexFormat format = this.format.getFormat();
		shaderAccess.setFormat(format);


		//JsonHelper.getArray(jsonObject, "samplers", (JsonArray)null)
		shaderAccess.setSamplers(this.samplers);


		// JsonHelper.getArray(jsonObject, "attributes", (JsonArray)null);
		shaderAccess.setAttributeNames(this.attributeNames);


		// JsonHelper.getArray(jsonObject, "uniforms", (JsonArray)null);
		final GlUniform modelViewMatOut = DashHelper.nullable(modelViewMat, (modelViewMat) -> modelViewMat.toUndash(toApply, uniforms));
		final GlUniform projectionMatOut = DashHelper.nullable(projectionMat, (projectionMat) -> projectionMat.toUndash(toApply, uniforms));
		final GlUniform textureMatOut = DashHelper.nullable(textureMat, (textureMat) -> textureMat.toUndash(toApply, uniforms));
		final GlUniform screenSizeOut = DashHelper.nullable(screenSize, (screenSize) -> screenSize.toUndash(toApply, uniforms));
		final GlUniform colorModulatorOut = DashHelper.nullable(colorModulator, (colorModulator) -> colorModulator.toUndash(toApply, uniforms));
		final GlUniform light0DirectionOut = DashHelper.nullable(light0Direction, (light0Direction) -> light0Direction.toUndash(toApply, uniforms));
		final GlUniform light1DirectionOut = DashHelper.nullable(light1Direction, (light1Direction) -> light1Direction.toUndash(toApply, uniforms));
		final GlUniform fogStartOut = DashHelper.nullable(fogStart, (fogStart) -> fogStart.toUndash(toApply, uniforms));
		final GlUniform fogEndOut = DashHelper.nullable(fogEnd, (fogEnd) -> fogEnd.toUndash(toApply, uniforms));
		final GlUniform fogColorOut = DashHelper.nullable(fogColor, (fogColor) -> fogColor.toUndash(toApply, uniforms));
		final GlUniform lineWidthOut = DashHelper.nullable(lineWidth, (lineWidth) -> lineWidth.toUndash(toApply, uniforms));
		final GlUniform gameTimeOut = DashHelper.nullable(gameTime, (gameTime) -> gameTime.toUndash(toApply, uniforms));
		final GlUniform chunkOffsetOut = DashHelper.nullable(chunkOffset, (chunkOffset) -> chunkOffset.toUndash(toApply, uniforms));


		toApply.markUniformsDirty();
		shaderAccess.setModelViewMat(modelViewMatOut);
		shaderAccess.setProjectionMat(projectionMatOut);
		shaderAccess.setTextureMat(textureMatOut);
		shaderAccess.setScreenSize(screenSizeOut);
		shaderAccess.setColorModulator(colorModulatorOut);
		shaderAccess.setLight0Direction(light0DirectionOut);
		shaderAccess.setLight1Direction(light1DirectionOut);
		shaderAccess.setFogStart(fogStartOut);
		shaderAccess.setFogEnd(fogEndOut);
		shaderAccess.setFogColor(fogColorOut);
		shaderAccess.setLineWidth(lineWidthOut);
		shaderAccess.setGameTime(gameTimeOut);
		shaderAccess.setChunkOffset(chunkOffsetOut);
		return toApply;
	}


	public void apply() {
		ShaderAccessor shaderAccess = (ShaderAccessor) toApply;
		shaderAccess.setBlendState(this.blendState.toUndash());
		shaderAccess.setVertexShader(this.vertexShader.toUndashProgram());
		shaderAccess.setFragmentShader(this.fragmentShader.toUndashProgram());
		final List<Integer> loadedAttributeIds = shaderAccess.getLoadedAttributeIds();

		final int programId = GlStateManager.glCreateProgram();
		shaderAccess.setProgramId(programId);


		if (this.attributeNames != null) {
			int l = 0;
			for (UnmodifiableIterator<String> var35 = format.getFormat().getShaderAttributes().iterator(); var35.hasNext(); ++l) {
				String string3 = var35.next();
				GlUniform.bindAttribLocation(programId, l, string3);
				loadedAttributeIds.add(l);
			}
		}
		GlProgramManager.linkProgram(toApply);
		shaderAccess.loadref();
	}


}
