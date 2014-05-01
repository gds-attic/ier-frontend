package uk.gov.gds.ier.transaction.crown.address

import org.scalatest.{Matchers, FlatSpec}
import uk.gov.gds.ier.validation.{FormKeys, ErrorMessages}
import uk.gov.gds.ier.test.TestHelpers
import uk.gov.gds.ier.serialiser.WithSerialiser
import uk.gov.gds.ier.model._
import scala.Some
import uk.gov.gds.ier.transaction.crown.InprogressCrown

class AddressFirstMustacheTest
  extends FlatSpec
  with Matchers
  with AddressFirstForms
  with AddressFirstMustache
  with ErrorMessages
  with FormKeys
  with TestHelpers {

  self: FormKeys
    with ErrorMessages
    with WithSerialiser =>

  val serialiser = jsonSerialiser

  it should "empty progress form should produce empty Model" in {
    val emptyApplicationForm = addressFirstForm

    val addressFirstModel = mustache.data(
      emptyApplicationForm,
      Call("GET","url-string-1"),
      InprogressCrown()
    ).asInstanceOf[AddressFirstModel]

    addressFirstModel.question.title should be("Do you have a UK address?")
    addressFirstModel.question.postUrl should be("url-string-1")

    addressFirstModel.hasAddressNo.attributes should be("")
    addressFirstModel.hasAddressYes.attributes should be("")

  }


  it should "progress form with valid values should produce Mustache Model with values present" in {
    val partiallyFilledApplicationForm = addressFirstForm.fill(InprogressCrown(
      address = Some(LastUkAddress(
        hasUkAddress = Some(true),
        address = None
      ))
    ))

    val addressFirstModel = mustache.data(
      partiallyFilledApplicationForm,
      Call("GET","url-string-1"),
      InprogressCrown()
    ).asInstanceOf[AddressFirstModel]

    addressFirstModel.question.title should be("Do you have a UK address?")
    addressFirstModel.question.postUrl should be("url-string-1")

    addressFirstModel.hasAddressNo.attributes should be("")
    addressFirstModel.hasAddressYes.attributes should be("checked=\"checked\"")

  }
}
