package com.ddosprotector

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.ddosprotector.IpAddressRegistry._

import scala.concurrent.Future

class DDOSProtectorRoutes(ipAddressRegistry: ActorRef[IpAddressRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  private implicit val timeout: Timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))
  private implicit val threshold: Int = system.settings.config.getInt("threshold")

  def updateIpRecords(address: IpAddress, threshold: Int): Future[ActionPerformed] =
    ipAddressRegistry.ask(UpdateIpRecords(address, threshold, _))

  val ddosProtectorRoutes: Route =
    pathPrefix("") {
      concat(
        pathEnd {
          concat(
            get { extractClientIP { ip =>
              val hostname = ip.toOption.map(_.getHostAddress).getOrElse("unknown")
              IpAddresses
              onSuccess(updateIpRecords(IpAddress(hostname), threshold)) { performed =>
                complete((StatusCodes.Created, performed))
              }
            }}
          )
        })
    }
}
