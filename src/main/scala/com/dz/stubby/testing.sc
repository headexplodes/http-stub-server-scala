import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper



object testing2 {

  def mapper = new ObjectMapper with ScalaObjectMapper
                                                  //> mapper: => com.fasterxml.jackson.databind.ObjectMapper with com.fasterxml.ja
                                                  //| ckson.module.scala.experimental.ScalaObjectMapper
  
  mapper.registerModule(DefaultScalaModule)       //> res0: com.fasterxml.jackson.databind.ObjectMapper = testing2$$anonfun$main$1
                                                  //| $$anon$1@79d3dd34

    val jsonContent ="""{"test":"113123","myList":{"test2":"321323"}}"""
                                                  //> jsonContent  : String = {"test":"113123","myList":{"test2":"321323"}}
    
    mapper.readValue(jsonContent, classOf[Map[String,String]])
                                                  //> com.fasterxml.jackson.databind.JsonMappingException: Can not construct insta
                                                  //| nce of scala.collection.immutable.Map, problem: abstract types either need t
                                                  //| o be mapped to concrete types, have custom deserializer, or be instantiated 
                                                  //| with additional type information
                                                  //|  at [Source: java.io.StringReader@11ca437b; line: 1, column: 1]
                                                  //| 	at com.fasterxml.jackson.databind.JsonMappingException.from(JsonMappingE
                                                  //| xception.java:164)
                                                  //| 	at com.fasterxml.jackson.databind.DeserializationContext.instantiationEx
                                                  //| ception(DeserializationContext.java:600)
                                                  //| 	at com.fasterxml.jackson.databind.deser.AbstractDeserializer.deserialize
                                                  //| (AbstractDeserializer.java:114)
                                                  //| 	at com.fasterxml.jackson.databind.ObjectMapper._readMapAndClose(ObjectMa
                                                  //| pper.java:2888)
                                                  //| 	at com.fasterxml.jackson.databind.ObjectMapper.readValue(ObjectMapper.ja
                                                  //| va:2034)
                                                  //| 	at testing2$$anonfun$main$1.apply$mcV$sp(testing2.scala:15)
                                                  //| 	at org.scalaide.worksheet.runtime.library.WorksheetSupp
                                                  //| Output exceeds cutoff limit.

}