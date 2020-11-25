package com.ddosprotector

import com.ddosprotector.IpAddressRegistry.ActionPerformed
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object JsonFormats  {
  import DefaultJsonProtocol._

  implicit val ipJsonFormat: RootJsonFormat[IpAddress] = jsonFormat1(IpAddress)
  implicit val ipsJsonFormat: RootJsonFormat[IpAddresses] = jsonFormat1(IpAddresses)

  implicit val actionPerformedJsonFormat: RootJsonFormat[ActionPerformed] = jsonFormat1(ActionPerformed)
}
