package com.example.iot

import akka.actor.{Actor, ActorLogging, Props}
import com.example.iot.DeviceManager.DeviceRegistrationRejected

object Device {
  def props(groupId: String, deviceId: String): Props = Props(new Device(groupId, deviceId))

  final case class ReadTemperature(requestId: Long)

  final case class RespondTemperature(requestId: Long, value: Option[Double])

  final case class RecordTemperature(requestId: Long, value: Double)

  final case class TemperatureRecorded(requestId: Long)

}

class Device(groupId: String, deviceId: String) extends Actor with ActorLogging {

  import Device._

  private var lastTemperatureReading: Option[Double] = None

  override def preStart(): Unit = log.info("Device actor {}-{} started", groupId, deviceId)

  override def postStop(): Unit = log.info("Device actor {}-{} stopped", groupId, deviceId)

  override def receive: Receive = {
    case ReadTemperature(id) =>
      sender() ! RespondTemperature(id, lastTemperatureReading)
    case RecordTemperature(requestId, temperature) =>
      this.lastTemperatureReading = Some(temperature)
      sender() ! TemperatureRecorded(requestId)
    case DeviceManager.RequestTrackDevice(`groupId`, `deviceId`) =>
      log.info("Device actor {}-{} registered", groupId, deviceId)
      sender() ! DeviceManager.DeviceRegistered
    case DeviceManager.RequestTrackDevice(groupId, deviceId) =>
      log.warning("Ignoring TrackDevice request for {}-{}.  This actor is responsible for {}-{}.", groupId, deviceId, this.groupId, this.deviceId)
      sender() ! DeviceManager.DeviceRegistrationRejected(groupId, deviceId, s"Unable to register device. This actor is responsible for ${this.groupId}-${this.deviceId}")
  }
}