package org.camunda.bpm.hackdays.serialization.kryo;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.UnknownTypeHandler;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class UnknownTypeHandlerSerializer extends Serializer<UnknownTypeHandler> {

  private Configuration configuration;

  public UnknownTypeHandlerSerializer(Configuration configuration) {
    super();
    this.configuration = configuration;
  }

  @Override
  public void write(Kryo kryo, Output output, UnknownTypeHandler object) {
    // do nothing

  }

  @Override
  public UnknownTypeHandler read(Kryo kryo, Input input, Class<? extends UnknownTypeHandler> type) {
    return new UnknownTypeHandler(configuration);
  }

}
