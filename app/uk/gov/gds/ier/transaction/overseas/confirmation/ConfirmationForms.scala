package uk.gov.gds.ier.transaction.overseas.confirmation

import play.api.data.Forms._
import uk.gov.gds.ier.model.{InprogressOverseas, Stub}
import uk.gov.gds.ier.serialiser.WithSerialiser
import uk.gov.gds.ier.validation.{ErrorTransformForm, FormKeys, ErrorMessages}
import uk.gov.gds.ier.validation.constraints.CommonConstraints
import uk.gov.gds.ier.transaction.overseas.lastUkAddress.LastUkAddressForms
import uk.gov.gds.ier.transaction.overseas.previouslyRegistered.PreviouslyRegisteredForms
import uk.gov.gds.ier.transaction.overseas.dateLeftUk.DateLeftUkForms
import uk.gov.gds.ier.transaction.overseas.dateOfBirth.DateOfBirthForms
import uk.gov.gds.ier.transaction.overseas.lastRegisteredToVote.LastRegisteredToVoteForms
import uk.gov.gds.ier.transaction.overseas.nino.NinoForms
import uk.gov.gds.ier.transaction.overseas.name.NameForms
import uk.gov.gds.ier.transaction.overseas.openRegister.OpenRegisterForms

trait ConfirmationForms
  extends FormKeys
  with ErrorMessages
  with WithSerialiser
  with PreviouslyRegisteredForms
  with DateLeftUkForms
  with DateOfBirthForms
  with LastRegisteredToVoteForms
  with NinoForms
  with LastUkAddressForms
  with OpenRegisterForms
  with NameForms
  with CommonConstraints {

  val stubMapping = mapping(
    "foo" -> text
  ) (foo => Stub()) (stub => Some("foo"))

  val optInMapping = single(
    keys.optIn.key -> boolean
  )

  val confirmationForm = ErrorTransformForm(
    mapping(
      keys.name.key -> stepRequired(nameMapping),
      keys.previousName.key -> stepRequired(previousNameMapping),
      keys.previouslyRegistered.key -> stepRequired(previouslyRegisteredMapping),
      keys.dateLeftUk.key -> stepRequired(dateLeftUkMapping),
      "firstTimeRegistered" -> stepRequired(stubMapping),
      "lastRegisteredToVote" -> stepRequired(lastRegisteredToVoteMapping),
      keys.dob.key -> stepRequired(dobAndReasonMapping),
      keys.nino.key -> stepRequired(ninoMapping),
      keys.lastUkAddress.key -> stepRequired(partialAddressMapping),
      "address" -> stepRequired(stubMapping),
      keys.openRegister.key -> stepRequired(optInMapping),
      "waysToVote" -> stepRequired(stubMapping),
      keys.possibleAddresses.key -> optional(possibleAddressesMapping)
    ) (InprogressOverseas.apply) (InprogressOverseas.unapply)
  )
}
