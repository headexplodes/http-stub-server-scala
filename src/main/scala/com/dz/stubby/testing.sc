
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

case class Foo(bar: String)
    
object testing {

    val mapper = new ObjectMapper() with ScalaObjectMapper
                                                  //> mapper  : com.fasterxml.jackson.databind.ObjectMapper with com.fasterxml.jac
                                                  //| kson.module.scala.experimental.ScalaObjectMapper = testing$$anonfun$main$1$$
                                                  //| anon$1@16fe1b92
    mapper.registerModule(DefaultScalaModule)     //> res0: com.fasterxml.jackson.databind.ObjectMapper = testing$$anonfun$main$1$
                                                  //| $anon$1@16fe1b92
    
    mapper.readValue[Foo](""" {"bar":"blah"} """) //> res1: Foo = Foo(blah)
        

}