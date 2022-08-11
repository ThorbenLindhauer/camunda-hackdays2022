package org.camunda.bpm.hackdays;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.hackdays.serialization.KryoObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyBatisMappingPlugin implements ProcessEnginePlugin {

  public static final String MYBATIS_MAPPING_FILENAME = "camunda-mybatis.mapping";
  public static final String MAPPING_DIR = "C:\\Config";

  private static final Logger LOG = LoggerFactory.getLogger(MyBatisMappingPlugin.class);

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    LOG.info("Mybatis mapping pre init");

    Path inputFile = FileSystems.getDefault().getPath(MAPPING_DIR, MYBATIS_MAPPING_FILENAME);
    if (Files.exists(inputFile)) {
      LOG.info("Read mapping from file {}", inputFile);
      try {
        InputStream inputStream = Files.newInputStream(inputFile);

        TransactionFactory realTransactionFactory = null;
        if (processEngineConfiguration.getTransactionFactory() == null) {
          realTransactionFactory = new JdbcTransactionFactory();
        }
        processEngineConfiguration.setTransactionFactory(realTransactionFactory);

        PooledDataSource pooledDataSource = new PooledDataSource(ReflectUtil.getClassLoader(),
            processEngineConfiguration.getJdbcDriver(), processEngineConfiguration.getJdbcUrl(),
            processEngineConfiguration.getJdbcUsername(), processEngineConfiguration.getJdbcPassword());

        processEngineConfiguration.setDataSource(pooledDataSource);

        Environment environment = new Environment("default", realTransactionFactory, pooledDataSource);

        Configuration myBatisConfiguration = new Configuration();
        DefaultSqlSessionFactory sqlSessionFactory = new DefaultSqlSessionFactory(myBatisConfiguration);
        processEngineConfiguration.setSqlSessionFactory(sqlSessionFactory);
        // Configuration mybatisConfiguration =
        // processEngineConfiguration.getSqlSessionFactory().getConfiguration();
        myBatisConfiguration.setEnvironment(environment);
        if (processEngineConfiguration.isJdbcBatchProcessing()) {
          myBatisConfiguration.setDefaultExecutorType(ExecutorType.BATCH);
        }

        KryoObjectMapper mapper = new KryoObjectMapper(myBatisConfiguration);

        List<MappedStatement> mybatisMappings = mapper.read(inputStream, ArrayList.class);
        for (MappedStatement mappedStatement : mybatisMappings) {
          if (myBatisConfiguration.hasStatement(mappedStatement.getId())) {
            // mybatis stores a statement under two keys (short and long), so we
            // iterate every
            // statement twice
            continue;
          }
          myBatisConfiguration.addMappedStatement(mappedStatement);

        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      LOG.info("MyBatis mapping read");
    } else {
      LOG.info("Mapping file doesn't exist, skipped it.");
    }
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    LOG.info("MyBatis mapping post init");
    try {
      Path outputDir = FileSystems.getDefault().getPath(MAPPING_DIR);
      Files.createDirectories(outputDir);
      Path outputFile = outputDir.resolve(MYBATIS_MAPPING_FILENAME);
      if (Files.notExists(outputFile)) {
        LOG.info("Write mapping to file: {}", outputFile);
        Configuration mybatisConfiguration = processEngineConfiguration.getSqlSessionFactory().getConfiguration();
        Collection<MappedStatement> mappedStatements = new ArrayList<>(mybatisConfiguration.getMappedStatements());

        KryoObjectMapper mapper = new KryoObjectMapper(mybatisConfiguration);

        try (OutputStream outStream = Files.newOutputStream(outputFile)) {
          mapper.write(mappedStatements, outStream);
          outStream.flush();
        }
      } else {
        LOG.info("MyBatis mapping file already exists");
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    LOG.info("MyBatis mapping post build");

  }

}
