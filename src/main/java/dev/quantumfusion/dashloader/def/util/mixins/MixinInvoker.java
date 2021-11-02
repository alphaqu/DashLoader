package dev.quantumfusion.dashloader.def.util.mixins;

import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MixinInvoker {

	public static void invokeMethod(Class<?> clazz, String name, Object self, Object... args) {
		for (Method declaredMethod : clazz.getDeclaredMethods()) {
			if (declaredMethod.isAnnotationPresent(MixinMerged.class) &&  declaredMethod.getName().endsWith(name)) {
				declaredMethod.setAccessible(true);
				try {
					System.out.println(name);
					MethodHandles.lookup().unreflect(declaredMethod).bindTo(self).invokeWithArguments(args);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static MethodHandle[] getMixinMethods(Class<?> clazz, Class<?>... mixinParameters) {
		List<Method> mixinMethods = new ArrayList<>();
		for (Method declaredMethod : clazz.getDeclaredMethods()) {
			if (declaredMethod.isAnnotationPresent(MixinMerged.class) && Arrays.equals(declaredMethod.getParameterTypes(), mixinParameters)) {
				final String name = declaredMethod.getName();
				if(name.endsWith("onTailInit")) continue;
				System.out.println("MIXIN_M: " + name);
				declaredMethod.setAccessible(true);
				mixinMethods.add(declaredMethod);
			}
		}

		MethodHandle[] out = new MethodHandle[mixinMethods.size()];
		for (int i = 0; i < mixinMethods.size(); i++) {
			try {
				out[i] = MethodHandles.lookup().unreflect(mixinMethods.get(i));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return out;
	}

	public static void invokeMixinMethods(MethodHandle[] methods, Object self, Object... parameters) {
		for (MethodHandle method : methods) {
			try {
				method.bindTo(self).invokeWithArguments(parameters);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
