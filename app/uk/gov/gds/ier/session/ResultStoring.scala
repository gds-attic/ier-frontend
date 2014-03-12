package uk.gov.gds.ier.session

import play.api.mvc.Result
import uk.gov.gds.ier.model.InprogressApplication
import org.joda.time.DateTime
import uk.gov.gds.ier.guice.{WithEncryption, WithConfig}
import uk.gov.gds.ier.serialiser.WithSerialiser

trait ResultStoring extends ResultHandling {
  self: WithConfig
    with WithEncryption
    with WithSerialiser =>

  implicit class InProgressResultStoring(result:Result) extends SessionKeys {
    def storeInSession[B <: InprogressApplication[B]](application:B) = {
      val (encryptedSessionPayloadValue, encryptedSessionPayloadIVValue) =
        encryptionService.encrypt(serialiser.toJson(application))
      result.withCookies(
        createSecureCookie(sessionPayloadKey, encryptedSessionPayloadValue.filter(_ >= ' ')),
        createSecureCookie(sessionPayloadKeyIV, encryptedSessionPayloadIVValue))
    }

    def refreshSession() = {
      val (encryptedSessionTokenValue, encryptedSessionTokenIVValue) =
        encryptionService.encrypt(DateTime.now.toString())
      result.withCookies(
        createSecureCookie(sessionTokenKey, encryptedSessionTokenValue.filter(_ >= ' ')),
        createSecureCookie(sessionTokenKeyIV, encryptedSessionTokenIVValue))
    }
  }
}