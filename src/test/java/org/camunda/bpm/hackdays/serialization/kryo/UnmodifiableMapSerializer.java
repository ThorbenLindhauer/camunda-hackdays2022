package org.camunda.bpm.hackdays.serialization.kryo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.ImmutableCollectionsSerializers;
import com.esotericsoftware.kryo.serializers.MapSerializer;

/**
 * Copied from {@link ImmutableCollectionsSerializers}
 */
public class UnmodifiableMapSerializer extends MapSerializer<Map<?, ?>> {

  public UnmodifiableMapSerializer () {
  }

  @Override
  protected Map<?, ?> create (Kryo kryo, Input input, Class<? extends Map<?, ?>> type, int size) {
    return new HashMap<>(size);
  }

  @Override
  protected Map<?, ?> createCopy(Kryo kryo, Map<?, ?> original) {
    return new HashMap<>(original.size());
  }

  @Override
  public Map<?, ?> read (Kryo kryo, Input input, Class<? extends Map<?, ?>> type) {
    return super.read(kryo, input, type);
  }

}