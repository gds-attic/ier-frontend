package uk.gov.gds.ier.transaction.forces.address

import org.scalatest.{Matchers, FlatSpec}
import uk.gov.gds.ier.test.TestHelpers
import uk.gov.gds.ier.validation.{ErrorMessages, FormKeys}
import uk.gov.gds.ier.serialiser.WithSerialiser
import play.api.libs.json.{JsNull, Json}

class AddressFirstFormTests
  extends FlatSpec
  with Matchers
  with AddressForms
  with AddressFirstForms
  with WithSerialiser
  with ErrorMessages
  with FormKeys
  with TestHelpers {

  val serialiser = jsonSerialiser

  it should "error out on empty input" in {
    val js = JsNull
    addressFirstForm.bind(js).fold(
      hasErrors => {
        hasErrors.errorMessages("address.hasUkAddress") should be(Seq("Please answer this question"))
        hasErrors.globalErrorMessages should be(Seq("Please answer this question"))
        hasErrors.errors.size should be(2)
      },
      success => fail("Should have errored out.")
    )
  }

  it should "error out on missing values in input" in {
    val js = Json.toJson(
      Map(
        "address.hasUkAddress" -> ""
      )
    )
    addressFirstForm.bind(js).fold(
      hasErrors => {
        hasErrors.errorMessages("address.hasUkAddress") should be(Seq("Please answer this question"))
        hasErrors.globalErrorMessages should be(Seq("Please answer this question"))
        hasErrors.errors.size should be(2)
      },
      success => fail("Should have errored out.")
    )
  }

  it should "successfully bind when user has previous address" in {
    val js = Json.toJson(
      Map(
        "address.hasUkAddress" -> "true"
      )
    )
    addressFirstForm.bind(js).fold(
      hasErrors => {
        fail("Binding failed with " + hasErrors.errorsAsTextAll)
      },
      success => {
        success.address.flatMap(_.hasUkAddress) should be(Some(true))
      }
    )
  }

  it should "successfully bind when user does not has previous address" in {
    val js = Json.toJson(
      Map(
        "address.hasUkAddress" -> "false"
      )
    )
    addressFirstForm.bind(js).fold(
      hasErrors => {
        fail("Binding failed with " + hasErrors.errorsAsTextAll)
      },
      success => {
        success.address.flatMap(_.hasUkAddress) should be(Some(false))
      }
    )
  }
}