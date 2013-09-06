package com.dz.stubby.standalone

import com.dz.stubby.core.service.{JsonServiceInterface, StubService}
import java.io.File
import org.apache.commons.io.FileUtils
import com.dz.stubby.core.util.JsonUtils
import com.dz.stubby.core.model.StubExchange
import collection.JavaConversions._
import java.util


class FileSource(paths:Seq[File], service: StubService, jsonService: JsonServiceInterface) {

  def loadInitialFiles() {
    val jsonFiles:Seq[File] = paths.flatMap(FileUtils.listFiles(_, Array("json"), true).asInstanceOf[util.LinkedList[File]])
    jsonFiles.foreach((f:File) => loadFile(f))
  }

  def loadFile(file:File) {
    try {
      println("reading:"+file.getName)
      val json = FileUtils.readFileToString(file)
      val exchange:StubExchange = JsonUtils.deserialize[StubExchange](json).nilLists()
      //TODO: this disallows multiple matches, would be nice to allow fall through cases
      service.deleteResponse(exchange)
      service.addResponse(exchange)
    } catch {
      case e:Throwable => System.err.println("failed to load file: "+ name+" error\n\t"+e.getMessage)
    }
  }
}