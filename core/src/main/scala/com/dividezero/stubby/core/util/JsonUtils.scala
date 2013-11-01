package com.dividezero.stubby.core.util

import java.io.InputStream
import java.io.OutputStream

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JsonUtils {

  def createDefaultMapper() = {
    val mapper = new ObjectMapper with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS) // for 'exact' floating-point matches
    mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)
    mapper.setSerializationInclusion(Include.NON_NULL)
    mapper
  }

  lazy val defaultMapper = createDefaultMapper()
  lazy val prettyWriter = defaultMapper.writerWithDefaultPrettyPrinter()

  def serialize(obj: AnyRef): String = defaultMapper.writeValueAsString(obj)
  def serialize(stream: OutputStream, obj: AnyRef): Unit = defaultMapper.writeValue(stream, obj)

  def deserializeObject(str: String): AnyRef = defaultMapper.readValue(str, classOf[Object])
  def deserialize[T: Manifest](str: String): T = defaultMapper.readValue[T](str)
  def deserialize[T: Manifest](stream: InputStream): T = defaultMapper.readValue[T](stream)

  def prettyPrint(value: AnyRef): String = prettyWriter.writeValueAsString(value)

}
