package uk.gov.gds.ier.validation.constraints

import uk.gov.gds.ier.validation.{FormKeys, ErrorMessages, NinoValidator}
import play.api.data.validation.{Valid, Invalid, Constraint}
import uk.gov.gds.ier.model.{PartialAddress, InprogressApplication, Nino}

trait AddressConstraints {
  self: ErrorMessages
    with FormKeys =>

  lazy val addressOrManualAddressDefined = Constraint[InprogressApplication](keys.address.key) {
    application =>
      application.address match {
        case Some(PartialAddress(Some(uprn), _, _)) if !uprn.isEmpty => Valid
        case Some(PartialAddress(_, _, Some(manualAddress))) if !manualAddress.isEmpty => Valid
        case _ => Invalid("Please select your address", keys.address.uprn)
      }
  }
}
