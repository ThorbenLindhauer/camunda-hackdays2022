package org.camunda.bpm.hackdays.serialization.kryo;

import java.util.ArrayList;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.ImmutableCollectionsSerializers;

/**
 * Copied from {@link ImmutableCollectionsSerializers}
 */
public class ImmutableListSerializer extends CollectionSerializer<List<Object>> {

  public ImmutableListSerializer () {
    setElementsCanBeNull(false);
  }

  @Override
  protected List<Object> create (Kryo kryo, Input input, Class<? extends List<Object>> type, int size) {
    return new ArrayList<>(size);
  }

  @Override
  protected List<Object> createCopy (Kryo kryo, List<Object> original) {
    return new ArrayList<>(original.size());
  }

  @Override
  public List<Object> read (Kryo kryo, Input input, Class<? extends List<Object>> type) {
    return super.read(kryo, input, type);
  }

  @Override
  public List<Object> copy (Kryo kryo, List<Object> original) {
    throw new UnsupportedOperationException();
  }
}