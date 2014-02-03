package uk.gov.gds.ier.transaction.overseas.confirmation

import uk.gov.gds.ier.serialiser.WithSerialiser
import uk.gov.gds.ier.model.InprogressOverseas
import org.scalatest.{Matchers, FlatSpec}
import play.api.libs.json.{Json, JsNull}
import org.joda.time.DateTime
import uk.gov.gds.ier.test.TestHelpers
import uk.gov.gds.ier.validation.{ErrorMessages, FormKeys}

class ConfirmationFormTests
  extends FlatSpec
  with Matchers
  with ConfirmationForms
  with WithSerialiser
  with ErrorMessages
  with FormKeys
  with TestHelpers {

  val serialiser = jsonSerialiser

  it should "error out on empty json" in {
    val js = JsNull
    val errorMessage = Seq("Please complete this step")
    confirmationForm.bind(js).fold(
      hasErrors => {
        hasErrors.errorMessages("dateLeftUk") should be(errorMessage)
        hasErrors.errorMessages("firstTimeRegistered") should be(errorMessage)
        hasErrors.errorMessages("name") should be(errorMessage)
        hasErrors.errorMessages("lastUkAddress") should be(errorMessage)
        hasErrors.errorMessages("previouslyRegistered") should be(errorMessage)
        hasErrors.globalErrorMessages.count(_ == "Please complete this step") should be(5)
        hasErrors.errors.size should be(10)
      },
      success => fail("Should have errored out.")
    )
  }

  it should "error out on empty application" in {
    val application = InprogressOverseas()
    val errorMessage = Seq("Please complete this step")
    confirmationForm.fillAndValidate(application).fold(
      hasErrors => {
        hasErrors.errorMessages("dateLeftUk") should be(errorMessage)
        hasErrors.errorMessages("firstTimeRegistered") should be(errorMessage)
        hasErrors.errorMessages("name") should be(errorMessage)
        hasErrors.errorMessages("lastUkAddress") should be(errorMessage)
        hasErrors.errorMessages("previouslyRegistered") should be(errorMessage)
        hasErrors.globalErrorMessages.count(_ == "Please complete this step") should be(5)
        hasErrors.errors.size should be(10)
      },
      success => fail("Should have errored out.")
    )
  }

}
