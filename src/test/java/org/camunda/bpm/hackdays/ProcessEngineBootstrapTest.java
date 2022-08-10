package org.camunda.bpm.hackdays;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.hackdays.serialization.MappedStatementReader;
import org.camunda.bpm.hackdays.serialization.MappedStatementWriter;
import org.camunda.bpm.hackdays.serialization.ReflectionBasedObjectMapper;
import org.camunda.bpm.hackdays.serialization.ReflectionUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessEngineBootstrapTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessEngineBootstrapTest.class);

  @Test
  public void testEngineStartup() {
    ProcessEngine processEngine = ProcessEngineConfiguration
      .createStandaloneInMemProcessEngineConfiguration()
      .buildProcessEngine();

    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    Configuration mybatisConfiguration = engineConfiguration.getSqlSessionFactory().getConfiguration();

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    LOGGER.info("Serializing mybatis configuration");
    writeMybatisConfigToStream(mybatisConfiguration, outStream);
    LOGGER.info("Done");

    ByteArrayInputStream inputStream = new ByteArrayInputStream(outStream.toByteArray());

    LOGGER.info("Deserializing mybatis configuration");
    MappedStatement mappedStatement = readMappedStatementFromStream(inputStream);
//    Configuration deserializedMybatisConfiguration = readMybatisConfigFromStream(inputStream);
    LOGGER.info("Done");

    assertThat(mappedStatement).isNotNull();
  }

  @Test
  public void testSerializationWithKryo() {
    ProcessEngine processEngine = ProcessEngineConfiguration
        .createStandaloneInMemProcessEngineConfiguration()
        .buildProcessEngine();

    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    Configuration mybatisConfiguration = engineConfiguration.getSqlSessionFactory().getConfiguration();
    MappedStatement mappedStatement = mybatisConfiguration.getMappedStatements().iterator().next();
    ReflectionUtil.setField(mappedStatement, "configuration", null);

    ReflectionBasedObjectMapper mapper = new ReflectionBasedObjectMapper();

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();

    mapper.write(mappedStatement, outStream);

    ByteArrayInputStream inputStream = new ByteArrayInputStream(outStream.toByteArray());
    MappedStatement deserializedStatement = mapper.read(inputStream, MappedStatement.class);

    assertThat(deserializedStatement).isNotNull();
  }

  public void writeMybatisConfigToStream(Configuration mybatisConfig, OutputStream stream) {
    try {
      ObjectOutputStream outStream = new ObjectOutputStream(stream);
      MappedStatement mappedStatement = mybatisConfig.getMappedStatements().iterator().next();

      MappedStatementWriter writer = new MappedStatementWriter();
      writer.write(mappedStatement, outStream, null);

    } catch (Exception e) {
      throw new RuntimeException("Could not write object to out stream", e);
    }
  }

  public Configuration readMybatisConfigFromStream(InputStream stream) {
    try {
      ObjectInputStream inStream = new ObjectInputStream(stream);


      return (Configuration) inStream.readObject();
    } catch (Exception e) {
      throw new RuntimeException("Could not read object from input stream", e);
    }
  }

  public MappedStatement readMappedStatementFromStream(InputStream stream) {
    try {
      ObjectInputStream inStream = new ObjectInputStream(stream);

      Configuration configuration = new Configuration();

      MappedStatementReader reader = new MappedStatementReader(configuration);

      return reader.readObject(inStream, null);
    } catch (Exception e) {
      throw new RuntimeException("Could not read object from input stream", e);
    }
  }
}
