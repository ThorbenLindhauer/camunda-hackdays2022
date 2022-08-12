package org.camunda.bpm.hackdays;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.Configuration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.util.ParseUtil;
import org.camunda.bpm.engine.impl.util.ProcessEngineDetails;
import org.camunda.bpm.hackdays.serialization.KryoObjectMapper;
import org.camunda.bpm.hackdays.serialization.KryoObjectMapper.KryoReader;
import org.camunda.bpm.hackdays.serialization.KryoObjectMapper.KryoWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MappingRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(MappingRegistry.class);

  public static final String MYBATIS_MAPPING_FILENAME_PATTERN = "camunda-%s.mapping";
  public static final String MYBATIS_INDEX_FILENAME_PATTERN = "camunda-%s.index";
  public static final String DEFAULT_MAPPING_DIR = System.getProperty("java.io.tmpdir") + "/camunda-mybatis-mappings";

  private Path mappingDirectory;

  public MappingRegistry() {
    this(FileSystems.getDefault(), null);
  }

  public MappingRegistry(FileSystem fileSystem, String mappingDirectory) {
    this.mappingDirectory = resolveMappingDirectory(fileSystem, mappingDirectory);
  }

  public void persistMybatisConfiguration(MappingParameters mappingParameters,
      Configuration configuration) {

    ensureMappingDirectoryExists();

    writeMappingFile(mappingParameters, configuration);
    writeIndexFile(mappingParameters);
  }

  private void writeIndexFile(MappingParameters mappingParameters) {
    Path outputFile = mappingDirectory.resolve(mappingParameters.getIndexFileName());
    if (Files.exists(outputFile)) {
      LOG.info("Index file at {} already exists. Not overwriting.", outputFile);
      return;
    }

    try (OutputStream outStream = Files.newOutputStream(outputFile)) {
      ObjectMapper jsonMapper = new ObjectMapper();
      jsonMapper.writeValue(outStream, mappingParameters);

      outStream.flush();
    } catch (Exception e) {
      throw new RuntimeException("Could not persist Mybatis mappings on file system", e);
    }
  }

  private void writeMappingFile(MappingParameters mappingParameters, Configuration configuration) {
    Path outputFile = mappingDirectory.resolve(mappingParameters.getMappingFileName());
    if (Files.exists(outputFile)) {
      LOG.info("Mapping at {} already exists. Not overwriting.", outputFile);
      return;
    }

    LOG.info("Writing Mybatis mappings to file: {}", outputFile);

    Collection<MappedStatement> mappedStatements = deduplicateAndFilterCollection(configuration.getMappedStatements());
    List<ResultMap> resultMaps = deduplicateAndFilterCollection(configuration.getResultMaps());

    KryoObjectMapper mapper = new KryoObjectMapper(configuration);

    try (OutputStream outStream = Files.newOutputStream(outputFile)) {
      KryoWriter writer = mapper.createWriter(outStream);
      writer.write(mappedStatements);
      writer.write(resultMaps);

      outStream.flush();
    } catch (Exception e) {
      throw new RuntimeException("Could not persist Mybatis mappings on file system", e);
    }
  }

  /**
   * @param mappingParameters
   * @return null if there is no persisted configuration
   */
  public Configuration loadMybatisConfiguration(MappingParameters mappingParameters) {

    Path inputFile = mappingDirectory.resolve(mappingParameters.getMappingFileName());

    if (!Files.exists(inputFile)) {
      LOG.info("Mapping file {} doesn't exist, not loading it.", inputFile);
      return null;
    }

    Configuration result = new Configuration();

    InputStream inputStream;
    try {
      inputStream = Files.newInputStream(inputFile);
    } catch (IOException e) {
      throw new RuntimeException("Could not restore Mybatis mappings from file system", e);
    }

    LOG.info("Read mappings from file {}", inputFile);

    KryoObjectMapper mapper = new KryoObjectMapper(result);
    KryoReader reader = mapper.createReader(inputStream);

    List<MappedStatement> mybatisMappings = (List<MappedStatement>) reader.readNextObject();
    mybatisMappings.forEach(result::addMappedStatement);

    List<ResultMap> resultMaps = (List<ResultMap>) reader.readNextObject();
    resultMaps.forEach(result::addResultMap);

    return result;
  }

  private void ensureMappingDirectoryExists() {
    try {
      Files.createDirectories(mappingDirectory);
    } catch (IOException e) {
      throw new RuntimeException("Could not create mapping directory " + mappingDirectory, e);
    }
  }

  public static Path getDefaultMappingDirectory(FileSystem fileSystem) {
    String jvmTempDir = System.getProperty("java.io.tmpdir");

    return fileSystem.getPath(jvmTempDir, "camunda-mybatis-mappings");
  }

  private Path resolveMappingDirectory(FileSystem fileSystem, String directoryPath) {
    return directoryPath != null ?
        fileSystem.getPath(directoryPath) :
        getDefaultMappingDirectory(fileSystem);
  }

  private <T> ArrayList<T> deduplicateAndFilterCollection(Collection<T> collection) {
    return new ArrayList<>(new HashSet<>(filterCollectionByAmbiguityClass(collection)));
  }

  private <T> ArrayList<T> filterCollectionByAmbiguityClass(Collection<T> collection) {
    return collection.stream()
        .filter(i -> !i.getClass().getName().contains("Ambiguity"))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static class MappingParameters {
    // TODO: implement more params
    private String engineVersion;
    private Properties parsingProperties;

    public String hash() {
      int hash = Objects.hash(engineVersion, parsingProperties);
      int positiveHash = Math.abs(hash);

      return Integer.toString(positiveHash);
    }

    @JsonIgnore
    public String getMappingFileName() {
      return String.format(MYBATIS_MAPPING_FILENAME_PATTERN, hash());
    }

    @JsonIgnore
    public String getIndexFileName() {
      return String.format(MYBATIS_INDEX_FILENAME_PATTERN, hash());
    }

    public static MappingParameters of(ProcessEngineConfigurationImpl engineConfiguration) {
      MappingParameters result = new MappingParameters();

      ProcessEngineDetails engineInfo = ParseUtil.parseProcessEngineVersion(true);
      String engineVersion = engineInfo.getVersion();
      result.engineVersion = engineVersion;

      result.parsingProperties = getParsingProperties(engineConfiguration);

      return result;
    }

    private static Properties getParsingProperties(ProcessEngineConfigurationImpl engineConfiguration) {
      Properties properties = new Properties();

      if (engineConfiguration.isUseSharedSqlSessionFactory()) {
        properties.put("prefix", "${@org.camunda.bpm.engine.impl.context.Context@getProcessEngineConfiguration().databaseTablePrefix}");
      } else {
        properties.put("prefix", engineConfiguration.getDatabaseTablePrefix());
      }

      ProcessEngineConfigurationImpl.initSqlSessionFactoryProperties(properties,
          engineConfiguration.getDatabaseTablePrefix(),
          engineConfiguration.getDatabaseType());

      return properties;
    }

    public String getEngineVersion() {
      return engineVersion;
    }

    public void setEngineVersion(String engineVersion) {
      this.engineVersion = engineVersion;
    }

    public Properties getParsingProperties() {
      return parsingProperties;
    }

    public void setParsingProperties(Properties parsingProperties) {
      this.parsingProperties = parsingProperties;
    }
  }

}
