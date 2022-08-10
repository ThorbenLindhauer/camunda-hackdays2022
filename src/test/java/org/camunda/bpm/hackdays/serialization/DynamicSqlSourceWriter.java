package org.camunda.bpm.hackdays.serialization;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.SqlNode;

public class DynamicSqlSourceWriter implements ObjectWriter<DynamicSqlSource> {

  @Override
  public void write(DynamicSqlSource objectToWrite, ObjectOutputStream outStream, Map<Class<?>, ObjectWriter<?>> writerRegistry) throws IOException {

    SqlNode rootSqlNode = (SqlNode) ReflectionUtil.readField(objectToWrite, "rootSqlNode");

    ObjectWriter writer = writerRegistry.get(rootSqlNode.getClass());
    writer.write(rootSqlNode, outStream, writerRegistry);
  }

}
