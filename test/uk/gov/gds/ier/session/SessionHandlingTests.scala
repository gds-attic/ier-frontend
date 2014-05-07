package uk.gov.gds.ier.session

import org.scalatest.{Matchers, FlatSpec}
import uk.gov.gds.ier.serialiser.{WithSerialiser, JsonSerialiser}
import play.api.mvc.Controller
import uk.gov.gds.ier.client.ApiResults
import play.api.test._
import play.api.test.Helpers._
import org.joda.time.DateTime
import uk.gov.gds.ier.security._
import uk.gov.gds.ier.controller.MockConfig
import uk.gov.gds.ier.guice.{WithEncryption, WithConfig}
import scala.Some
import play.api.test.FakeApplication
import play.api.mvc.Cookie
import uk.gov.gds.ier.logging.Logging
import uk.gov.gds.ier.step.InprogressApplication


class SessionHandlingTests extends FlatSpec with Matchers {

  val jsonSerialiser = new JsonSerialiser

  case class FakeInprogress(foo:String) extends InprogressApplication[FakeInprogress] {
    def merge(other:FakeInprogress) = {
      this.copy(foo + other.foo)
    }
  }

  it should "successfully create a new session" in {
    running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
      class TestController
        extends SessionHandling[FakeInprogress]
          with Controller
          with WithSerialiser
          with WithConfig
          with Logging
          with ApiResults
          with WithEncryption {

        def factoryOfT() = FakeInprogress("")
        val serialiser = jsonSerialiser
        val config = new MockConfig
        val encryptionService = new EncryptionService (new Base64EncodingService, config)


        def index() = NewSession requiredFor {
          request =>
            okResult(Map("status" -> "Ok"))
        }
      }

      val controller = new TestController()
      val result = controller.index()(FakeRequest())

      status(result) should be(OK)
      jsonSerialiser.fromJson[Map[String,String]](contentAsString(result)) should be(Map("status" -> "Ok"))

      cookies(result).get("sessionKey") should not be None
      cookies(result).get("sessionKeyIV") should not be None

      val Some(cookie) = cookies(result).get("sessionKey")
      val Some(cookieIV) = cookies(result).get("sessionKeyIV")

      val decryptedInfo = controller.encryptionService.decrypt(cookie.value, cookieIV.value)

      val token = jsonSerialiser.fromJson[SessionToken](decryptedInfo)
      token.timestamp.getDayOfMonth should be(DateTime.now.getDayOfMonth)
      token.timestamp.getMonthOfYear should be(DateTime.now.getMonthOfYear)
      token.timestamp.getYear should be(DateTime.now.getYear)
      token.timestamp.getHourOfDay should be(DateTime.now.getHourOfDay)
      token.timestamp.getMinuteOfHour should be(DateTime.now.getMinuteOfHour)
      token.timestamp.getSecondOfMinute should be(DateTime.now.getSecondOfMinute +- 2)
    }
  }

  it should "force a redirect with no valid session" in {
    running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
      class TestController
        extends SessionHandling[FakeInprogress]
          with Controller
          with WithSerialiser
          with WithConfig
          with Logging
          with ApiResults
          with WithEncryption {

        def factoryOfT() = FakeInprogress("")
        val serialiser = jsonSerialiser
        val config = new MockConfig
        val encryptionService = new EncryptionService (new Base64EncodingService, config)

        def index() = ValidSession requiredFor {
          request => application =>
            okResult(Map("status" -> "Ok"))
        }
      }

      val controller = new TestController()
      val result = controller.index()(FakeRequest())

      status(result) should be(SEE_OTHER)
    }
  }

  it should "refresh a session with a new timestamp" in {
    running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
      class TestController
        extends SessionHandling[FakeInprogress]
          with Controller
          with WithSerialiser
          with WithConfig
          with Logging
          with ApiResults
          with WithEncryption {

        def factoryOfT() = FakeInprogress("")
        val serialiser = jsonSerialiser
        val config = new MockConfig
        val encryptionService = new EncryptionService (new Base64EncodingService, config)

        def index() = NewSession requiredFor {
          request =>
            okResult(Map("status" -> "Ok"))
        }
        def nextStep() = ValidSession requiredFor {
          request => application =>
            okResult(Map("status" -> "Ok"))
        }
      }

      val controller = new TestController()
      val result = controller.index()(FakeRequest())

      status(result) should be(OK)
      jsonSerialiser.fromJson[Map[String,String]](contentAsString(result)) should be(Map("status" -> "Ok"))

      cookies(result).get("sessionKey") should not be None
      cookies(result).get("sessionKeyIV") should not be None

      val Some(initialTokenHash) = cookies(result).get("sessionKey")
      val Some(initialTokenIV) = cookies(result).get("sessionKeyIV")

      val initialTokendecryptedInfo =
        controller.encryptionService.decrypt(initialTokenHash.value, initialTokenIV.value)
      val initialToken = jsonSerialiser.fromJson[SessionToken](initialTokendecryptedInfo)

      val nextResult = controller.nextStep()(FakeRequest().withCookies(initialTokenHash, initialTokenIV))
      status(nextResult) should be(OK)

      val Some(newToken) = cookies(nextResult).get("sessionKey")
      val Some(newTokenIV) = cookies(nextResult).get("sessionKeyIV")

      val newTokendecryptedInfo =
        controller.encryptionService.decrypt(newToken.value, newTokenIV.value)
      val nextToken = jsonSerialiser.fromJson[SessionToken](newTokendecryptedInfo)

      nextToken should not be initialToken
      nextToken.timestamp.getDayOfMonth should be(DateTime.now.getDayOfMonth)
      nextToken.timestamp.getMonthOfYear should be(DateTime.now.getMonthOfYear)
      nextToken.timestamp.getYear should be(DateTime.now.getYear)
      nextToken.timestamp.getHourOfDay should be(DateTime.now.getHourOfDay)
      nextToken.timestamp.getMinuteOfHour should be(DateTime.now.getMinuteOfHour)
      nextToken.timestamp.getSecondOfMinute should be(DateTime.now.getSecondOfMinute +- 1)
    }
  }

  it should "invalidate a session after 20 mins" in {
    running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
      class TestController
        extends SessionHandling[FakeInprogress]
          with Controller
          with WithSerialiser
          with WithConfig
          with Logging
          with ApiResults
          with WithEncryption {

        def factoryOfT() = FakeInprogress("")
        val serialiser = jsonSerialiser
        val config = new MockConfig
        val encryptionService = new EncryptionService (new Base64EncodingService, config)

        def index() = ValidSession requiredFor {
          request => application =>
            okResult(Map("status" -> "Ok"))
        }
      }

      val controller = new TestController()
      val sessionToken = SessionToken(DateTime.now.minusMinutes(20))
      val (encryptedSessionTokenValue, encryptedSessionTokenIVValue) =
        controller.encryptionService.encrypt(jsonSerialiser.toJson(sessionToken))

      val result = controller.index()(
        FakeRequest().withCookies(
          Cookie("sessionKey", encryptedSessionTokenValue),
          Cookie("sessionKeyIV", encryptedSessionTokenIVValue)
        )
      )

      status(result) should be(SEE_OTHER)

      cookies(result).get("sessionKey") should not be None
      cookies(result).get("sessionKeyIV") should not be None

      val Some(token) = cookies(result).get("sessionKey")
      val Some(tokenIV) = cookies(result).get("sessionKeyIV")

      val decryptedInfo = controller.encryptionService.decrypt(token.value, tokenIV.value)

      val tokenDate = jsonSerialiser.fromJson[SessionToken](decryptedInfo)
      tokenDate.timestamp.getDayOfMonth should be(DateTime.now.getDayOfMonth)
      tokenDate.timestamp.getMonthOfYear should be(DateTime.now.getMonthOfYear)
      tokenDate.timestamp.getYear should be(DateTime.now.getYear)
      tokenDate.timestamp.getHourOfDay should be(DateTime.now.getHourOfDay)
      tokenDate.timestamp.getMinuteOfHour should be(DateTime.now.getMinuteOfHour)
      tokenDate.timestamp.getSecondOfMinute should be(DateTime.now.getSecondOfMinute +- 2)
      tokenDate.timestamp.getMinuteOfHour should not be(DateTime.now.minusMinutes(20).getMinuteOfHour)
    }
  }

  it should "refresh a session before 20 mins" in {
    running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
      class TestController
        extends SessionHandling[FakeInprogress]
        with Controller
        with WithSerialiser
        with WithConfig
        with Logging
        with ApiResults
        with WithEncryption {

        def factoryOfT() = FakeInprogress("")
        val serialiser = jsonSerialiser
        val config = new MockConfig
        val encryptionService = new EncryptionService (new Base64EncodingService, config)


        def index() = ValidSession requiredFor {
          request => application =>
            okResult(Map("status" -> "Ok"))
        }
      }

      val controller = new TestController()
      val sessionToken = SessionToken(DateTime.now.minusMinutes(19))
      val (encryptedSessionTokenValue, encryptedSessionTokenIVValue) =
        controller.encryptionService.encrypt(jsonSerialiser.toJson(sessionToken))

      val result = controller.index()(
        FakeRequest().withCookies(
          Cookie("sessionKey", encryptedSessionTokenValue),
          Cookie("sessionKeyIV", encryptedSessionTokenIVValue)
        )
      )

      status(result) should be(OK)

      cookies(result).get("sessionKey") should not be None
      cookies(result).get("sessionKeyIV") should not be None

      val Some(token) = cookies(result).get("sessionKey")
      val Some(tokenIV) = cookies(result).get("sessionKeyIV")

      val decryptedInfo = controller.encryptionService.decrypt(token.value, tokenIV.value)

      val tokenDate = jsonSerialiser.fromJson[SessionToken](decryptedInfo)
      tokenDate.timestamp.getDayOfMonth should be(DateTime.now.getDayOfMonth)
      tokenDate.timestamp.getMonthOfYear should be(DateTime.now.getMonthOfYear)
      tokenDate.timestamp.getYear should be(DateTime.now.getYear)
      tokenDate.timestamp.getHourOfDay should be(DateTime.now.getHourOfDay)
      tokenDate.timestamp.getMinuteOfHour should be(DateTime.now.getMinuteOfHour)
      tokenDate.timestamp.getSecondOfMinute should be(DateTime.now.getSecondOfMinute +- 1)
    }
  }

  it should "store the old timestamp when refreshing" in {
    running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
      class TestController
        extends SessionHandling[FakeInprogress]
        with Controller
        with WithSerialiser
        with WithConfig
        with Logging
        with ApiResults
        with WithEncryption {

        def factoryOfT() = FakeInprogress("")
        val serialiser = jsonSerialiser
        val config = new MockConfig
        val encryptionService = new EncryptionService (new Base64EncodingService, config)


        def index() = ValidSession requiredFor {
          request => application =>
            okResult(Map("status" -> "Ok"))
        }
      }

      val controller = new TestController()
      val sessionToken = SessionToken(DateTime.now.minusMinutes(19))
      val (encryptedSessionTokenValue, encryptedSessionTokenIVValue) =
        controller.encryptionService.encrypt(jsonSerialiser.toJson(sessionToken))

      val result = controller.index()(
        FakeRequest().withCookies(
          Cookie("sessionKey", encryptedSessionTokenValue),
          Cookie("sessionKeyIV", encryptedSessionTokenIVValue)
        )
      )

      status(result) should be(OK)

      cookies(result).get("sessionKey") should not be None
      cookies(result).get("sessionKeyIV") should not be None

      val Some(token) = cookies(result).get("sessionKey")
      val Some(tokenIV) = cookies(result).get("sessionKeyIV")

      val decryptedInfo = controller.encryptionService.decrypt(token.value, tokenIV.value)

      val refreshedToken = jsonSerialiser.fromJson[SessionToken](decryptedInfo)
      refreshedToken.history.size should be(1)
      refreshedToken.history.map(_.getMillis) should contain(sessionToken.timestamp.getMillis)
    }
  }

  it should "clear session cookies by setting a maxage below 0" in {
    running(FakeApplication(additionalConfiguration = Map("application.secret" -> "test"))) {
      class TestController
        extends SessionCleaner
          with Controller
          with WithSerialiser
          with WithConfig
          with ApiResults
          with WithEncryption {

        val serialiser = jsonSerialiser
        val config = new MockConfig
        val encryptionService = new EncryptionService (new Base64EncodingService, config)

        def noSessionTest() = ClearSession requiredFor {
          request =>
            okResult(Map("status" -> "no session"))
        }
      }

      val controller = new TestController()
      val result = controller.noSessionTest()(FakeRequest().withCookies(
        Cookie("application", "barfoo")))

      status(result) should be(OK)
      jsonSerialiser.fromJson[Map[String,String]](contentAsString(result)) should be(Map("status" -> "no session"))

      cookies(result).get("sessionKey").isDefined should be(true)
      cookies(result).get("sessionKey").get.maxAge.get should be < 0
      cookies(result).get("application").isDefined should be(true)
      cookies(result).get("application").get.maxAge.get should be < 0
      cookies(result).get("application").get.value should be("")
    }
  }
}
