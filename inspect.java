import java.lang.reflect.*;
public class Inspect {
  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("usage: Inspect <class>");
      return;
    }
    Class<?> c = Class.forName(args[0]);
    System.out.println("CLASS " + c.getName());
    System.out.println("FIELDS:");
    for (Field f : c.getDeclaredFields()) {
      System.out.println("  " + Modifier.toString(f.getModifiers()) + " " + f.getType().getTypeName() + " " + f.getName());
    }
    System.out.println("CTORS:");
    for (Constructor<?> k : c.getDeclaredConstructors()) {
      System.out.println("  " + Modifier.toString(k.getModifiers()) + " " + c.getSimpleName() + "(" + params(k.getParameterTypes()) + ")");
    }
    System.out.println("METHODS:");
    for (Method m : c.getDeclaredMethods()) {
      System.out.println("  " + Modifier.toString(m.getModifiers()) + " " + m.getReturnType().getTypeName() + " " + m.getName() + "(" + params(m.getParameterTypes()) + ")");
    }
  }
  private static String params(Class<?>[] ps) {
    StringBuilder sb = new StringBuilder();
    for (int i=0;i<ps.length;i++) {
      if (i>0) sb.append(", ");
      sb.append(ps[i].getTypeName());
    }
    return sb.toString();
  }
}
