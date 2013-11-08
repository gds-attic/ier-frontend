package uk.gov.gds.ier.validation

import play.api.data.Forms._
import uk.gov.gds.ier.model._
import uk.gov.gds.ier.validation
import uk.gov.gds.ier.validation.DateValidator._
import uk.gov.gds.ier.serialiser.WithSerialiser
import play.api.data.validation.{Invalid, Valid, Constraint}
import play.api.data.Form
import uk.gov.gds.ier.step.nationality.NationalityForms
import uk.gov.gds.ier.step.name.NameForms
import uk.gov.gds.ier.step.previousName.PreviousNameForms
import uk.gov.gds.ier.step.dateOfBirth.DateOfBirthForms
import uk.gov.gds.ier.step.nino.NinoForms
import uk.gov.gds.ier.step.address.AddressForms

trait FormMappings 
  extends Constraints 
  with FormKeys 
  with ErrorMessages 
  with NinoForms
  with NationalityForms 
  with NameForms 
  with AddressForms
  with PreviousNameForms
  with DateOfBirthForms {
    self: WithSerialiser =>

  val contactMapping = mapping(
    keys.contactType.key -> text.verifying("Please select a contact method", method => List("phone", "post", "text", "email").contains(method)),
    keys.post.key -> optional(nonEmptyText.verifying(postMaxLengthError, _.size <= maxTextFieldLength)),
    keys.phone.key -> optional(nonEmptyText.verifying(postMaxLengthError, _.size <= maxTextFieldLength)),
    keys.textNum.key -> optional(nonEmptyText.verifying(postMaxLengthError, _.size <= maxTextFieldLength)),
    keys.email.key -> optional(nonEmptyText.verifying(postMaxLengthError, _.size <= maxTextFieldLength))
  ) (Contact.apply) (Contact.unapply) verifying(contactEmailConstraint, contactTelephoneConstraint, contactTextConstraint)

  val previousAddressMapping = mapping(
    keys.movedRecently.key -> boolean,
    keys.previousAddress.key -> optional(addressMapping)
  ) (PreviousAddress.apply) (PreviousAddress.unapply)
    .verifying("Please enter your postcode", p => (p.movedRecently && p.previousAddress.isDefined) || !p.movedRecently)
    .verifying("Please answer this question", p => p.movedRecently || (!p.movedRecently && !p.previousAddress.isDefined))

  val otherAddressMapping = mapping(
    keys.hasOtherAddress.key -> boolean
  ) (OtherAddress.apply) (OtherAddress.unapply)

  val optInMapping = single(
    keys.optIn.key -> boolean
  )
}
