package com.dz.stubby.standalone

import com.dz.stubby.core.service.{JsonServiceInterface, StubService}
import java.io.File
import org.apache.commons.io.FileUtils

import com.dz.stubby.core.util.JsonUtils
import com.dz.stubby.core.model.StubExchange
import collection.JavaConversions._
import java.util
import org.apache.commons.io.monitor.{FileAlterationListenerAdaptor, FileAlterationObserver, FileAlterationMonitor}


class FileSource(paths:Seq[File], service: StubService, jsonService: JsonServiceInterface) {
  val monitor = new FileAlterationMonitor(10L)

  def watchFolders() = {
    paths.foreach(path => monitor.addObserver(watchFolder(path)))
    monitor.start()
    this
  }

  def watchFolder(path:File) = {
    val monitor = new FileAlterationObserver(path)
    monitor.addListener(new FileAlterationListenerAdaptor(){
      override def onFileCreate(file:File) {
        loadFile(file)
      }

      override def onFileChange(file:File) {
        reloadFile(file)
      }

      override def onFileDelete(file:File) {
        deleteFile(file)
      }
    })
    monitor
  }

  def loadInitialFiles() = {
    val jsonFiles:Seq[File] = paths.flatMap(FileUtils.listFiles(_, Array("json"), true).asInstanceOf[util.LinkedList[File]])
    jsonFiles.foreach((f:File) => loadFile(f))
    this
  }

  def safeAction(file:File, action:(StubExchange) => Unit) {
    try {
      println("reading:"+file.getName)
      val json = FileUtils.readFileToString(file)
      val exchange:StubExchange = JsonUtils.deserialize[StubExchange](json).nilLists()
      //TODO: this disallows multiple matches, would be nice to allow fall through cases
      action(exchange)
    } catch {
      case e:Throwable => System.err.println("failed to load file: "+ file.getName+" error\n\t"+e.getMessage)
    }
  }

  def loadFile(file:File) = safeAction(file, (exchange)=> service.addResponse(exchange))

  def deleteFile(file:File) = safeAction(file, (exchange) => service.deleteResponse(exchange))

  def reloadFile(file:File) = safeAction(file, (exchange) => {
    service.deleteResponse(exchange)
    service.addResponse(exchange)
  })
}