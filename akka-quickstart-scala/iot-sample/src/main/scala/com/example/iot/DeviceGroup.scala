package com.example.iot

import akka.actor
import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.actor.SupervisorStrategy.Stop
import com.example.iot.DeviceGroup.{ReplyDeviceList, RequestAllTemperatures, RequestDeviceList}
import com.example.iot.DeviceManager.RequestTrackDevice

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
  private var deviceIdToActor = Map.empty[String, ActorRef]  // TODO mutable state in actor
  var actorToDeviceId = Map.empty[ActorRef, String]

  override def preStart(): Unit = log.info("DeviceGroup {} started", groupId)

  override def postStop(): Unit = log.info("DeviceGroup {} stopped", groupId)

  override def receive: Receive = {
    // only include the @ if need access to the top level object, in this case the RequestTrackDevice object
    case trackMsg@RequestTrackDevice(`groupId`, deviceId) =>
      deviceIdToActor.get(deviceId) match {
        case Some(deviceActor) =>
          deviceActor.forward(trackMsg)
        case None =>
          val deviceActor = createDeviceActor(deviceId)
          deviceActor.forward(trackMsg)
      }

    case RequestTrackDevice(groupId, deviceId) =>
      log.warning(s"Rejecting TrackDevice request for groupId=$groupId. This actor is responsible for groupId=${this.groupId}")
      sender() ! DeviceManager.DeviceRegistrationRejected(groupId, deviceId, s"Rejecting TrackDevice request for groupId=$groupId. This actor is responsible for groupId=${this.groupId}")

    case rejectedMsg@DeviceManager.DeviceRegistrationRejected(groupId, deviceId, originalMessage) =>
      log.warning(s"TrackDevice request rejected unexpectedly from ${sender()} for $groupId-$deviceId: $originalMessage")
      deviceIdToActor.get(deviceId) match {
        case Some(deviceActor) if sender().eq(deviceActor) =>
          log.error(s"Stop and remove corrupt device actor: $deviceActor")
          context.stop(deviceActor)
          deviceIdToActor -= deviceId
//          val newDeviceActor = createDeviceActor(deviceId)
//          newDeviceActor ! DeviceManager.RequestTrackDevice(groupId, deviceId) // TODO can get into an infinite loop
        case None =>
      }

    case RequestDeviceList(requestId) =>
      sender() ! ReplyDeviceList(requestId, deviceIdToActor.keySet)

    case Terminated(deviceActor) =>
      val deviceId = actorToDeviceId(deviceActor)
      log.info("Device actor for {}-{} has been terminated. {}", this.groupId, deviceId, deviceActor)
      actorToDeviceId -= deviceActor
      deviceIdToActor -= deviceId

    case RequestAllTemperatures(requestId) =>
      log.info(s"RequestAllTemperatures $requestId")

  }

  private def createDeviceActor(deviceId: String): ActorRef = {
    log.info("Creating device actor for device-{}", deviceId)
    val deviceActor = context.actorOf(Device.props(groupId, deviceId), s"device-$deviceId")
    deviceIdToActor += deviceId -> deviceActor
    actorToDeviceId += deviceActor -> deviceId
    context.watch(deviceActor)
    deviceActor
  }
}
