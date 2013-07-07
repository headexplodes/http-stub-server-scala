package com.dz.stubby.core.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.`type`.CollectionLikeType
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.module.scala.JacksonModule
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.`type`.TypeModifier
import java.lang.reflect.ParameterizedType
import com.fasterxml.jackson.databind.`type`.TypeBindings
import com.fasterxml.jackson.databind.`type`.SimpleType
import java.lang.reflect.Type

object CustomScalaModule extends JacksonModule {
  this += NilTypeModifier
  this += NilDeserializerResolver
}

private class NilDeserializer(elementType: JavaType, deser: JsonDeserializer[_])
    extends JsonDeserializer[List[AnyRef]] with ContextualDeserializer {

  override def createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer[_] = {
    val cd = ctxt.findContextualValueDeserializer(elementType, property)
    if (cd != null) new NilDeserializer(elementType, cd)
    else this
  }

  override def getNullValue = Nil

  override def deserialize(jp: JsonParser, ctxt: DeserializationContext) = {
    deser.deserialize(jp, ctxt).asInstanceOf[List[AnyRef]]
  }
}

private object NilDeserializerResolver extends Deserializers.Base {

  private val LIST = classOf[List[AnyRef]]

  override def findCollectionLikeDeserializer(
    theType: CollectionLikeType,
    config: DeserializationConfig,
    beanDesc: BeanDescription,
    elementTypeDeserializer: TypeDeserializer,
    elementDeserializer: JsonDeserializer[_]) = {
    
    if (!LIST.isAssignableFrom(theType.getRawClass)) {
      null
    } else {
      new NilDeserializer(theType.containedType(0), elementDeserializer)
    }
  }
}

//trait NilDeserializerModule extends NilTypeModifierModule {
//  this += NilDeserializerResolver
//}

private object NilTypeModifier extends CollectionLikeTypeModifier {
  def BASE = classOf[List[Any]]
}

//trait NilTypeModifierModule extends JacksonModule {
//  this += NilTypeModifier
//}

private trait CollectionLikeTypeModifier extends TypeModifier with GenTypeModifier {

  def BASE: Class[_]

  override def modifyType(originalType: JavaType, jdkType: Type, context: TypeBindings, typeFactory: TypeFactory) =
    if (originalType.containedTypeCount() > 1) originalType else
      classObjectFor(jdkType) find (BASE.isAssignableFrom(_)) map { cls =>
        val eltType = if (originalType.containedTypeCount() == 1) originalType.containedType(0) else UNKNOWN
        typeFactory.constructCollectionLikeType(cls, eltType)
      } getOrElse originalType

}

private trait GenTypeModifier {

  // Workaround for http://jira.codehaus.org/browse/JACKSON-638
  protected def UNKNOWN = SimpleType.construct(classOf[AnyRef])

  protected def classObjectFor(jdkType: Type) = jdkType match {
    case cls: Class[_] => Some(cls)
    case pt: ParameterizedType => pt.getRawType match {
      case cls: Class[_] => Some(cls)
      case _ => None
    }
    case _ => None
  }

}