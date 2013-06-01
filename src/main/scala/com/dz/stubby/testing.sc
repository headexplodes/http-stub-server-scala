import com.dz.stubby.core.util._

object testing {

  val deserialized1 = JsonUtils.deserializeObject("{}")
                                                  //> deserialized1  : AnyRef = Map()
 
  val deserialized2 = JsonUtils.deserializeObject("[]")
                                                  //> deserialized2  : AnyRef = Buffer()
   
 
}