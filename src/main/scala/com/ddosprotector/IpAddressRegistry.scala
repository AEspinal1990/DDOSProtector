package com.ddosprotector

import java.io.{BufferedWriter, File, FileWriter}

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.immutable

final case class IpAddress(address: String)
final case class IpAddresses(ipAddresses: immutable.Map[String, Int])

object IpAddressRegistry {

  sealed trait Command
  final case class UpdateIpRecords(address: IpAddress, threshold: Int, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetIpResponse(maybeIpAddress: Option[IpAddress])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(Map.empty)

  private def registry(ipAddresses: Map[String, Int]): Behavior[Command] =
    Behaviors.receiveMessage {
      case UpdateIpRecords(ipAddress, threshold, replyTo) =>
        val numberOfHits: Int = ipAddresses.getOrElse(ipAddress.address, 0) + 1
        val updatedRecord: immutable.Map[String, Int] = Map(ipAddress.address -> numberOfHits)

        // Only update blacklist when hits == threshold to avoid writing same ip address more than once
        if (violatedThreshold(numberOfHits, threshold)) writeToBlacklist("blacklist.txt", ipAddress.address)

        replyTo ! ActionPerformed(s"Endpoint hit from: ${ipAddress.address} $numberOfHits times.")
        registry(ipAddresses ++ updatedRecord)


    }

  def violatedThreshold(hits: Int, threshold: Int): Boolean= if (hits == threshold) true else false

  def writeToBlacklist(blacklist: String, ip: String): Unit = {
    val file = new File(blacklist)
    val bw = new BufferedWriter(new FileWriter(file, true))
    bw.append(s"$ip\n")
    bw.close()
  }
}