package io.vacco.shax.json;

import java.io.StringWriter;
import java.lang.reflect.*;
import java.util.*;

import static io.vacco.shax.json.ShReflectionUtil.*;
import static io.vacco.shax.json.ShJsonValue.*;

public class ShObjectWriter extends ShObjectScanner {

  static class ShRefMeta {
    List<Field> fields = new ArrayList<>();
    List<Method> accessors = new ArrayList<>();
  }

  // TODO de-init thread local upon JVM shutdown
  private static final ThreadLocal<WeakHashMap<Class<?>, ShRefMeta>> classMeta = ThreadLocal.withInitial(WeakHashMap::new);

  private final boolean omitNullValues;
  private final boolean prettyPrint;

  public ShObjectWriter(boolean omitNullValues, boolean prettyPrint) {
    this.omitNullValues = omitNullValues;
    this.prettyPrint = prettyPrint;
  }

  private String getAccessorName(String fieldName) {
    String fName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    return String.format("get%s", fName);
  }

  private String fromAccessorName(String getterName) {
    String rawProp = getterName.replace("get", "");
    rawProp = Character.toLowerCase(rawProp.charAt(0)) + rawProp.substring(1);
    return rawProp;
  }

  private ShRefMeta metaOf(Class<?> clazz) {
    ShRefMeta meta = classMeta.get().get(clazz);
    Class<?> cN = clazz;
    if (meta == null) {
      meta = new ShRefMeta();
      while (cN != null) {
        for (Field field : cN.getDeclaredFields()) {
          if (Modifier.isPublic(field.getModifiers())) {
            field.setAccessible(true);
            meta.fields.add(field);
          } else {
            try {
              Method accessor = cN.getMethod(getAccessorName(field.getName()));
              if (Modifier.isPublic(accessor.getModifiers())) {
                meta.accessors.add(accessor);
              }
            } catch (NoSuchMethodException ignored) {}
          }
        }
        cN = cN.getSuperclass();
      }
      classMeta.get().put(clazz, meta);
    }
    return meta;
  }

  private Map<String, Object> rawValuesOf(Object o) {
    ShRefMeta meta = metaOf(o.getClass());
    Map<String, Object> values = new TreeMap<>();
    try {
      for (Field field : meta.fields) {
        values.put(field.getName(), field.get(o));
      }
      for (Method accessor : meta.accessors) {
        values.put(fromAccessorName(accessor.getName()), accessor.invoke(o));
      }
      return values;
    } catch (Exception e) { throw new IllegalStateException(e); }
  }

  private ShJsonValue fromCollection(Object o) {
    if (o instanceof List) {
      return new ShJsonArray().add(((List<?>) o).stream().map(this::fromObject));
    } else if (o instanceof Set) {
      return new ShJsonArray().add(((Set<?>) o).stream().map(this::fromObject));
    } else if (o instanceof Object[]) {
      return new ShJsonArray().add(Arrays.stream(((Object[]) o)).map(this::fromObject));
    } else if (o instanceof Map) {
      ShJsonObject mo = new ShJsonObject();
      ((Map<?, ?>) o).forEach((k, v) -> {
        ShJsonValue jv = fromObject(v);
        if (jv != null) {
          mo.set(k.toString(), jv);
        }
      });
      return mo;
    }
    throw new IllegalStateException(String.format("Not a collection type: [%s]", o));
  }

  private ShJsonValue fromObject(Object o) {
    if (mark(o) == null) {
      return null;
    }
    if (isBaseType(o)) {
      return isCollection(o) ? fromCollection(o) : fromValue(o);
    }
    ShJsonObject root = new ShJsonObject();
    Map<String, ?> rawVals = rawValuesOf(o);
    for (String k : rawVals.keySet()) {
      Object v = rawVals.get(k);
      ShJsonValue jv = fromObject(v);
      if (jv == null) {
        if (!omitNullValues) {
          root.set(k, ShJsonLiteral.NULL);
        }
      } else {
        root.set(k, jv);
      }
    }
    return root;
  }

  public String apply(Object o) {
    StringWriter sw = new StringWriter();
    ShWritingBuffer wb = new ShWritingBuffer(sw);
    ShJsonWriter w = prettyPrint ? new ShPrettyPrintWriter(wb, new char[] {' ', ' '}) : new ShJsonWriter(wb);
    try {
      ShJsonValue jv = fromObject(o);
      if (jv != null) {
        jv.write(w);
        wb.flush();
        wb.close();
      }
      return sw.toString();
    } catch (Exception x) {
      throw new IllegalStateException(x);
    }
  }
}
