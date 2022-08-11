package org.camunda.bpm.hackdays.serialization.kryo;

import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class ConfigurationSerializer extends Serializer<Configuration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationSerializer.class);

  private Configuration configuration;

  public ConfigurationSerializer(Configuration configuration) {
    super();
    this.configuration = configuration;
  }

  @Override
  public void write(Kryo kryo, Output output, Configuration object) {
//    LOGGER.info("Serializing Configuration object. Doing nothing.");
  }

  @Override
  public Configuration read(Kryo kryo, Input input, Class<? extends Configuration> type) {
//    LOGGER.info("Deserializing Configuration object. Returning default configuration.");
    return configuration;
  }

}
