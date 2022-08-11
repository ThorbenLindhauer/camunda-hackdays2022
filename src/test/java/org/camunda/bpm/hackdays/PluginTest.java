package org.camunda.bpm.hackdays;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(org.junit.runners.MethodSorters.NAME_ASCENDING)
public class PluginTest {

  private static final Logger LOG = LoggerFactory.getLogger(PluginTest.class);

  @Test
    public void test1WriteMapping() {
      try {
        FileSystem fileSystem = FileSystems.getDefault();
        Path mappingFilePath = fileSystem.getPath(MyBatisMappingPlugin.DEFAULT_MAPPING_DIR, MyBatisMappingPlugin.MYBATIS_MAPPING_FILENAME);
        Files.delete(mappingFilePath);
      } catch (IOException e) {
        LOG.info("File doesn't exist: {}", e.getLocalizedMessage());
      }
      ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();

  //    configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
      configuration.setProcessEnginePlugins(Arrays.asList(new MyBatisMappingPlugin()));
      ProcessEngine processEngine = configuration.buildProcessEngine();
      processEngine.close();
    }

  @Test
  public void test2StartEngineWithPlugin() {
    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();

    configuration.setProcessEnginePlugins(Arrays.asList(new MyBatisMappingPlugin()));
    ProcessEngine processEngine = configuration.buildProcessEngine();
    processEngine.close();
  }

}
