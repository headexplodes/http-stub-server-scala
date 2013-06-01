import com.dz.stubby.core.util.ListUtils

object testing {

  val map: Map[String, String] = Map("foo" -> "bar")
                                                  //> map  : Map[String,String] = Map(foo -> bar)
  val unknown: Any = map                          //> unknown  : Any = Map(foo -> bar)

  val matched: Map[_,Any] = unknown match {
    case m: Map[_, _] => m
  }                                               //> matched  : Map[_, Any] = Map(foo -> bar)

  val testing: Map[_,Any] = matched               //> testing  : Map[_, Any] = Map(foo -> bar)

 
}