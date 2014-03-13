package uk.gov.gds.ier.transaction.country

import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.validation._
import uk.gov.gds.ier.validation.constraints.CountryConstraints
import play.api.mvc.Call
import uk.gov.gds.ier.model.{InprogressOrdinary, Country}
import play.api.templates.Html
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.security.EncryptionService
import uk.gov.gds.ier.step.{OrdinaryStep, Routes, Exit}
import controllers.step.ordinary.NationalityController
import controllers.step.routes.CountryController
import controllers.routes.ExitController
import controllers.routes.RegisterToVoteController

class CountryStep @Inject ()(
    val serialiser: JsonSerialiser,
    val config:Config,
    val encryptionService : EncryptionService)
  extends OrdinaryStep
  with CountryConstraints
  with CountryForms
  with CountryMustache {

  val validation = countryForm
  val previousRoute = None

  val routes = Routes(
    get = CountryController.get,
    post = CountryController.post,
    editGet = CountryController.editGet,
    editPost = CountryController.editPost
  )

  def template(
      form:InProgressForm[InprogressOrdinary],
      call:Call,
      backUrl: Option[Call]): Html = {
    countryMustache(form.form, call)
  }

  def nextStep(currentState: InprogressOrdinary) = {
    currentState.country match {
      case Some(Country("Northern Ireland", _)) => Exit(ExitController.northernIreland)
      case Some(Country("Scotland", _)) => Exit(ExitController.scotland)
      case Some(Country("British Islands", _)) => Exit(ExitController.britishIslands)
      case Some(Country(_, true)) => Exit(RegisterToVoteController.registerToVoteOverseasStart)
      case _ => NationalityController.nationalityStep
    }
  }
}
