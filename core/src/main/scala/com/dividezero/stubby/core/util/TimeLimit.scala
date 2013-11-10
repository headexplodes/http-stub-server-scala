package com.dividezero.stubby.core.util

object TimeLimit {

  type Action[T] = (Long) => Option[T]
  
  def time() = System.currentTimeMillis
  
  def retry[T](timeout: Long)(action: Action[T]): Option[T] = {
    val start = time()
    val result = action(timeout)
    val remaining = timeout - (time() - start)
    if (result.isEmpty && remaining > 0) {
      retry(remaining)(action)
    } else {
      result
    }
  }

}