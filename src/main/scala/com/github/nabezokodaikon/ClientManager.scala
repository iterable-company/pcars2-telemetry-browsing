package com.github.nabezokodaikon

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import akka.pattern.{ AskTimeoutException, gracefulStop }
import com.github.nabezokodaikon.pcars2.{
  UdpData,
  RaceData,
  ParticipantsData,
  ParticipantVehicleNamesData,
  VehicleClassNamesData
}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.reflect.ClassTag
import scala.util.control.Exception.catching

object ClientManager {
  case class AddClient(client: ActorRef)
  case class RemoveClient(client: ActorRef)
}

class ClientManager extends Actor with LazyLogging {
  import ClientManager._

  val raceDataStorage = context.actorOf(
    Props(classOf[RaceDataStorage]), "RaceDataStorage"
  )
  val participantsDataStorage = context.actorOf(
    Props(classOf[ParticipantsDataStorage]), "ParticipantsDataStorage"
  )
  val participantVehicleNamesDataStorage = context.actorOf(
    Props(classOf[ParticipantVehicleNamesDataStorage]), "ParticipantVehicleNamesDataStorage"
  )
  val vehicleClassNamesDataStorage = context.actorOf(
    Props(classOf[VehicleClassNamesDataStorage]), "VehicleClassNamesDataStorage"
  )

  override def preStart() = {
    logger.debug("ClientManager preStart.");
  }

  override def postStop() = {
    context.children.foreach { child =>
      catching(classOf[AskTimeoutException]).either {
        val stopped = gracefulStop(child, 5.seconds, PoisonPill)
        Await.result(stopped, 6.seconds)
      } match {
        case Left(e) => logger.error(e.getMessage)
        case _ => Unit
      }
    }

    logger.debug("ClientManager postStop.")
  }

  def receive() = processing(List[ActorRef]())

  private def processing(clientList: List[ActorRef]): Receive = {
    case AddClient(client) =>
      raceDataStorage ! client
      participantsDataStorage ! client
      participantVehicleNamesDataStorage ! client
      vehicleClassNamesDataStorage ! client
      context.become(processing(clientList :+ client))
    case RemoveClient(client) =>
      context.become(processing(clientList.filter(_ != client).toList))
    case udpData: RaceData =>
      clientList.foreach(_ ! udpData.toJsonString)
      raceDataStorage ! udpData
    case udpData: ParticipantsData =>
      clientList.foreach(_ ! udpData.toJsonString)
      participantsDataStorage ! udpData
    case udpData: ParticipantVehicleNamesData =>
      clientList.foreach(_ ! udpData.toJsonString)
      participantVehicleNamesDataStorage ! udpData
    case udpData: VehicleClassNamesData =>
      clientList.foreach(_ ! udpData.toJsonString)
      vehicleClassNamesDataStorage ! udpData
    case udpData: UdpData =>
      clientList.foreach(_ ! udpData.toJsonString)
    case _ =>
      logger.warn("ClientManager received unknown message.")
  }
}

abstract class UdpDataStorage[T <: UdpData: ClassTag](name: String)
  extends Actor
  with LazyLogging {

  override def preStart() = {
    logger.debug(s"${name} preStart.");
  }

  override def postStop() = {
    logger.debug(s"${name} postStop.")
  }

  def receive: Receive = {
    case udpData: T =>
      context.become(processing(udpData))
    case client: ActorRef =>
      logger.debug(s"${name} storage data nothing.")
    case _ =>
      logger.warn(s"${name} received unknown message.")
  }

  private def processing(currentUdpData: T): Receive = {
    case nextUdpData: T =>
      context.become(processing(nextUdpData))
    case client: ActorRef =>
      val json = currentUdpData.toJsonString
      client ! json
    case _ =>
      logger.warn(s"${name} received unknown message.")
  }
}

final class RaceDataStorage
  extends UdpDataStorage[RaceData]("RaceDataStorage")
final class ParticipantsDataStorage
  extends UdpDataStorage[ParticipantsData]("ParticipantsDataStorage")
final class ParticipantVehicleNamesDataStorage
  extends UdpDataStorage[ParticipantVehicleNamesData]("ParticipantVehicleNamesDataStorage")
final class VehicleClassNamesDataStorage
  extends UdpDataStorage[VehicleClassNamesData]("VehicleClassNamesDataStorage")
