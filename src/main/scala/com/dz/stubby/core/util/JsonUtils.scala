package com.dz.stubby.core.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JsonUtils {

  def defaultMapper = {
    val mapper = new ObjectMapper with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper
  }
  
  //def deserialize(str: String, toType: Class[_]) = defaultMapper.convertValue

  //
  //    public static ObjectMapper mapper() {
  //        return new ObjectMapper();
  //    }
  //    
  //    public static ObjectMapper defaultMapper() {
  //        ObjectMapper result = mapper();
  //        result.enable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
  //        result.enable(DeserializationConfig.Feature.USE_BIG_DECIMAL_FOR_FLOATS); // for 'exact' floating-point matches
  //        result.setSerializationInclusion(Inclusion.NON_NULL);
  //        return result;
  //    }
  //    
  //    public static ObjectWriter prettyWriter() {
  //        return defaultMapper().writerWithDefaultPrettyPrinter();
  //    }
  //    
  //    public static String prettyPrint(Object value) {
  //        try {
  //            return prettyWriter().writeValueAsString(value); 
  //        } catch (IOException e) {
  //            throw new RuntimeException("Error serializing JSON", e);
  //        }
  //    }
  //
  //    public static String serialize(Object object) {
  //        try {
  //            return defaultMapper().writeValueAsString(object);
  //        } catch (IOException e) {
  //            throw new RuntimeException("Error serializing JSON", e);
  //        }
  //    }
  //    
  //    public static void serialize(OutputStream stream, Object object) {
  //        try {
  //            defaultMapper().writeValue(stream, object);
  //        } catch (IOException e) {
  //            throw new RuntimeException("Error serializing JSON", e);
  //        }
  //    }
  //
  //    public static <T> T deserialize(String json, Class<T> type) {
  //        try {
  //            return defaultMapper().readValue(json, type);
  //        } catch (IOException e) {
  //            throw new RuntimeException("Error deserializing JSON", e);
  //        }
  //    }

  //    public static <T> T deserialize(InputStream stream, Class<T> type) {
  //        try {
  //            return defaultMapper().readValue(stream, type);
  //        } catch (IOException e) {
  //            throw new RuntimeException("Error deserializing JSON", e);
  //        }
  //    }
}