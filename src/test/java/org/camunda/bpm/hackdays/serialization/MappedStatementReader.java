package org.camunda.bpm.hackdays.serialization;

import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.session.Configuration;

public class MappedStatementReader implements ObjectReader<MappedStatement> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MappedStatementReader.class);

  private Configuration configuration;

  public MappedStatementReader(Configuration configuration) {
    this.configuration = configuration;
  }

  @Override
  public MappedStatement readObject(ObjectInputStream inputStream, Map<Class<?>, ObjectReader<?>> readerRegistry) throws Exception {
    String resource = (String) inputStream.readObject();
    // config
    String id = (String) inputStream.readObject();
    Integer fetchSize = (Integer) inputStream.readObject();
    Integer timeout = (Integer) inputStream.readObject();
    StatementType statementType = (StatementType) inputStream.readObject();
    ResultSetType resultSetType = (ResultSetType) inputStream.readObject();
    // sqlSource
    ObjectReader<?> sqlSourceReader = readerRegistry.get(DynamicSqlSource.class);
    DynamicSqlSource sqlSource = (DynamicSqlSource) sqlSourceReader.readObject(inputStream, readerRegistry);

    // parameterMap;
    // resultMaps;
    boolean flushCacheRequired = inputStream.readBoolean();
    boolean useCache = inputStream.readBoolean();
    boolean resultOrdered = inputStream.readBoolean();
    SqlCommandType sqlCommandType = (SqlCommandType) inputStream.readObject();
    // keyGenerator
    String[] keyProperties = (String[]) inputStream.readObject();
    String[] keyColumns = (String[]) inputStream.readObject();
//    boolean nestedResultMaps = inputStream.readBoolean();
    String databaseId = (String) inputStream.readObject();
    // statementLog
    // lang;
    String[] resultSets = (String[]) inputStream.readObject();
    LOGGER.info("Deserialized properties - resource: {}, result sets: {}", resource, resultSets);

    Builder builder = new MappedStatement.Builder(configuration, id, sqlSource, sqlCommandType)
        .resource(resource)
        .fetchSize(fetchSize)
        .timeout(timeout)
        .statementType(statementType)
        .resultSetType(resultSetType)
        .flushCacheRequired(flushCacheRequired)
        .useCache(useCache)
        .resultOrdered(resultOrdered)
        .keyProperty(join(keyProperties))
        .keyColumn(join(keyColumns))
        .databaseId(databaseId)
        .resultSets(join(resultSets));

    LOGGER.info("Mapped statment builder: {}", builder);

//    private Integer timeout;
//    private StatementType statementType;
//    private ResultSetType resultSetType;
//    private SqlSource sqlSource;
//    private Cache cache;
//    private ParameterMap parameterMap;
//    private List<ResultMap> resultMaps;
//    private boolean flushCacheRequired;
//    private boolean useCache;
//    private boolean resultOrdered;
//    private SqlCommandType sqlCommandType;
//    private KeyGenerator keyGenerator;
//    private String[] keyProperties;
//    private String[] keyColumns;
//    private boolean hasNestedResultMaps;
//    private String databaseId;
//    private Log statementLog;
//    private LanguageDriver lang;
//    private String[] resultSets;

    return builder.build();
  }
  private static String join(String[] array) {

    if (array == null) {
      return null;
    }

    List<String> list = Arrays.asList(array);
    return String.join(",", list);
  }

}
