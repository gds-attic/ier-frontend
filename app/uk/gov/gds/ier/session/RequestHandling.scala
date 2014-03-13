package uk.gov.gds.ier.session

import uk.gov.gds.ier.guice.WithEncryption
import uk.gov.gds.ier.serialiser.WithSerialiser

trait RequestHandling {
    self: WithEncryption with WithSerialiser =>

    private[session] implicit class InProgressRequest(request: play.api.mvc.Request[_])
      extends SessionKeys {
        def getToken = for {
            cookie <- request.cookies.get(sessionTokenKey)
            cookieInitVec <- request.cookies.get(sessionTokenKeyIV)
        } yield encryptionService.decrypt(cookie.value, cookieInitVec.value)

        def getApplication[T](implicit manifest: Manifest[T]): Option[T] = for {
            cookie <- request.cookies.get(sessionPayloadKey)
            cookieInitVec <- request.cookies.get(sessionPayloadKeyIV)
        } yield serialiser.fromJson[T](
            encryptionService.decrypt(cookie.value,  cookieInitVec.value))
    }
}
