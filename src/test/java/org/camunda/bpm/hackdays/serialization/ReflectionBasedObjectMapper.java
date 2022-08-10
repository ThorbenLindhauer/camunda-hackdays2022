package org.camunda.bpm.hackdays.serialization;

import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ReflectionBasedObjectMapper {

  private Kryo kryo;

  public ReflectionBasedObjectMapper() {
    kryo = new Kryo();
    kryo.setRegistrationRequired(false);
  }

  public void write(Object objectToWrite, OutputStream outStream) {

    kryo.writeObject(new Output(outStream), objectToWrite);
  }

  public <T> T read(InputStream inputStream, Class<T> clazz) {
    return kryo.readObject(new Input(inputStream), clazz);
  }
}
