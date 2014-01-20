package uk.gov.gds.ier.controller

import play.api.mvc._
import com.google.inject.Inject
import controllers._
import uk.gov.gds.ier.serialiser.{WithSerialiser, JsonSerialiser}
import scala.Some
import org.slf4j.LoggerFactory
import uk.gov.gds.ier.session.{SessionCleaner, SessionHandling}
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.logging.Logging
import uk.gov.gds.ier.guice.{WithEncryption, WithConfig}
import uk.gov.gds.ier.security.{EncryptionKeys, EncryptionService}
import uk.gov.gds.ier.mustache.GovukMustache

class RegisterToVoteController @Inject() (val serialiser: JsonSerialiser,
                                          val config: Config,
                                          val encryptionService : EncryptionService,
                                          val encryptionKeys : EncryptionKeys)
    extends Controller
    with WithSerialiser
    with WithConfig
    with Logging
    with SessionCleaner
    with WithEncryption
    with GovukMustache {

  def registerToVote = Action {
    Ok(RegisterToVote.ordinaryStartPage())
  }

  def registerToVoteStart = NewSession requiredFor {
    request =>
      Redirect(step.routes.CountryController.get)
  }

  def registerToVoteOverseas = Action {
    Ok(RegisterToVote.overseasStartPage())
  }

  def registerToVoteOverseasStart = NewSession requiredFor {
    request =>
      Redirect(step.overseas.routes.PreviouslyRegisteredController.get)
  }
}

