package uk.gov.gds.ier.transaction.forces.address

import org.scalatest.{Matchers, FlatSpec}
import uk.gov.gds.ier.validation.{FormKeys, ErrorMessages}
import uk.gov.gds.ier.test.{WithMockRemoteAssets, TestHelpers}
import uk.gov.gds.ier.serialiser.WithSerialiser
import uk.gov.gds.ier.model._
import uk.gov.gds.ier.transaction.forces.InprogressForces

class AddressLookupMustacheTest
  extends FlatSpec
  with Matchers
  with AddressForms
  with AddressLookupMustache
  with ErrorMessages
  with WithSerialiser
  with FormKeys
  with WithMockRemoteAssets
  with TestHelpers {


  val serialiser = jsonSerialiser

  it should "empty progress form should produce empty Model (lookupData)" in {

    val emptyApplicationForm =  addressForm
    val addressModel = mustache.data(
      emptyApplicationForm,
      Call("POST", "/register-to-vote/forces/address/lookup"),
      InprogressForces()
    ).asInstanceOf[LookupModel]

    addressModel.question.title should be("What was your last UK address?")
    addressModel.question.postUrl should be("/register-to-vote/forces/address/lookup")

    addressModel.postcode.value should be ("")
  }

  it should "have correct title for hasAddress = yes and living there" in {
    val partiallyFilledApplicationForm = addressForm.fill(InprogressForces(
      address = Some(LastAddress(
        hasAddress = Some(HasAddressOption.YesAndLivingThere),
        address = Some(PartialAddress(
          addressLine = Some("Fake street 123"),
          uprn = Some("1234567"),
          postcode = "WR26NJ",
          manualAddress = None
        ))
      ))
    ))

    val addressModel = mustache.data(
      partiallyFilledApplicationForm,
      Call("POST", "/register-to-vote/forces/address/lookup"),
      InprogressForces()
    ).asInstanceOf[LookupModel]

    addressModel.question.title should be("What is your UK address?")
    addressModel.question.postUrl should be("/register-to-vote/forces/address/lookup")

    addressModel.postcode.value should be ("WR26NJ")
  }

  it should "have correct title for hasAddress = yes and not living there" in {
    val partiallyFilledApplicationForm = addressForm.fill(InprogressForces(
      address = Some(LastAddress(
        hasAddress = Some(HasAddressOption.YesAndNotLivingThere),
        address = Some(PartialAddress(
          addressLine = Some("Fake street 123"),
          uprn = Some("1234567"),
          postcode = "WR26NJ",
          manualAddress = None
        ))
      ))
    ))

    val addressModel = mustache.data(
      partiallyFilledApplicationForm,
      Call("POST", "/register-to-vote/forces/address/lookup"),
      InprogressForces()
    ).asInstanceOf[LookupModel]

    addressModel.question.title should be("What is your UK address?")
    addressModel.question.postUrl should be("/register-to-vote/forces/address/lookup")

    addressModel.postcode.value should be ("WR26NJ")
  }

  it should "have correct title for hasAddress = no" in {
    val partiallyFilledApplicationForm = addressForm.fill(InprogressForces(
      address = Some(LastAddress(
        hasAddress = Some(HasAddressOption.No),
        address = Some(PartialAddress(
          addressLine = Some("Fake street 123"),
          uprn = Some("1234567"),
          postcode = "WR26NJ",
          manualAddress = None
        ))
      ))
    ))

    val addressModel = mustache.data(
      partiallyFilledApplicationForm,
      Call("POST", "/register-to-vote/forces/address/lookup"),
      InprogressForces()
    ).asInstanceOf[LookupModel]

    addressModel.question.title should be("What was your last UK address?")
    addressModel.question.postUrl should be("/register-to-vote/forces/address/lookup")

    addressModel.postcode.value should be ("WR26NJ")
  }

}
