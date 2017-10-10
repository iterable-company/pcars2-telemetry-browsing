package com.github.nabezokodaikon

import akka.actor.{ ActorSystem, Props }
import akka.pattern.{ AskTimeoutException, ask, gracefulStop }
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object ActorDone

object UsingActor {
  implicit val system = ActorSystem("pcars2-udp-app")
  implicit val materializer = ActorMaterializer()
}

object Main extends /* App with */ LazyLogging {
  import UsingActor._

  val clientManagerProps = Props(classOf[ClientManager])
  val clientManager = system.actorOf(clientManagerProps, "clientManager")

  val udpProps = Props(classOf[UdpListener], clientManager)
  val udpListener = system.actorOf(udpProps, "udpListener")

  val server = new Server(clientManager)
  server.startServer("192.168.1.18", 9000, system)

  try {
    val stopped = gracefulStop(udpListener, 5.seconds, ActorDone)
    Await.result(stopped, 6.seconds)
  } catch {
    case e: AskTimeoutException =>
      logger.error(e.getMessage)
  }

  try {
    val stopped = gracefulStop(clientManager, 5.seconds, ActorDone)
    Await.result(stopped, 6.seconds)
  } catch {
    case e: AskTimeoutException =>
      logger.error(e.getMessage)
  }

  system.terminate()
}
