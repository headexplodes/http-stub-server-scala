package com.dz.stubby.core.util

import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.ObjectOutputStream
import java.io.ObjectInputStream

object DeepCopyUtils {

  def deepCopy(src: AnyRef): AnyRef = { // TODO: Actually need to copy Jackson JSON graph to Java objects
    try {
      val outStream = new ByteArrayOutputStream()
      new ObjectOutputStream(outStream).writeObject(src)
      val inStream = new ByteArrayInputStream(outStream.toByteArray)
      return new ObjectInputStream(inStream).readObject
    } catch {
      case e: Exception => throw new RuntimeException("Error performing deep copy", e);
    }
  }
  
}