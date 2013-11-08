package uk.gov.gds.ier.step.address

import uk.gov.gds.ier.validation.{ErrorMessages, FormKeys}
import uk.gov.gds.ier.model.{InprogressApplication, Address, Addresses}
import uk.gov.gds.ier.validation.PostcodeValidator
import uk.gov.gds.ier.serialiser.WithSerialiser
import play.api.data.Form
import play.api.data.Forms._

trait AddressForms {
  self:  FormKeys
    with ErrorMessages
    with WithSerialiser =>

  val possibleAddressMapping = mapping(
    keys.jsonList.key -> nonEmptyText
  ) (serialiser.fromJson[Addresses]) (list => Some(serialiser.toJson(list)))

  val addressMapping = mapping(
    keys.address.key -> optional(nonEmptyText
      .verifying(addressMaxLengthError, _.size <= maxTextFieldLength)
    ).verifying("Please select your address", 
      address => address.exists(_ != "Select your address")),
    keys.postcode.key -> nonEmptyText
      .verifying("Your postcode is not valid", 
        postcode => PostcodeValidator.isValid(postcode))
  ) (
    Address.apply
  ) (
    Address.unapply
  )  
    
  val addressForm = Form(
    mapping(
      keys.address.key -> optional(addressMapping)
        .verifying("Please answer this question", _.isDefined),
      keys.possibleAddresses.key -> optional(possibleAddressMapping)
    ) (
      (address, possibleAddresses) => 
        InprogressApplication(address = address, possibleAddresses = possibleAddresses)
    ) (
      inprogress => Some(inprogress.address, inprogress.possibleAddresses)
    )
  )
}
