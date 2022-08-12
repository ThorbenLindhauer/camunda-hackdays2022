package org.camunda.bpm.hackdays;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.hackdays.MappingRegistry.MappingParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyBatisMappingPlugin implements ProcessEnginePlugin {

  private static final Logger LOG = LoggerFactory.getLogger(MyBatisMappingPlugin.class);

  private String mappingDirectory;


  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    LOG.info("Mybatis mapping pre init");

    TransactionFactory realTransactionFactory = null;
    if (processEngineConfiguration.getTransactionFactory() == null) {
      realTransactionFactory = new JdbcTransactionFactory();
    }
    processEngineConfiguration.setTransactionFactory(realTransactionFactory);

    PooledDataSource pooledDataSource = new PooledDataSource(ReflectUtil.getClassLoader(),
        processEngineConfiguration.getJdbcDriver(), processEngineConfiguration.getJdbcUrl(),
        processEngineConfiguration.getJdbcUsername(), processEngineConfiguration.getJdbcPassword());

    processEngineConfiguration.setDataSource(pooledDataSource);

    // the database type is a parameter
    // of the mybatis mappings, so we need to initialize it before we can
    // load them.
    processEngineConfiguration.initDatabaseType();

    Environment environment = new Environment("default", realTransactionFactory, pooledDataSource);

    MappingRegistry mappingRegistry = createMappingRegistry();
    MappingParameters params = MappingParameters.of(processEngineConfiguration);
    Configuration myBatisConfiguration = mappingRegistry.loadMybatisConfiguration(params);

    if (myBatisConfiguration == null) {
      LOG.info("No persisted Mybatis mappings found. Starting engine regularly.");
      return;
    }

    DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(myBatisConfiguration);
    processEngineConfiguration.setSqlSessionFactory(sqlSessionFactory);

    myBatisConfiguration.setEnvironment(environment);
    if (processEngineConfiguration.isJdbcBatchProcessing()) {
      myBatisConfiguration.setDefaultExecutorType(ExecutorType.BATCH);
    }

    LOG.info("MyBatis mapping read");
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    LOG.info("MyBatis mapping post init");

    MappingParameters params = MappingParameters.of(processEngineConfiguration);
    Configuration mybatisConfiguration = processEngineConfiguration.getSqlSessionFactory().getConfiguration();

    MappingRegistry mappingRegistry = createMappingRegistry();
    mappingRegistry.persistMybatisConfiguration(params, mybatisConfiguration);
    LOG.info("MyBatis mapping stored");
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    LOG.info("MyBatis mapping post build");

  }

  private MappingRegistry createMappingRegistry() {
    if (mappingDirectory != null) {
      return new MappingRegistry(FileSystems.getDefault(), mappingDirectory);
    } else {
      return new MappingRegistry();
    }
  }

  public String getMappingDirectory() {
    return mappingDirectory;
  }

  public void setMappingDirectory(String mappingDirectory) {
    this.mappingDirectory = mappingDirectory;
  }

  public static Path getDefaultMappingDirectory(FileSystem fileSystem) {
    String jvmTempDir = System.getProperty("java.io.tmpdir");

    return fileSystem.getPath(jvmTempDir, "camunda-mybatis-mappings");
  }

  public Path getMappingDirectory(FileSystem fileSystem) {
    return mappingDirectory != null ?
        fileSystem.getPath(mappingDirectory) :
        getDefaultMappingDirectory(fileSystem);
  }
}
