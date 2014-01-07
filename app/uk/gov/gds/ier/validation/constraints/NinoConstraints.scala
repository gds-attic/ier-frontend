package uk.gov.gds.ier.validation.constraints

import uk.gov.gds.ier.validation.{FormKeys, ErrorMessages, NinoValidator}
import play.api.data.validation.{Valid, Invalid, Constraint}
import uk.gov.gds.ier.model.{InprogressApplication, Nino}

trait NinoConstraints {
  self: ErrorMessages
    with FormKeys =>

  lazy val ninoOrNoNinoReasonDefined = Constraint[InprogressApplication](keys.nino.key) {
    application =>
      if (application.nino.isDefined) {
        Valid
      }
      else {
        Invalid("Please enter your National Insurance number", keys.nino.nino)
      }
  }

  lazy val ninoIsValidIfProvided = Constraint[Nino](keys.nino.nino.key) {
    nino => 
      nino match {
        case Nino(Some(nino), _) if NinoValidator.isValid(nino) => Valid
        case Nino(Some(nino), _) if !NinoValidator.isValid(nino) => {
          Invalid("Your National Insurance number is not correct", keys.nino.nino)
        }
        case _ => Valid
      }
  }
}