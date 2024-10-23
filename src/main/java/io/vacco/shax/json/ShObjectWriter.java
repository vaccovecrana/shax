package io.vacco.shax.json;

import java.io.StringWriter;
import java.lang.reflect.*;
import java.util.*;

import static io.vacco.shax.json.ShReflect.*;
import static io.vacco.shax.json.ShJsonValue.*;

public class ShObjectWriter {

  static class ShRefMeta {
    List<Field> fields = new ArrayList<>();
    List<Method> accessors = new ArrayList<>();
  }

  // TODO de-init thread local upon JVM shutdown
  private static final ThreadLocal<WeakHashMap<Class<?>, ShRefMeta>> classMeta = ThreadLocal.withInitial(WeakHashMap::new);

  private final Map<Object, Object> refs = new IdentityHashMap<>();
  private final boolean omitNullValues;
  private final boolean prettyPrint;

  public ShObjectWriter(boolean omitNullValues, boolean prettyPrint) {
    this.omitNullValues = omitNullValues;
    this.prettyPrint = prettyPrint;
  }

  private Object mark(Object o) {
    if (o == null) return null;
    if (!ShReflect.isBaseType(o)) {
      if (!refs.containsKey(o)) {
        refs.put(o, o);
        return o;
      }
      return null;
    }
    return o;
  }

  private String getAccessorName(String fieldName) {
    var fName = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    return String.format("get%s", fName);
  }

  private String fromAccessorName(String getterName) {
    var rawProp = getterName.replace("get", "");
    rawProp = Character.toLowerCase(rawProp.charAt(0)) + rawProp.substring(1);
    return rawProp;
  }

  private ShRefMeta metaOf(Class<?> clazz) {
    var meta = classMeta.get().get(clazz);
    var cN = clazz;
    if (meta == null) {
      meta = new ShRefMeta();
      while (cN != null) {
        for (var field : cN.getDeclaredFields()) {
          int mods = field.getModifiers();
          if (Modifier.isPublic(mods) && !Modifier.isStatic(mods) && !Modifier.isTransient(mods)) {
            field.setAccessible(true);
            meta.fields.add(field);
          } else {
            try {
              var accessor = cN.getMethod(getAccessorName(field.getName()));
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
    var meta = metaOf(o.getClass());
    var values = new TreeMap<String, Object>();
    try {
      for (var field : meta.fields) {
        values.put(field.getName(), field.get(o));
      }
      for (var accessor : meta.accessors) {
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
    } else if (o instanceof Map) {
      var mo = new ShJsonObject();
      ((Map<?, ?>) o).forEach((k, v) -> {
        var jv = fromObject(v);
        if (jv != null) {
          mo.set(k.toString(), jv);
        }
      });
      return mo;
    }
    var oa = wrap(o);
    return new ShJsonArray().add(Arrays.stream(oa).map(this::fromObject));
  }

  private ShJsonValue fromObject(Object o) {
    if (mark(o) == null) {
      return null;
    }
    if (isBaseType(o)) {
      return isCollection(o) ? fromCollection(o) : fromValue(o);
    }
    var root = new ShJsonObject();
    var rawVals = rawValuesOf(o);
    for (var k : rawVals.keySet()) {
      var v = rawVals.get(k);
      var jv = fromObject(v);
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
    var sw = new StringWriter();
    var wb = new ShWritingBuffer(sw);
    var w = prettyPrint ? new ShPrettyPrintWriter(wb, new char[] {' ', ' '}) : new ShJsonWriter(wb);
    try {
      var jv = fromObject(o);
      if (jv != null) {
        jv.write(w);
        wb.flush();
        wb.close();
      }
      refs.clear();
      return sw.toString();
    } catch (Exception x) {
      return String.join("\n",
        "JSON serialization failed for object payload.",
        "Please report an upstream bug at https://github.com/vaccovecrana/shax/issues",
        String.format("Payload: [%s]", o),
        x.getMessage()
      );
    }
  }

}
