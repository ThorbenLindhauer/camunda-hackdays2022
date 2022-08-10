package org.camunda.bpm.hackdays.serialization;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappedStatementWriter implements ObjectWriter<MappedStatement> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MappedStatementWriter.class);

  @Override
  public void write(MappedStatement mappedStatement, ObjectOutputStream outStream, Map<Class<?>, ObjectWriter<?>> writerRegistry) throws IOException {
    outStream.writeObject(mappedStatement.getResource());
    // config
    outStream.writeObject(mappedStatement.getId());
    outStream.writeObject(mappedStatement.getFetchSize());
    outStream.writeObject(mappedStatement.getTimeout());
    outStream.writeObject(mappedStatement.getStatementType());
    outStream.writeObject(mappedStatement.getResultSetType());
    // sqlSource
    SqlSource sqlSource = mappedStatement.getSqlSource();
    ObjectWriter writer = writerRegistry.get(sqlSource.getClass());
    writer.write(sqlSource, outStream, writerRegistry);
    // parameterMap;
    // resultMaps;
    outStream.writeBoolean(mappedStatement.isFlushCacheRequired());
    outStream.writeBoolean(mappedStatement.isUseCache());
    outStream.writeBoolean(mappedStatement.isResultOrdered());
    outStream.writeObject(mappedStatement.getSqlCommandType());
    // keyGenerator
    outStream.writeObject(mappedStatement.getKeyProperties());
    outStream.writeObject(mappedStatement.getKeyColumns());
    // TODO: we think we don't need the next property, because
    // it is generated when result maps are restored
//    outStream.writeBoolean(mappedStatement.hasNestedResultMaps());
    outStream.writeObject(mappedStatement.getDatabaseId());
    // statementLog
    // lang;
    outStream.writeObject(mappedStatement.getResultSets());

    LOGGER.info("Serialized properties - resource: {}, result sets: {}", mappedStatement.getResource(), mappedStatement.getResultSets());
  }

}
