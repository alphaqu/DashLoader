package dev.notalpha.dashloader.io.def;

import dev.quantumfusion.hyphen.SerializerHandler;
import dev.quantumfusion.hyphen.codegen.MethodHandler;
import dev.quantumfusion.hyphen.codegen.def.BufferDef;
import dev.quantumfusion.hyphen.scan.type.Clazz;
import org.lwjgl.system.MemoryUtil;
import org.objectweb.asm.Opcodes;

import java.nio.ByteBuffer;

public class UnsafeByteBufferDef extends BufferDef {
	private final boolean unsafe;
	public UnsafeByteBufferDef(Clazz clazz, SerializerHandler<?, ?> serializerHandler) {
		super(clazz, serializerHandler);
		this.unsafe = clazz.containsAnnotation(DataUnsafeByteBuffer.class);
	}

	@Override
	protected void allocateBuffer(MethodHandler mh) {
		if (unsafe) {
			if (buffer != ByteBuffer.class) {
				throw new UnsupportedOperationException();
			}
			mh.callInst(Opcodes.INVOKESTATIC, MemoryUtil.class, "memAlloc", ByteBuffer.class, int.class);
		} else {
			super.allocateBuffer(mh);
		}
	}
}
