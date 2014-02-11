package uk.gov.gds.ier.transaction.overseas.lastUkAddress

import play.api.data.Forms._
import play.api.data.validation.{Invalid, Valid, Constraint}
import uk.gov.gds.ier.validation.{
  ErrorTransformForm,
  ErrorMessages,
  FormKeys,
  PostcodeValidator}
import uk.gov.gds.ier.validation.constraints.CommonConstraints
import uk.gov.gds.ier.serialiser.WithSerialiser
import uk.gov.gds.ier.model.{
  InprogressOverseas,
  PartialAddress,
  PossibleAddress,
  Addresses}

trait LastUkAddressForms extends LastUkAddressConstraints {
  self: FormKeys
  with ErrorMessages
  with WithSerialiser =>

  lazy val partialAddressMapping = mapping(
    keys.addressLine.key -> optional(nonEmptyText),
    keys.uprn.key -> optional(nonEmptyText),
    keys.postcode.key -> nonEmptyText,
    keys.manualAddress.key -> optional(nonEmptyText)
  ) (
    PartialAddress.apply
  ) (
    PartialAddress.unapply
  ).verifying(manualAddressMaxLength, postcodeIsValid, uprnOrManualDefined)

  lazy val manualPartialAddressMapping = mapping(
    keys.postcode.key -> nonEmptyText,
    keys.manualAddress.key -> optional(nonEmptyText)
  ) (
    (postcode, manualAddress) => PartialAddress(
      addressLine = None,
      uprn = None,
      postcode = postcode,
      manualAddress = manualAddress
    )
  ) (
    partial => Some(
      partial.postcode,
      partial.manualAddress
    )
  ).verifying(manualAddressMaxLength, postcodeIsValid)

  lazy val postcodeLookupMapping = mapping(
    keys.postcode.key -> nonEmptyText
  ) (
    postcode => PartialAddress(
      addressLine = None,
      uprn = None,
      postcode = postcode,
      manualAddress = None
    )
  ) (
    partial => Some(partial.postcode)
  ).verifying(postcodeIsValid)

  lazy val possibleAddressesMapping = mapping(
    keys.jsonList.key -> nonEmptyText,
    keys.postcode.key -> nonEmptyText
  ) (
    (json, postcode) => PossibleAddress(
      serialiser.fromJson[Addresses](json),
      postcode
    )
  ) (
    possibleAddress => Some(
      serialiser.toJson(possibleAddress.jsonList),
      possibleAddress.postcode
    )
  )

  val lookupAddressForm = ErrorTransformForm(
    mapping (
      keys.lastUkAddress.key -> optional(postcodeLookupMapping)
    ) (
      lastUkAddr => InprogressOverseas(
        lastUkAddress = lastUkAddr
      )
    ) (
      inprogress => Some(inprogress.lastUkAddress)
    ).verifying( postcodeIsNotEmpty )
  )

  val lastUkAddressForm = ErrorTransformForm(
    mapping (
      keys.lastUkAddress.key -> optional(partialAddressMapping),
      keys.possibleAddresses.key -> optional(possibleAddressesMapping)
    ) (
      (lastUkAddr, possibleAddr) => InprogressOverseas(
        lastUkAddress = lastUkAddr,
        possibleAddresses = possibleAddr
      )
    ) (
      inprogress => Some(
        inprogress.lastUkAddress,
        inprogress.possibleAddresses
      )
    ).verifying( addressIsRequired )
  )

  val manualAddressForm = ErrorTransformForm(
    mapping(
      keys.lastUkAddress.key -> optional(manualPartialAddressMapping)
    ) (
      lastUkAddr => InprogressOverseas(lastUkAddress = lastUkAddr)
    ) (
      inprogress => Some(inprogress.lastUkAddress)
    ).verifying( manualAddressIsRequired )
  )
}


trait LastUkAddressConstraints extends CommonConstraints {
  self: FormKeys
   with ErrorMessages =>

  lazy val manualAddressIsRequired = Constraint[InprogressOverseas](keys.lastUkAddress.key) {
    inprogress =>
      inprogress.lastUkAddress match {
        case Some(partialAddress) if partialAddress.manualAddress.isDefined => Valid
        case _ => Invalid("Please answer this question", keys.lastUkAddress.manualAddress)
      }
  }

  lazy val postcodeIsNotEmpty = Constraint[InprogressOverseas](keys.lastUkAddress.key) {
    inprogress =>
      inprogress.lastUkAddress match {
        case Some(partialAddress) if partialAddress.postcode == "" => {
          Invalid("Please enter your postcode", keys.lastUkAddress.postcode)
        }
        case None => Invalid("Please enter your postcode", keys.lastUkAddress.postcode)
        case _ => Valid
      }
  }

  lazy val addressIsRequired = Constraint[InprogressOverseas](keys.lastUkAddress.key) {
    inprogress =>
      if (inprogress.lastUkAddress.isDefined) Valid
      else Invalid("Please answer this question", keys.lastUkAddress)
  }

  lazy val uprnOrManualDefined = Constraint[PartialAddress](keys.lastUkAddress.key) {
    case partialAddress if partialAddress.uprn.exists(_ != "") => Valid
    case partialAddress if partialAddress.manualAddress.exists(_ != "") => Valid
    case _ => Invalid(
      "Please select your address",
      keys.lastUkAddress.uprn,
      keys.lastUkAddress.manualAddress,
      keys.lastUkAddress
    )
  }

  lazy val manualAddressMaxLength = Constraint[PartialAddress](keys.lastUkAddress.key) {
    case PartialAddress(_, _, _, Some(manualAddress))
      if manualAddress.size <= maxExplanationFieldLength => Valid
    case PartialAddress(_, _, _, None) => Valid
    case _ => Invalid(addressMaxLengthError, keys.lastUkAddress.manualAddress)
  }

  lazy val postcodeIsValid = Constraint[PartialAddress](keys.lastUkAddress.key) {
    case PartialAddress(_, _, postcode, _)
      if PostcodeValidator.isValid(postcode) => Valid
    case _ => Invalid("Your postcode is not valid", keys.lastUkAddress.postcode)
  }
}