package org.camunda.bpm.hackdays.serialization.kryo;

import java.util.HashSet;
import java.util.Set;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.ImmutableCollectionsSerializers;

/**
 * Copied from {@link ImmutableCollectionsSerializers}
 */
public class UnmodifiableSetSerializer extends CollectionSerializer<Set<Object>> {

  public UnmodifiableSetSerializer () {
    setElementsCanBeNull(false);
  }

  @Override
  protected Set<Object> create (Kryo kryo, Input input, Class<? extends Set<Object>> type, int size) {
    return new HashSet<>(size);
  }

  @Override
  protected Set<Object> createCopy(Kryo kryo, Set<Object> original) {
    return new HashSet<>(original.size());
  }

  @Override
  public Set<Object> read (Kryo kryo, Input input, Class<? extends Set<Object>> type) {
    return super.read(kryo, input, type);
  }

  @Override
  public Set<Object> copy (Kryo kryo, Set<Object> original) {
    throw new UnsupportedOperationException();
  }
}