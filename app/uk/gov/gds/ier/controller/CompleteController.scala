package uk.gov.gds.ier.controller

import play.api.mvc._
import com.google.inject.Inject
import uk.gov.gds.ier.service.PlacesService
import views._
import controllers._
import uk.gov.gds.ier.serialiser.{WithSerialiser, JsonSerialiser}
import scala.Some
import uk.gov.gds.common.model.{Ero, LocalAuthority}
import org.slf4j.LoggerFactory
import uk.gov.gds.ier.session.{SessionCleaner, SessionHandling}
import uk.gov.gds.ier.guice.{WithEncryption, WithConfig}
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.logging.Logging
import uk.gov.gds.ier.security.{EncryptionKeys, EncryptionService}
import uk.gov.gds.ier.transaction.complete.CompleteMustache

class CompleteController @Inject() (val serialiser: JsonSerialiser,
                                    placesService:PlacesService,
                                    val config: Config,
                                    val encryptionService : EncryptionService,
                                    val encryptionKeys : EncryptionKeys)
    extends Controller
    with WithSerialiser
    with WithConfig
    with Logging
    with SessionCleaner
    with WithEncryption
    with CompleteMustache {

  def complete = ClearSession requiredFor {
    implicit request =>
      val authority = request.flash.get("postcode") match {
        case Some("") => None
        case Some(postCode) => placesService.lookupAuthority(postCode)
        case None => None
      }
      val refNum = request.flash.get("refNum")

      Ok(Complete.completePage(authority, refNum))
  }

  def fakeComplete = Action {
    val authority = Some(LocalAuthority("Tower Hamlets", Ero(), "00BG", "E09000030"))
    Ok(Complete.completePage(authority, Some("123456")))
  }

}
