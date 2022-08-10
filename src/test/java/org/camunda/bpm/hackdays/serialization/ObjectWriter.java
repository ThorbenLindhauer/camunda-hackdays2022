package org.camunda.bpm.hackdays.serialization;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

public interface ObjectWriter<T> {

  void write(T objectToWrite,
      ObjectOutputStream outStream,
      Map<Class<?>, ObjectWriter<?>> writerRegistry) throws IOException;
}
