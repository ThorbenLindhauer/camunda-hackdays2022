package org.camunda.bpm.hackdays.serialization;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.session.Configuration;
import org.camunda.bpm.hackdays.serialization.kryo.ImmutableListSerializer;
import org.camunda.bpm.hackdays.serialization.kryo.UnmodifiableSetSerializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ReflectionBasedObjectMapper {

  private Kryo kryo;

  public ReflectionBasedObjectMapper() {
    kryo = new Kryo();
    kryo.setRegistrationRequired(false);
    kryo.register(Configuration.class, new ConfigurationSerializer());
    Class<? extends List> unmodifiableListClass = Collections.unmodifiableList(new ArrayList<String>()).getClass();
    kryo.register(
        unmodifiableListClass, new ImmutableListSerializer());
    Class<? extends Set> unmodifiableSetClass = Collections.unmodifiableSet(new HashSet<String>()).getClass();
    kryo.register(unmodifiableSetClass, new UnmodifiableSetSerializer());
  }

  public void write(Object objectToWrite, OutputStream outStream) {

    kryo.writeObject(new Output(outStream), objectToWrite);
  }

  public <T> T read(InputStream inputStream, Class<T> clazz) {
    return kryo.readObject(new Input(inputStream), clazz);
  }
}
