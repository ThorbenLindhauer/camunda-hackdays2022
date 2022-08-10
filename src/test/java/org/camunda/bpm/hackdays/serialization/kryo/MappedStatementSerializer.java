package org.camunda.bpm.hackdays.serialization.kryo;

import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.MappedStatement;
import org.camunda.bpm.hackdays.serialization.ReflectionUtil;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

public class MappedStatementSerializer extends FieldSerializer<MappedStatement> {

  public MappedStatementSerializer(Kryo kryo) {
    super(kryo, MappedStatement.class);
    removeField("statementLog");
  }

  @Override
  public MappedStatement read(Kryo kryo, Input input, Class<? extends MappedStatement> type) {
    MappedStatement statement = super.read(kryo, input, type);

    String logId = statement.getId();
//    if (configuration.getLogPrefix() != null) {
//      logId = configuration.getLogPrefix() + id;
//    }
    ReflectionUtil.setField(statement, "statementLog", LogFactory.getLog(logId));

    return statement;
  }

}
