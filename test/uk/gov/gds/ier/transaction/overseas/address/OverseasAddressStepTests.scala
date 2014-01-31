package uk.gov.gds.ier.transaction.overseas.address

import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.mock.MockitoSugar
import play.api.test._
import play.api.test.Helpers._
import uk.gov.gds.ier.test.TestHelpers
import org.joda.time.DateTime

class OverseasAddressStepTests
  extends FlatSpec
  with Matchers
  with MockitoSugar
  with TestHelpers {

  behavior of "OverseasAddressStep.get"
  it should "display the page" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(GET, "/register-to-vote/overseas/address").withIerSession()
      )

      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("<a class=\"back-to-previous\" href=\"/register-to-vote/overseas/nino")
      contentAsString(result) should include("Where do you live?")
      contentAsString(result) should include("/register-to-vote/overseas/address")
    }
  }

  behavior of "OverseasAddressStep.post"
  it should "bind successfully and redirect to the Open Register step if all the fields are completed in this page" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/overseas/address")
          .withIerSession()
          .withFormUrlEncodedBody(
          "overseasAddress.country" -> "United Kingdom",
          "overseasAddress.overseasAddressDetails" -> "some street, some borough, postcode")
      )

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/register-to-vote/overseas/openRegister"))
    }
  }

  it should "display any errors on unsuccessful bind" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/overseas/address").withIerSession()
      )

      status(result) should be(OK)
      contentAsString(result) should include("Please enter the country")
      contentAsString(result) should include("Please enter the address")
      contentAsString(result) should include("/register-to-vote/overseas/address")
    }
  }

  behavior of "OverseasAddressStep.editGet"
  it should "display the edit page" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(GET, "/register-to-vote/overseas/edit/address").withIerSession()
      )

      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Where do you live?")
      contentAsString(result) should include("<a class=\"back-to-previous\" href=\"/register-to-vote/overseas/confirmation")
      contentAsString(result) should include("/register-to-vote/overseas/edit/address")
    }
  }

  behavior of "OverseasAddressStep.editPost"
  it should "bind successfully and redirect to the Open Register step if all the fiedls are entered" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/overseas/edit/address")
          .withIerSession()
          .withFormUrlEncodedBody(
          "overseasAddress.country" -> "United Kingdom",
          "overseasAddress.overseasAddressDetails" -> "some street, some borough, postcode")
      )

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/register-to-vote/overseas/openRegister"))
    }
  }

  it should "display any errors on unsuccessful bind" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/overseas/edit/address").withIerSession()
      )

      status(result) should be(OK)
      contentAsString(result) should include("Please enter the country")
      contentAsString(result) should include("Please enter the address")
      contentAsString(result) should include("/register-to-vote/overseas/edit/address")
    }
  }

  behavior of "OverseasAddressStep.post when complete application"
  it should "bind successfully and redirect to the confirmation step if all the fields are entered" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/overseas/address")
          .withIerSession()
          .withApplication(completeOverseasApplication)
          .withFormUrlEncodedBody(
          "overseasAddress.country" -> "United Kingdom",
          "overseasAddress.overseasAddressDetails" -> "some street, some borough, postcode")
      )

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/register-to-vote/overseas/confirmation"))
    }
  }

  behavior of "OverseasAddressStep.editPost when complete application"
  it should "bind successfully and redirect to the confirmation step if all the fields are entered" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/overseas/edit/address")
          .withIerSession()
          .withApplication(completeOverseasApplication)
          .withFormUrlEncodedBody(
          "overseasAddress.country" -> "United Kingdom",
          "overseasAddress.overseasAddressDetails" -> "some street, some borough, postcode")
      )

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/register-to-vote/overseas/confirmation"))
    }
  }
}
