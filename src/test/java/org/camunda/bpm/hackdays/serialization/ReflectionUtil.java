package org.camunda.bpm.hackdays.serialization;

import java.lang.reflect.Field;

public class ReflectionUtil {

  public static Object readField(Object obj, String fieldName) {
    Field field;
    try {
      field = obj.getClass().getField(fieldName);
      return field.get(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void setField(Object obj, String fieldName, Object value) {
    Field field;
    try {
      field = obj.getClass().getDeclaredField(fieldName);
      field.setAccessible(true);
      field.set(obj, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
