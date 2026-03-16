package org.mule.extension.vectors.internal.store.qdrant;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.Map;

import com.google.protobuf.InvalidProtocolBufferException;
import io.qdrant.client.grpc.JsonWithInt;
import org.junit.jupiter.api.Test;

class JsonFactoryTest {

  @Test
  void toJson_stringValue() throws InvalidProtocolBufferException {
    JsonWithInt.Value value = JsonWithInt.Value.newBuilder().setStringValue("hello").build();
    String json = invokeToJson(Map.of("key", value));
    assertThat(json).contains("hello");
  }

  @Test
  void toJson_boolValue() throws InvalidProtocolBufferException {
    JsonWithInt.Value value = JsonWithInt.Value.newBuilder().setBoolValue(true).build();
    String json = invokeToJson(Map.of("flag", value));
    assertThat(json).contains("true");
  }

  @Test
  void toJson_integerValue() throws InvalidProtocolBufferException {
    JsonWithInt.Value value = JsonWithInt.Value.newBuilder().setIntegerValue(42).build();
    String json = invokeToJson(Map.of("count", value));
    assertThat(json).contains("42");
  }

  @Test
  void toJson_doubleValue() throws InvalidProtocolBufferException {
    JsonWithInt.Value value = JsonWithInt.Value.newBuilder().setDoubleValue(3.14).build();
    String json = invokeToJson(Map.of("pi", value));
    assertThat(json).contains("3.14");
  }

  @Test
  void toJson_nullValue() throws InvalidProtocolBufferException {
    JsonWithInt.Value value = JsonWithInt.Value.newBuilder()
        .setNullValue(io.qdrant.client.grpc.JsonWithInt.NullValue.NULL_VALUE).build();
    String json = invokeToJson(Map.of("nil", value));
    assertThat(json).contains("null");
  }

  @Test
  void toJson_structValue() throws InvalidProtocolBufferException {
    JsonWithInt.Struct innerStruct = JsonWithInt.Struct.newBuilder()
        .putFields("inner", JsonWithInt.Value.newBuilder().setStringValue("nested").build())
        .build();
    JsonWithInt.Value value = JsonWithInt.Value.newBuilder().setStructValue(innerStruct).build();
    String json = invokeToJson(Map.of("obj", value));
    assertThat(json).contains("nested");
    assertThat(json).contains("inner");
  }

  @Test
  void toJson_listValue() throws InvalidProtocolBufferException {
    JsonWithInt.ListValue listValue = JsonWithInt.ListValue.newBuilder()
        .addValues(JsonWithInt.Value.newBuilder().setStringValue("a").build())
        .addValues(JsonWithInt.Value.newBuilder().setIntegerValue(1).build())
        .build();
    JsonWithInt.Value value = JsonWithInt.Value.newBuilder().setListValue(listValue).build();
    String json = invokeToJson(Map.of("arr", value));
    assertThat(json).contains("a");
  }

  @Test
  void toJson_emptyMap() throws InvalidProtocolBufferException {
    String json = invokeToJson(Collections.emptyMap());
    assertThat(json.replaceAll("\\s+", "")).isEqualTo("{}");
  }

  @Test
  void toJson_mixedValues() throws InvalidProtocolBufferException {
    Map<String, JsonWithInt.Value> map = Map.of(
                                                "str", JsonWithInt.Value.newBuilder().setStringValue("hello").build(),
                                                "num", JsonWithInt.Value.newBuilder().setDoubleValue(1.5).build(),
                                                "flag", JsonWithInt.Value.newBuilder().setBoolValue(false).build());
    String json = invokeToJson(map);
    assertThat(json).contains("hello").contains("1.5").contains("false");
  }

  private String invokeToJson(Map<String, JsonWithInt.Value> map) throws InvalidProtocolBufferException {
    try {
      var method = Class.forName("org.mule.extension.vectors.internal.service.store.qdrant.JsonFactory")
          .getDeclaredMethod("toJson", Map.class);
      method.setAccessible(true);
      return (String) method.invoke(null, map);
    } catch (java.lang.reflect.InvocationTargetException e) {
      if (e.getCause() instanceof InvalidProtocolBufferException) {
        throw (InvalidProtocolBufferException) e.getCause();
      }
      throw new RuntimeException(e.getCause());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
