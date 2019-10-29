package com.example.iot

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.{Duration, MILLISECONDS}

class DeviceSpec() extends TestKit(ActorSystem("DeviceSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  private val deviceId = "myDevice1"
  private val groupId = "myGroup1"

  "temperature management" should {
    "return default temperature before any has been recorded" in {
      val deviceActor = system.actorOf(Device.props(groupId, deviceId))
      val requestId: Long = 1
      deviceActor ! Device.ReadTemperature(requestId)
      expectMsg(Device.RespondTemperature(requestId, None))
    }

    "ack after recording temperature" in {
      val deviceActor = system.actorOf(Device.props(groupId, deviceId))
      val requestId: Long = 1
      deviceActor ! Device.RecordTemperature(requestId, 78.0)
      expectMsg(Device.TemperatureRecorded(requestId))
    }

    "return latest temperature after recording" in {
      val deviceActor = system.actorOf(Device.props(groupId, deviceId))
      var requestId: Long = 1
      deviceActor ! Device.RecordTemperature(requestId, 78.0)
      requestId += 1
      deviceActor ! Device.RecordTemperature(requestId, 89.5)
      requestId += 1
      deviceActor ! Device.ReadTemperature(requestId)

      expectMsgAllOf(
        Device.TemperatureRecorded(1),
        Device.TemperatureRecorded(2),
        Device.RespondTemperature(3, Some(89.5))
      )
    }
  }

  "device registration" should {
    "ack success with matching groupId and deviceId" in {
      val deviceActor = system.actorOf(Device.props(groupId, deviceId))
      deviceActor ! DeviceManager.RequestTrackDevice(groupId, deviceId)
      expectMsg(DeviceManager.DeviceRegistered)
    }

    "reject if groupId does not match" in {
      val groupId2 = groupId + "other"
      val deviceActor = system.actorOf(Device.props(groupId, deviceId))
      deviceActor ! DeviceManager.RequestTrackDevice(groupId2, deviceId)
      val response = expectMsgType[DeviceManager.DeviceRegistrationRejected]

      response.groupId should be(groupId2)
      response.deviceId should be(deviceId)
      response.message should not be empty
    }

    "reject if deviceId does not match" in {
      val deviceId2 = deviceId + "other"
      val deviceActor = system.actorOf(Device.props(groupId, deviceId))
      deviceActor ! DeviceManager.RequestTrackDevice(groupId, deviceId2)

      val response = expectMsgType[DeviceManager.DeviceRegistrationRejected]

      response.groupId should be(groupId)
      response.deviceId should be(deviceId2)
      response.message should not be empty
    }
  }

  "lifecycle management" should {
    "stop when PoisonPill received" in {
      val deviceActor = system.actorOf(Device.props(groupId, deviceId))
      watch(deviceActor)
      deviceActor ! PoisonPill
      expectTerminated(deviceActor, max = Duration(500, MILLISECONDS))
    }
  }
}