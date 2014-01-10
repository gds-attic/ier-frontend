package uk.gov.gds.ier.step.confirmation

import uk.gov.gds.ier.validation.{FormMappings, ErrorTransformForm}
import play.api.data.Forms._
import uk.gov.gds.ier.serialiser.WithSerialiser
import uk.gov.gds.ier.model.InprogressOrdinary

trait ConfirmationForms extends FormMappings {
  self: WithSerialiser =>

  val confirmationForm = ErrorTransformForm(
    mapping(
      keys.name.key -> optional(nameMapping).verifying("Please complete this step", _.isDefined),
      keys.previousName.key -> optional(previousNameMapping).verifying("Please complete this step", _.isDefined),
      keys.dob.key -> optional(dobAndReasonMapping).verifying("Please complete this step", _.isDefined),
      keys.nationality.key -> optional(nationalityMapping).verifying("Please complete this step", _.isDefined),
      keys.nino.key -> optional(ninoMapping).verifying("Please complete this step", _.isDefined),
      keys.address.key -> optional(addressMapping).verifying("Please complete this step", _.isDefined),
      keys.previousAddress.key -> optional(previousAddressMapping).verifying("Please complete this step", _.isDefined),
      keys.otherAddress.key -> optional(otherAddressMapping).verifying("Please complete this step", _.isDefined),
      keys.openRegister.key -> optional(optInMapping).verifying("Please complete this step", _.isDefined),
      keys.postalVote.key -> optional(optInMapping).verifying("Please complete this step", _.isDefined),
      keys.contact.key -> optional(contactMapping).verifying("Please complete this step", _.isDefined),
      keys.possibleAddresses.key -> optional(possibleAddressMapping),
      keys.country.key -> optional(countryMapping)
    ) (InprogressOrdinary.apply) (InprogressOrdinary.unapply)
  )

}
