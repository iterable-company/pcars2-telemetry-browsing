package com.github.nabezokodaikon

import com.github.nabezokodaikon.util.FileUtil
import com.typesafe.scalalogging.LazyLogging
import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity }
import akka.http.scaladsl.server.{ HttpApp, Route }

object WebServer extends HttpApp {

  val contentDirectory = {
    val current = FileUtil.getCurrentDirectory
    s"$current/contents"
  }

  override def routes: Route = {
    get {
      pathSingleSlash {
        val file = s"${contentDirectory}/index.html"
        val contentType = FileUtil.getContentType(file)
        val text = FileUtil.read(file)
        complete(HttpEntity(contentType, text))
      } ~
        path(Segments) { x: List[String] =>
          val segments = x.mkString("/")
          val file = s"${contentDirectory}/${segments}"
          val contentType = FileUtil.getContentType(file)
          val text = FileUtil.read(file)
          complete(HttpEntity(contentType, text))
        }
    }
  }
}

object HttpExample extends App with LazyLogging {

  def helloWorld(name: String): String = {
    "Hello " + name + "!"
  }

  logger.info(helloWorld("Http"))

  val system = ActorSystem("webserver")
  WebServer.startServer("192.168.1.18", 9000, system)
  system.terminate
}
