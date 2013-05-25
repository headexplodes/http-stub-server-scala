/*
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

case class Foo(bar: String)
    
object testing {

    val mapper = new ObjectMapper() with ScalaObjectMapper
    mapper.registerModule(DefaultScalaModule)
    
    mapper.readValue[Foo](""" {"bar":"blah"} """)
        
}
*/

import com.dz.stubby.core.model.PartialMatchField
import com.dz.stubby.core.model.FieldType
import scala.util.matching.Regex
import com.dz.stubby.core.TextPattern

object testing2 {

        val field1 = new PartialMatchField(FieldType.BODY, "foo", new Regex(".*")).asMatchFailure("bar")
                                                  //> field1  : com.dz.stubby.core.model.MatchField = MatchField(BODY,foo,.*,MATCH
                                                  //| _FAILURE,bar,null)
        val field2 = new PartialMatchField(FieldType.BODY, "foo", ".*").asMatchFailure("bar")
                                                  //> field2  : com.dz.stubby.core.model.MatchField = MatchField(BODY,foo,.*,MATCH
                                                  //| _FAILURE,bar,null)
        
        val r1 = new TextPattern(".*")            //> r1  : com.dz.stubby.core.TextPattern = .*
        val r2 = new TextPattern(".*")            //> r2  : com.dz.stubby.core.TextPattern = .*
 
        "foo" == "foo"                            //> res0: Boolean = true
        r1 == r2                                  //> res1: Boolean = true
        
        field1.equals(field2)                     //> res2: Boolean = false

}