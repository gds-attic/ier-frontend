package uk.gov.gds.ier.session

import uk.gov.gds.ier.serialiser.WithSerialiser
import play.api.mvc._
import uk.gov.gds.ier.validation.IerForms
import controllers.routes
import scala.Some
import org.joda.time.DateTime
import uk.gov.gds.ier.model.InprogressApplication
import uk.gov.gds.ier.guice.WithConfig

trait SessionHandling {
  self: WithSerialiser
    with Controller
    with WithConfig =>

  object ClearSession {
    final def eradicateSession[A](bodyParser: BodyParser[A], block:Request[A] => Result):Action[A] = Action(bodyParser) {
      implicit request =>
        block(request).emptySession()
    }

    final def withParser[A](bodyParser: BodyParser[A]) = new {
      def requiredFor(action: Request[A] => Result) = eradicateSession(bodyParser, action)
    }

    final def requiredFor(action: Request[AnyContent] => Result) = withParser(BodyParsers.parse.anyContent) requiredFor action
  }

  object NewSession {
    final def validateSession[A](bodyParser: BodyParser[A], block:Request[A] => Result):Action[A] = Action(bodyParser) {
      request =>
        block(request).withFreshSession()
    }

    final def withParser[A](bodyParser: BodyParser[A]) = new {
      def requiredFor(action: Request[A] => Result) = validateSession(bodyParser, action)
    }

    final def requiredFor(action: Request[AnyContent] => Result) = withParser(BodyParsers.parse.anyContent) requiredFor action

  }

  object ValidSession {

    final def validateSessionAndStore[A](bodyParser: BodyParser[A], block:Request[A] => InprogressApplication => (Result, InprogressApplication)):Action[A] = Action(bodyParser) {
      request =>
        request.getToken match {
          case Some(token) => isValidToken(token) match {
            case true => {
              val (result, application) = block(request)(request.getApplication)
              result.refreshSessionAndStore(application)
            }
            case false => Redirect(routes.RegisterToVoteController.index()).withFreshSession()
          }
          case None => Redirect(routes.RegisterToVoteController.index()).withFreshSession()
        }
    }

    final def validateSession[A](bodyParser: BodyParser[A], block:Request[A] => InprogressApplication => Result):Action[A] = Action(bodyParser) {
      request =>
        request.getToken match {
          case Some(token) => isValidToken(token) match {
            case true => {
              val result = block(request)(request.getApplication)
              result.refreshSession()
            }
            case false => Redirect(routes.RegisterToVoteController.index()).withFreshSession()
          }
          case None => Redirect(routes.RegisterToVoteController.index()).withFreshSession()
        }
    }

    final def withParser[A](bodyParser: BodyParser[A]) = new {
      def storeAfter(action: Request[A] => InprogressApplication => (Result, InprogressApplication)) = validateSessionAndStore(bodyParser, action)
      def requiredFor(action: Request[A] => InprogressApplication => Result) = validateSession(bodyParser, action)
    }

    final def storeAfter(action: Request[AnyContent] => InprogressApplication => (Result, InprogressApplication)) = withParser(BodyParsers.parse.anyContent) storeAfter action

    final def requiredFor(action: Request[AnyContent] => InprogressApplication => Result) = withParser(BodyParsers.parse.anyContent) requiredFor action

    protected def isValidToken(token:String) = {
      try {
        val dt = DateTime.parse(token)
        dt.isAfter(DateTime.now.minusMinutes(config.sessionTimeout))
      } catch {
        case e:IllegalArgumentException => false
      }
    }
  }

  private implicit class InProgressRequest(request:play.api.mvc.Request[_]) extends SessionKeys {
    def getToken = {
      request.cookies.get(sessionTokenKey).map(_.value)
    }
    def getApplication = {
      request.cookies.get(sessionPayloadKey) match {
        case Some(cookie) => serialiser.fromJson[InprogressApplication](cookie.value)
        case _ => InprogressApplication()
      }
    }
  }

  implicit class InProgressResult(result:Result) extends SessionKeys {
    def emptySession()(implicit request: play.api.mvc.Request[_]) = {
      val requestCookies = DiscardingCookie(sessionPayloadKey) ::
        DiscardingCookie(sessionTokenKey) :: Nil

      result.discardingCookies(requestCookies: _*)
    }
    def withFreshSession() = {
      result.withCookies(Cookie(sessionTokenKey, DateTime.now.toString())).discardingCookies(DiscardingCookie(sessionPayloadKey))
    }
    def refreshSessionAndStore(application:InprogressApplication) = {
      result.withCookies(Cookie(sessionTokenKey, DateTime.now.toString()), Cookie(sessionPayloadKey, serialiser.toJson(application)))
    }
    def refreshSession() = {
      result.withCookies(Cookie(sessionTokenKey, DateTime.now.toString()))
    }
  }

  trait SessionKeys {
    val sessionPayloadKey = "application"
    val sessionTokenKey = "sessionKey"
  }
}
