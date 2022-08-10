package org.camunda.bpm.hackdays;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.hackdays.serialization.KryoObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;

public class ProcessEngineBootstrapTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProcessEngineBootstrapTest.class);

  @Test
  public void testSerializationWithKryo() {
    Log.DEBUG();

    ProcessEngine processEngine = ProcessEngineConfiguration
        .createStandaloneInMemProcessEngineConfiguration()
        .buildProcessEngine();

    ProcessEngineConfigurationImpl engineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    Configuration mybatisConfiguration = engineConfiguration.getSqlSessionFactory().getConfiguration();
    Collection<MappedStatement> mappedStatements = new ArrayList<>(mybatisConfiguration.getMappedStatements());

    KryoObjectMapper mapper = new KryoObjectMapper(mybatisConfiguration);
//    mybatisConfiguration.getMappedStatements().clear();
//    ReflectionUtil.setField(mybatisConfiguration, "mappedStatements", new Configuration.Str<>());

    Configuration newConfiguration = new Configuration();
    newConfiguration.setEnvironment(mybatisConfiguration.getEnvironment());

    for (MappedStatement mappedStatement : mappedStatements) {
      if (newConfiguration.hasStatement(mappedStatement.getId())) {
        // mybatis stores a statement under two keys (short and long), so we iterate every
        // statement twice
        continue;
      }

      LOGGER.info("Processing statement {}", mappedStatement.getId());
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();

      mapper.write(mappedStatement, outStream);

      ByteArrayInputStream inputStream = new ByteArrayInputStream(outStream.toByteArray());
      MappedStatement deserializedStatement = mapper.read(inputStream, MappedStatement.class);

      newConfiguration.addMappedStatement(deserializedStatement);
    }

//    engineConfiguration.setSqlSessionFactory(sqlSessionFactory);
//    engineConfiguration.getDbSqlSessionFactory().setSqlSessionFactory(sqlSessionFactory);

    ProcessEngineConfigurationImpl newEngineConfiguration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
        .createStandaloneInMemProcessEngineConfiguration()
        .setProcessEngineName("newProcessEngine")
        .setDatabaseSchemaUpdate("false");

    DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(newConfiguration);
    newEngineConfiguration.setSqlSessionFactory(sqlSessionFactory);
    ProcessEngine newProcessEngine = newEngineConfiguration
        .buildProcessEngine();

    Map<String, String> properties = newProcessEngine.getManagementService().getProperties();
    assertThat(properties).isNotEmpty();
  }

}
