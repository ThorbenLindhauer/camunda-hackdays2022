package org.camunda.bpm.hackdays.serialization;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;

public interface ObjectReader<T> {

  T readObject(ObjectInputStream inputStream, Map<Class<?>, ObjectReader<?>> readerRegistry) throws Exception;
}
