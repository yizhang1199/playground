package com.example.iot

import akka.actor
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.example.iot.DeviceManager.RequestTrackDevice

import scala.concurrent.duration.{Duration, SECONDS}

object DeviceGroup {
  def props(groupId: String): Props = actor.Props(new DeviceGroup(groupId))

  final case class RequestDeviceList(requestId: Long)

  final case class ReplyDeviceList(requestId: Long, ids: Set[String])

  final case class RequestAllTemperatures(requestId: Long)

  final case class RespondAllTemperatures(requestId: Long, temperatures: Map[String, TemperatureReading])

  sealed trait TemperatureReading

  final case class Temperature(value: Double) extends TemperatureReading

  case object TemperatureNotAvailable extends TemperatureReading

  case object DeviceNotAvailable extends TemperatureReading

  case object DeviceTimedOut extends TemperatureReading

}

class DeviceGroup(groupId: String) extends Actor with ActorLogging {
  import DeviceGroup._

  override def preStart(): Unit = log.info("DeviceGroup {} started", groupId)

  override def postStop(): Unit = log.info("DeviceGroup {} stopped", groupId)

  override def receive: Receive = {
    receiveWithDeviceActors(Map.empty[String, ActorRef], Map.empty[ActorRef, String])
  }

  private def receiveWithDeviceActors(deviceIdToActor: Map[String, ActorRef], actorToDeviceId: Map[ActorRef, String]): Receive = {
    // only include the @ if need access to the top level object, in this case the RequestTrackDevice object
    case trackMsg@RequestTrackDevice(`groupId`, deviceId) =>
      deviceIdToActor.get(deviceId) match {
        case Some(deviceActor) =>
          deviceActor.forward(trackMsg)
        case None =>
          val deviceActor = context.actorOf(Device.props(groupId, deviceId), s"device-$deviceId")
          context.watch(deviceActor)
          val newDeviceIdToActor = deviceIdToActor + (deviceId -> deviceActor)
          val newActorToDeviceId = actorToDeviceId + (deviceActor -> deviceId)
          deviceActor.forward(trackMsg)
          context.become(receiveWithDeviceActors(newDeviceIdToActor, newActorToDeviceId))
      }

    case RequestTrackDevice(groupId, deviceId) =>
      log.warning(s"Rejecting TrackDevice request for groupId=$groupId. This actor is responsible for groupId=${this.groupId}")
      sender() ! DeviceManager.DeviceRegistrationRejected(groupId, deviceId, s"Rejecting TrackDevice request for groupId=$groupId. This actor is responsible for groupId=${this.groupId}")

    case RequestDeviceList(requestId) =>
      sender() ! ReplyDeviceList(requestId, deviceIdToActor.keySet)

    case Terminated(deviceActor) =>
      val deviceId = actorToDeviceId(deviceActor)
      log.info("Device actor for {}-{} has been terminated. {}", this.groupId, deviceId, deviceActor)
      val newActorToDeviceId = actorToDeviceId - deviceActor
      val newDeviceIdToActor = deviceIdToActor - deviceId
      context.become(receiveWithDeviceActors(newDeviceIdToActor, newActorToDeviceId))

    case RequestAllTemperatures(requestId) =>
      context.actorOf(
        DeviceGroupQuery
          .props(actorToDeviceId = actorToDeviceId, requestId = requestId, requester = sender(), Duration(3, SECONDS)))
  }
}
