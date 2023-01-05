package dev.notalpha.dashloader.misc;

import org.apache.commons.lang3.builder.MultilineRecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Objects;

public class ObjectDumper {
	public static String dump(Object object) {
		return ReflectionToStringBuilder.toString(object, new Style());
	}

	private static final class Style extends MultilineRecursiveToStringStyle {
		public Style() {
			setFieldNameValueSeparator(": ");
			setUseIdentityHashCode(false);
			setUseShortClassName(true);

		}

		public void appendDetail(StringBuffer buffer, String fieldName, Object value) {
			if (value != null) {
				if (Objects.equals(fieldName, "glRef")) {
					buffer.append("<id>");
					return;
				}

				if (value instanceof IntBuffer) {
					buffer.append("IntBuffer");
					return;
				} else if (value instanceof FloatBuffer) {
					buffer.append("FloatBuffer");
					return;
				}
			}

			try {
				StringBuffer builder = new StringBuffer();
				super.appendDetail(builder, fieldName, value);
				String s = builder.toString();
				String result = s.split("@")[0];
				buffer.append(result);
			} catch (Exception e) {
				buffer.append("unknown");
				try {
					Field spaces = MultilineRecursiveToStringStyle.class.getDeclaredField("spaces");
					spaces.setAccessible(true);
					spaces.setInt(this, spaces.getInt(this) - 2);
				} catch (IllegalAccessException | NoSuchFieldException ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		@Override
		protected void appendDetail(StringBuffer buffer, String fieldName, Map<?, ?> map) {
			buffer.append(this.getArrayStart());

			map.forEach((o, o2) -> {
				buffer.append(getArraySeparator());
				this.appendDetail(buffer, String.valueOf(o), o2);
			});
			buffer.append(this.getArrayEnd());
		}

		@Override
		protected void appendIdentityHashCode(StringBuffer buffer, Object object) {


		}
	}

	public static class Hi {
		public Hi hello;
		private final int value;

		public Hi(Hi hello, int value) {
			this.hello = hello;
			this.value = value;
		}
	}
}
