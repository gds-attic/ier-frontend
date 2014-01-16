package uk.gov.gds.ier.transaction.ordinary.postalVote

import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.mock.MockitoSugar
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import play.api.test._
import play.api.test.Helpers._
import uk.gov.gds.ier.test.TestHelpers

class PostalVoteControllerTests
  extends FlatSpec
  with Matchers
  with MockitoSugar
  with TestHelpers {

  behavior of "PostalVoteController.get"
  it should "display the page" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(GET, "/register-to-vote/postal-vote").withIerSession()
      )

      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Question 10")
      contentAsString(result) should include("<a class=\"back-to-previous\" href=\"/register-to-vote/open-register")
      contentAsString(result) should include("Do you want to apply for a postal vote?")
      contentAsString(result) should include("/register-to-vote/postal-vote")
    }
  }

  behavior of "PostalVoteController.post"
  it should "bind successfully and redirect to the Open Register step" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/postal-vote")
          .withIerSession()
          .withFormUrlEncodedBody(
            "postalVote.optIn" -> "true"
          )
      )

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/register-to-vote/contact"))
    }
  }

  it should "bind successfully and redirect to the confirmation step when complete application" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/postal-vote")
          .withIerSession()
          .withApplication(completeOrdinaryApplication)
          .withFormUrlEncodedBody(
            "postalVote.optIn" -> "true"
          )
      )

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/register-to-vote/confirmation"))
    }
  }

  it should "display any errors on unsuccessful bind" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/postal-vote").withIerSession()
      )

      status(result) should be(OK)
      contentAsString(result) should include("Do you want to apply for a postal vote?")
      contentAsString(result) should include("Please answer this question")
      contentAsString(result) should include("/register-to-vote/postal-vote")
    }
  }

  behavior of "Completing a prior step when this question is incomplete"
  it should "stop on this page" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/country-of-residence")
          .withIerSession()
          .withApplication(completeOrdinaryApplication.copy(postalVoteOptin = None))
          .withFormUrlEncodedBody(
          "country.residence" -> "England"
        )
      )

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/register-to-vote/postal-vote"))
    }
  }
}
