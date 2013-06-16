package com.dz.stubby.core.util

import java.util.ArrayList
import scala.collection.JavaConversions
import java.util.HashMap

object DeepCopyUtils { // hacks to convert between mutable and immutable types (for JavaScript engine)

  type JavaMap = java.util.Map[Object, Object]
  type JavaList = java.util.List[Object]

  type AnyMap = Map[AnyRef, AnyRef]
  type AnySeq = List[AnyRef]

  import JavaConversions._

  def toJava(src: AnyRef): Object = src match {
    case s: AnySeq => toJavaList(s)
    case m: AnyMap => toJavaMap(m)
    case _ => src
  }

  def toJavaList(src: AnySeq): JavaList =
    new ArrayList(seqAsJavaList(src.map(x => toJava(x))))
  
  def toJavaMap(src: AnyMap): JavaMap =
    new HashMap(mapAsJavaMap(src.map { case (k, v) => (toJava(k), toJava(v)) }))

  def toScala(src: Object): AnyRef = src match {
    case l: JavaList => toScalaSeq(l)
    case m: JavaMap => toScalaMap(m)
    case _ => src
  }
  
  def toScalaSeq(src: JavaList): AnySeq =
    collectionAsScalaIterable(src).map(x => toScala(x)).toList
  
  def toScalaMap(src: JavaMap): AnyMap =
    mapAsScalaMap(src).toMap.map { case (k, v) => (toScala(k), toScala(v)) }
  
}