package org.camunda.bpm.hackdays.serialization;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SetSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.scripting.xmltags.TrimSqlNode;
import org.apache.ibatis.scripting.xmltags.VarDeclSqlNode;
import org.apache.ibatis.scripting.xmltags.WhereSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.UnknownTypeHandler;
import org.camunda.bpm.hackdays.serialization.kryo.ConfigurationSerializer;
import org.camunda.bpm.hackdays.serialization.kryo.ImmutableListSerializer;
import org.camunda.bpm.hackdays.serialization.kryo.LogSerializer;
import org.camunda.bpm.hackdays.serialization.kryo.MappedStatementSerializer;
import org.camunda.bpm.hackdays.serialization.kryo.UnknownTypeHandlerSerializer;
import org.camunda.bpm.hackdays.serialization.kryo.UnmodifiableMapSerializer;
import org.camunda.bpm.hackdays.serialization.kryo.UnmodifiableSetSerializer;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoObjectMapper {

  private Kryo kryo;

  public KryoObjectMapper(Configuration configuration) {
    kryo = new Kryo();
    kryo.setRegistrationRequired(false);
    kryo.setInstantiatorStrategy(new StdInstantiatorStrategy()); // creates objects without calling instructors

    kryo.register(MappedStatement.class, new MappedStatementSerializer(kryo));
    kryo.register(Configuration.class, new ConfigurationSerializer(configuration));
    kryo.register(UnknownTypeHandler.class, new UnknownTypeHandlerSerializer(configuration));

    Class<? extends List> unmodifiableListClass = Collections.unmodifiableList(new ArrayList<String>()).getClass();
    kryo.register(
        unmodifiableListClass, new ImmutableListSerializer());
    Class<? extends Set> unmodifiableSetClass = Collections.unmodifiableSet(new HashSet<String>()).getClass();
    kryo.register(unmodifiableSetClass, new UnmodifiableSetSerializer());
    Class<? extends Map> unmodifiableMapClass = Collections.unmodifiableMap(new HashMap<>()).getClass();
    kryo.register(unmodifiableMapClass, new UnmodifiableMapSerializer());

    Registration registration = kryo.register(UnknownTypeHandler.class);
    registration.setInstantiator(new ObjectInstantiator<UnknownTypeHandler>() {
      @Override
      public UnknownTypeHandler newInstance() {
        return new UnknownTypeHandler(configuration);
      }
    });
  }

  public static class KryoWriter {

    private Kryo kryo;
    private Output output;

    public KryoWriter(Kryo kryo, Output output) {
      this.kryo = kryo;
      this.output = output;
    }

    public void write(Object objectToWrite) {
      kryo.writeClassAndObject(output, objectToWrite);
      output.flush();
    }
  }

  public static class KryoReader {

    private Kryo kryo;
    private Input input;

    public KryoReader(Kryo kryo, Input input) {
      this.kryo = kryo;
      this.input = input;
    }

    public Object readNextObject() {
      return kryo.readClassAndObject(input);
    }
  }

  public KryoWriter createWriter(OutputStream outStream) {

    return new KryoWriter(kryo, new Output(outStream));
  }

  public KryoReader createReader(InputStream inputStream) {
    return new KryoReader(kryo, new Input(inputStream));
  }
}
