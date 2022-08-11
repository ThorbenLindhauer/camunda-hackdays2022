package org.camunda.bpm.hackdays.serialization.kryo;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.session.Configuration;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class LogSerializer extends Serializer<Log> {

  @Override
  public void write(Kryo kryo, Output output, Log object) {
  }

  @Override
  public Log read(Kryo kryo, Input input, Class<? extends Log> type) {
    return null;
  }

}
