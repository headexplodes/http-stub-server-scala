package com.dz.stubby.core.util

import com.dz.stubby.core.model.StubMessage

object HttpMessageUtils {
  
  def bodyAsText(request: StubMessage): String = ""
  def isText(request: StubMessage): Boolean = false
    
}