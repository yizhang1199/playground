package com.example.iot

import akka.actor.{ActorSystem, PoisonPill}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.{Duration, MILLISECONDS}

class DeviceGroupSpec() extends TestKit(ActorSystem("DeviceGroupSpec"))
  with ImplicitSender
  with WordSpecLike
  with Matchers
  with BeforeAndAfterAll {

  private val groupId = "teaHouse"

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Device Registration" should {
    "create new device for new deviceId" in {
      val actor = system.actorOf(DeviceGroup.props(groupId))
      val deviceId = "AC"
      actor ! DeviceManager.RequestTrackDevice(groupId, deviceId)
      expectMsg(DeviceManager.DeviceRegistered)
    }

    "return same device for same deviceId" in {
      val probe = TestProbe()
      val groupActor = system.actorOf(DeviceGroup.props(groupId))
      val deviceId = "heater"
      groupActor.tell(DeviceManager.RequestTrackDevice(groupId, deviceId), probe.ref)
      probe.expectMsg(DeviceManager.DeviceRegistered)

      groupActor ! DeviceGroup.RequestDeviceList(123)
      expectMsg(DeviceGroup.ReplyDeviceList(123, Set(deviceId)))
      val deviceActor1 = probe.lastSender

      groupActor.tell(DeviceManager.RequestTrackDevice(groupId, deviceId), probe.ref)
      probe.expectMsg(DeviceManager.DeviceRegistered)
      val deviceActor2 = probe.lastSender

      deviceActor1 should be theSameInstanceAs deviceActor2 // call left eq right
    }

    "reject requests with wrong groupId" in {
      val groupActor = system.actorOf(DeviceGroup.props(groupId))
      val groupId2 = "teaHouse2"
      val deviceId = "ignored"

      groupActor ! DeviceManager.RequestTrackDevice(groupId2, deviceId)

      val response = expectMsgType[DeviceManager.DeviceRegistrationRejected]

      response.groupId should be (groupId2)
      response.deviceId should be (deviceId)
      response.message should not be empty
    }
  }

  "Device Management" should {
    "list devices when empty" in {
      val groupActor = system.actorOf(DeviceGroup.props(groupId))
      groupActor ! DeviceGroup.RequestDeviceList(123)
      expectMsg(DeviceGroup.ReplyDeviceList(123, Set()))
    }

    "list active devices" in {
      val groupActor = system.actorOf(DeviceGroup.props(groupId))
      val deviceId1 = "heater"
      groupActor ! DeviceManager.RequestTrackDevice(groupId, deviceId1)
      val deviceId2 = "AC"
      groupActor ! DeviceManager.RequestTrackDevice(groupId, deviceId2)
      groupActor ! DeviceGroup.RequestDeviceList(123)
      expectMsgAllOf(
        DeviceManager.DeviceRegistered,
        DeviceManager.DeviceRegistered,
        DeviceGroup.ReplyDeviceList(123, Set(deviceId1, deviceId2))
      )
    }

    "remove stopped devices" in {
      val groupActor = system.actorOf(DeviceGroup.props(groupId))
      val deviceId1 = "willDieSoon"
      groupActor ! DeviceManager.RequestTrackDevice(groupId, deviceId1)
      expectMsg(DeviceManager.DeviceRegistered)
      val toDieActor = lastSender

      val deviceId2 = "alarm"
      groupActor ! DeviceManager.RequestTrackDevice(groupId, deviceId2)
      expectMsg(DeviceManager.DeviceRegistered)

      watch(toDieActor)
      println("toDieActor=" + toDieActor)
      toDieActor ! PoisonPill
      expectTerminated(toDieActor, Duration(300, MILLISECONDS))

      awaitAssert(a = {
        groupActor ! DeviceGroup.RequestDeviceList(123)
        expectMsg(DeviceGroup.ReplyDeviceList(123, Set(deviceId2)))
      }, max = Duration(500, MILLISECONDS))
    }
  }
}
