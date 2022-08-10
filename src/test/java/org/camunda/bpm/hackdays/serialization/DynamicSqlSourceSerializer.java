package org.camunda.bpm.hackdays.serialization;

import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.session.Configuration;

public class DynamicSqlSourceSerializer {


  public static class ExtendedSqlSource extends DynamicSqlSource {

    public ExtendedSqlSource() {
      super(null, null);
    }

  }
}
