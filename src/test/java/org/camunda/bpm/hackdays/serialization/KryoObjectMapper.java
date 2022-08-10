package org.camunda.bpm.hackdays.serialization;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.StaticTextSqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.scripting.xmltags.TrimSqlNode;
import org.apache.ibatis.scripting.xmltags.VarDeclSqlNode;
import org.apache.ibatis.scripting.xmltags.WhereSqlNode;
import org.apache.ibatis.session.Configuration;
import org.camunda.bpm.hackdays.serialization.kryo.ConfigurationSerializer;
import org.camunda.bpm.hackdays.serialization.kryo.ImmutableListSerializer;
import org.camunda.bpm.hackdays.serialization.kryo.LogSerializer;
import org.camunda.bpm.hackdays.serialization.kryo.MappedStatementSerializer;
import org.camunda.bpm.hackdays.serialization.kryo.UnmodifiableSetSerializer;
import org.objenesis.instantiator.ObjectInstantiator;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoObjectMapper {

  private Kryo kryo;

  public KryoObjectMapper(Configuration configuration) {
    kryo = new Kryo();
    kryo.setRegistrationRequired(false);

    kryo.register(MappedStatement.class, new MappedStatementSerializer(kryo));
    kryo.register(Configuration.class, new ConfigurationSerializer(configuration));
//    kryo.register(Log.class, new LogSerializer());

    Class<? extends List> unmodifiableListClass = Collections.unmodifiableList(new ArrayList<String>()).getClass();
    kryo.register(
        unmodifiableListClass, new ImmutableListSerializer());
    Class<? extends Set> unmodifiableSetClass = Collections.unmodifiableSet(new HashSet<String>()).getClass();
    kryo.register(unmodifiableSetClass, new UnmodifiableSetSerializer());
    Registration registration = kryo.register(DynamicSqlSource.class);
    registration.setInstantiator(new ObjectInstantiator<DynamicSqlSource>() {
      @Override
      public DynamicSqlSource newInstance() {
        return new DynamicSqlSource(null, null);
      }
    });

    registration = kryo.register(MixedSqlNode.class);
    registration.setInstantiator(new ObjectInstantiator<MixedSqlNode>() {
      @Override
      public MixedSqlNode newInstance() {
        return new MixedSqlNode(null);
      }
    });


    registration = kryo.register(StaticTextSqlNode.class);
    registration.setInstantiator(new ObjectInstantiator<StaticTextSqlNode>() {
      @Override
      public StaticTextSqlNode newInstance() {
        return new StaticTextSqlNode(null);
      }
    });

    registration = kryo.register(ChooseSqlNode.class);
    registration.setInstantiator(new ObjectInstantiator<ChooseSqlNode>() {
      @Override
      public ChooseSqlNode newInstance() {
        return new ChooseSqlNode(null, null);
      }
    });

    registration = kryo.register(IfSqlNode.class);
    registration.setInstantiator(new ObjectInstantiator<IfSqlNode>() {
      @Override
      public IfSqlNode newInstance() {
        return new IfSqlNode(null, null);
      }
    });


    registration = kryo.register(ForEachSqlNode.class);
    registration.setInstantiator(new ObjectInstantiator<ForEachSqlNode>() {
      @Override
      public ForEachSqlNode newInstance() {
        return new ForEachSqlNode(null, null, null, null, null, null, null, null);
      }
    });


    registration = kryo.register(TextSqlNode.class);
    registration.setInstantiator(new ObjectInstantiator<TextSqlNode>() {
      @Override
      public TextSqlNode newInstance() {
        return new TextSqlNode(null);
      }
    });


    registration = kryo.register(TrimSqlNode.class);
    registration.setInstantiator(new ObjectInstantiator<TrimSqlNode>() {
      @Override
      public TrimSqlNode newInstance() {
        return new TrimSqlNode(null, null, null, null, null, null);
      }
    });


    registration = kryo.register(VarDeclSqlNode.class);
    registration.setInstantiator(new ObjectInstantiator<VarDeclSqlNode>() {
      @Override
      public VarDeclSqlNode newInstance() {
        return new VarDeclSqlNode(null, null);
      }
    });


    registration = kryo.register(WhereSqlNode.class);
    registration.setInstantiator(new ObjectInstantiator<WhereSqlNode>() {
      @Override
      public WhereSqlNode newInstance() {
        return new WhereSqlNode(null, null);
      }
    });


//    registration = kryo.register(RawSqlSource.class);
//    registration.setInstantiator(new ObjectInstantiator<RawSqlSource>() {
//      @Override
//      public RawSqlSource newInstance() {
//        return new RawSqlSource((Configuration) null, (SqlNode) null, (Class<?>) null);
//      }
//    });
  }

  public void write(Object objectToWrite, OutputStream outStream) {

    Output output = new Output(outStream);
    kryo.writeObject(output, objectToWrite);
    output.close();
  }

  public <T> T read(InputStream inputStream, Class<T> clazz) {
    return kryo.readObject(new Input(inputStream), clazz);
  }
}
