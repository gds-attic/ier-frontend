package uk.gov.gds.ier.transaction.crown.contactAddress

import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.mock.MockitoSugar
import play.api.test._
import play.api.test.Helpers._
import uk.gov.gds.ier.test.TestHelpers

class ContactAddressControllerTests
  extends FlatSpec
  with Matchers
  with MockitoSugar
  with TestHelpers {

  behavior of "ContactAddressController.get"
  it should "display the page" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(GET, "/register-to-vote/crown/contact-address").withIerSession()
      )

      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Question 8")
      contentAsString(result) should include("Where should we write to you about your registration?")
      contentAsString(result) should include("/register-to-vote/crown/contact-address")
    }
  }

  behavior of "ContactAddressController.post"
  it should "bind successfully and redirect to the next step" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/crown/contact-address")
          .withIerSession()
          .withFormUrlEncodedBody(
            "contactAddress.contactAddressType" -> "uk"
          )
      )

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/register-to-vote/crown/open-register"))
    }
  }

  it should "bind successfully and redirect to the confirmation step when all complete" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/crown/contact-address")
          .withIerSession()
          .withApplication(completeCrownApplication)
          .withFormUrlEncodedBody(
          "contactAddress.contactAddressType" -> "uk"
        )
      )

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/register-to-vote/crown/confirmation"))
    }
  }

  it should "display any errors on unsuccessful bind" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/crown/contact-address").withIerSession()
      )

      status(result) should be(OK)
      contentAsString(result) should include("Where should we write to you about your registration?")
      contentAsString(result) should include("Please answer this question")
      contentAsString(result) should include("/register-to-vote/crown/contact-address")
    }
  }

//  behavior of "Completing a prior step when this question is incomplete"
//  it should "stop on this page" in {
//    running(FakeApplication()) {
//      val Some(result) = route(
//        FakeRequest(POST, "/register-to-vote/crown/nino")
//          .withIerSession()
//          .withApplication(completeCrownApplication.copy(contactAddress = None))
//          .withFormUrlEncodedBody(
//          "NINO.NINO" -> "AB 12 34 56 D"
//        )
//      )
//
//      status(result) should be(SEE_OTHER)
//      redirectLocation(result) should be(Some("/register-to-vote/crown/contact-address"))
//    }
//  }

  behavior of "ContactAddressController.editGet"
  it should "display the page" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(GET, "/register-to-vote/crown/edit/contact-address").withIerSession()
      )

      status(result) should be(OK)
      contentType(result) should be(Some("text/html"))
      contentAsString(result) should include("Question 8")
      contentAsString(result) should include("Where should we write to you about your registration?")
      contentAsString(result) should include("/register-to-vote/crown/edit/contact-address")    }
  }

  behavior of "ContactAddressController.editPost"
  it should "bind successfully and redirect to the next step" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/crown/edit/contact-address")
          .withIerSession()
          .withFormUrlEncodedBody(
          "contactAddress.contactAddressType" -> "uk"
          )
        )

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/register-to-vote/crown/open-register"))
    }
  }

  it should "bind successfully and redirect to the confirmation step when all complete" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/crown/edit/contact-address")
          .withIerSession()
          .withApplication(completeCrownApplication)
          .withFormUrlEncodedBody(
          "contactAddress.contactAddressType" -> "uk"
        )
      )

      status(result) should be(SEE_OTHER)
      redirectLocation(result) should be(Some("/register-to-vote/crown/confirmation"))
    }
  }

  it should "display any errors on unsuccessful bind" in {
    running(FakeApplication()) {
      val Some(result) = route(
        FakeRequest(POST, "/register-to-vote/crown/edit/contact-address").withIerSession()
      )

      status(result) should be(OK)
      contentAsString(result) should include("Where should we write to you about your registration?")
      contentAsString(result) should include("Please answer this question")
      contentAsString(result) should include("/register-to-vote/crown/edit/contact-address")
    }
  }
}
