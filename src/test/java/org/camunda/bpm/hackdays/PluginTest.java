package org.camunda.bpm.hackdays;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

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
      Path mappingDirectory = MappingRegistry.getDefaultMappingDirectory(FileSystems.getDefault());
      deleteDirectory(mappingDirectory);

      ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();

  //    configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
      configuration.setProcessEnginePlugins(Arrays.asList(new MyBatisMappingPlugin()));
      ProcessEngine processEngine = configuration.buildProcessEngine();
      processEngine.close();
    }

  private void deleteDirectory(Path mappingDirectory) {
    try {
      try (Stream<Path> walk = Files.walk(mappingDirectory)) {
        walk.sorted(Comparator.reverseOrder())
            .filter(p -> !p.equals(mappingDirectory))
            .map(Path::toFile)
            .forEach(File::delete);
      }
    } catch (IOException e) {
      LOG.info("File doesn't exist: {}", e.getLocalizedMessage());
    }
  }

  @Test
  public void test2StartEngineWithPlugin() {
    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();

    configuration.setProcessEnginePlugins(Arrays.asList(new MyBatisMappingPlugin()));
    ProcessEngine processEngine = configuration.buildProcessEngine();
    processEngine.close();
  }

}
