import com.dz.stubby.core.util._

case class Foo(foo: String)

object testing2 {

  //val jsonContent = """{"test":"113123","myList":{"test2":"321323"}}"""

  //JsonUtils.defaultMapper.readValue(jsonContent, classOf[Object])


  val jsonContent = """{"foo":"bar"}"""           //> jsonContent  : String = {"foo":"bar"}

  JsonUtils.defaultMapper.readValue[Foo](jsonContent)
                                                  //> res0: Foo = Foo(bar)

}