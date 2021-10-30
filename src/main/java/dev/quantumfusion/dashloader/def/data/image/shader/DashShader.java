package dev.quantumfusion.dashloader.def.data.image.shader;

import com.google.common.collect.UnmodifiableIterator;
import com.mojang.blaze3d.platform.GlStateManager;
import dev.quantumfusion.dashloader.core.util.DashUtil;
import dev.quantumfusion.dashloader.def.mixin.accessor.ShaderAccessor;
import dev.quantumfusion.dashloader.def.util.UnsafeHelper;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import dev.quantumfusion.hyphen.scan.annotations.DataNullable;
import dev.quantumfusion.hyphen.scan.annotations.DataSubclasses;
import net.minecraft.client.gl.GlProgramManager;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;

import java.util.*;

@Data
public class DashShader {
	public final Map<String, Sampler> samplers;
	public final String name;
	public final DashGlBlendState blendState;
	public final List<String> attributeNames;
	public final DashProgram vertexShader;
	public final DashProgram fragmentShader;
	public final VertexFormatsHelper.Value format;
	@DataNullable
	public final DashGlUniform modelViewMat;
	@DataNullable
	public final DashGlUniform projectionMat;
	@DataNullable
	public final DashGlUniform textureMat;
	@DataNullable
	public final DashGlUniform screenSize;
	@DataNullable
	public final DashGlUniform colorModulator;
	@DataNullable
	public final DashGlUniform light0Direction;
	@DataNullable
	public final DashGlUniform light1Direction;
	@DataNullable
	public final DashGlUniform fogStart;
	@DataNullable
	public final DashGlUniform fogEnd;
	@DataNullable
	public final DashGlUniform fogColor;
	@DataNullable
	public final DashGlUniform lineWidth;
	@DataNullable
	public final DashGlUniform gameTime;
	@DataNullable
	public final DashGlUniform chunkOffset;
	public final List<String> samplerNames;


	transient Shader toApply;


	public DashShader(Map<String, Sampler> samplers,
			String name,
			DashGlBlendState blendState,
			List<String> attributeNames,
			DashProgram vertexShader,
			DashProgram fragmentShader,
			VertexFormatsHelper.Value format,
			DashGlUniform modelViewMat,
			DashGlUniform projectionMat,
			DashGlUniform textureMat,
			DashGlUniform screenSize,
			DashGlUniform colorModulator,
			DashGlUniform light0Direction,
			DashGlUniform light1Direction,
			DashGlUniform fogStart,
			DashGlUniform fogEnd,
			DashGlUniform fogColor,
			DashGlUniform lineWidth,
			DashGlUniform gameTime,
			DashGlUniform chunkOffset,
			List<String> samplerNames) {
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

		this.samplers = new LinkedHashMap<>();
		shaderAccess.getSamplers().forEach((s, o) -> this.samplers.put(s, new Sampler(o)));
		this.name = shader.getName();

		this.blendState = new DashGlBlendState(shaderAccess.getBlendState());
		this.attributeNames = shaderAccess.getAttributeNames();
		this.vertexShader = new DashProgram(shader.getVertexShader());
		this.fragmentShader = new DashProgram(shader.getFragmentShader());
		this.format = VertexFormatsHelper.getEnum(shader.getFormat());
		this.modelViewMat = DashUtil.nullable(shader.modelViewMat, DashGlUniform::new);
		this.projectionMat = DashUtil.nullable(shader.projectionMat, DashGlUniform::new);
		this.textureMat = DashUtil.nullable(shader.textureMat, DashGlUniform::new);
		this.screenSize = DashUtil.nullable(shader.screenSize, DashGlUniform::new);
		this.colorModulator = DashUtil.nullable(shader.colorModulator, DashGlUniform::new);
		this.light0Direction = DashUtil.nullable(shader.light0Direction, DashGlUniform::new);
		this.light1Direction = DashUtil.nullable(shader.light1Direction, DashGlUniform::new);
		this.fogStart = DashUtil.nullable(shader.fogStart, DashGlUniform::new);
		this.fogEnd = DashUtil.nullable(shader.fogEnd, DashGlUniform::new);
		this.fogColor = DashUtil.nullable(shader.fogColor, DashGlUniform::new);
		this.lineWidth = DashUtil.nullable(shader.lineWidth, DashGlUniform::new);
		this.gameTime = DashUtil.nullable(shader.gameTime, DashGlUniform::new);
		this.chunkOffset = DashUtil.nullable(shader.chunkOffset, DashGlUniform::new);
		this.samplerNames = shaderAccess.getSamplerNames();
	}


	public Shader export() {
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
		var samplersOut = new HashMap<String, Object>();
		this.samplers.forEach((s, o) -> samplersOut.put(s, o.sampler));
		shaderAccess.setSamplers(samplersOut);

		// JsonHelper.getArray(jsonObject, "attributes", (JsonArray)null);
		shaderAccess.setAttributeNames(this.attributeNames);


		// JsonHelper.getArray(jsonObject, "uniforms", (JsonArray)null);
		final GlUniform modelViewMatOut = DashUtil.nullable(modelViewMat, (a) -> a.export(toApply, uniforms));
		final GlUniform projectionMatOut = DashUtil.nullable(projectionMat, (a) -> a.export(toApply, uniforms));
		final GlUniform textureMatOut = DashUtil.nullable(textureMat, (a) -> a.export(toApply, uniforms));
		final GlUniform screenSizeOut = DashUtil.nullable(screenSize, (a) -> a.export(toApply, uniforms));
		final GlUniform colorModulatorOut = DashUtil.nullable(colorModulator, (a) -> a.export(toApply, uniforms));
		final GlUniform light0DirectionOut = DashUtil.nullable(light0Direction, (a) -> a.export(toApply, uniforms));
		final GlUniform light1DirectionOut = DashUtil.nullable(light1Direction, (a) -> a.export(toApply, uniforms));
		final GlUniform fogStartOut = DashUtil.nullable(fogStart, (a) -> a.export(toApply, uniforms));
		final GlUniform fogEndOut = DashUtil.nullable(fogEnd, (a) -> a.export(toApply, uniforms));
		final GlUniform fogColorOut = DashUtil.nullable(fogColor, (a) -> a.export(toApply, uniforms));
		final GlUniform lineWidthOut = DashUtil.nullable(lineWidth, (a) -> a.export(toApply, uniforms));
		final GlUniform gameTimeOut = DashUtil.nullable(gameTime, (a) -> a.export(toApply, uniforms));
		final GlUniform chunkOffsetOut = DashUtil.nullable(chunkOffset, (a) -> a.export(toApply, uniforms));


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
		shaderAccess.setBlendState(this.blendState.export());
		shaderAccess.setVertexShader(this.vertexShader.exportProgram());
		shaderAccess.setFragmentShader(this.fragmentShader.exportProgram());
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

	public static class Sampler {
		@Data
		@DataNullable
		@DataSubclasses({Integer.class, String.class})
		public final Object sampler;

		public Sampler(@DataSubclasses({Integer.class, String.class}) Object sampler) {
			this.sampler = sampler;
		}
	}

}
