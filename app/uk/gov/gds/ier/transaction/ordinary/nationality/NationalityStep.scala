package uk.gov.gds.ier.transaction.ordinary.nationality

import controllers.step.routes.CountryController
import controllers.step.ordinary.DateOfBirthController
import controllers.step.ordinary.routes.NationalityController
import controllers.routes.ExitController
import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.validation._
import play.api.mvc.Call
import play.api.templates.Html
import uk.gov.gds.ier.service.IsoCountryService
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.security.EncryptionService
import uk.gov.gds.ier.model.InprogressOrdinary
import uk.gov.gds.ier.step.{OrdinaryStep, Routes, GoTo}

class NationalityStep @Inject ()(val serialiser: JsonSerialiser,
                                       val isoCountryService: IsoCountryService,
                                       val config: Config,
                                       val encryptionService : EncryptionService)
  extends OrdinaryStep
  with NationalityForms
  with NationalityMustache {

  val validation = nationalityForm
  val previousRoute = Some(CountryController.get)

  val routes = Routes(
    get = NationalityController.get,
    post = NationalityController.post,
    editGet = NationalityController.editGet,
    editPost = NationalityController.editPost
  )

  def template(
      form: ErrorTransformForm[InprogressOrdinary],
      postEndpoint: Call,
      backEndpoint:Option[Call]): Html = Html.empty

  override def templateWithApplication(
      form: ErrorTransformForm[InprogressOrdinary],
      call: Call,
      backUrl: Option[Call]):InprogressOrdinary => Html = {
    application:InprogressOrdinary =>
      nationalityMustache(application, form, call, backUrl)
  }

  def nextStep(currentState: InprogressOrdinary) = {
    if (currentState.nationality.flatMap(_.noNationalityReason) == None) {
      val franchises = currentState.nationality match {
        case Some(nationality) => isoCountryService.getFranchises(nationality)
        case None => List.empty
      }

      franchises match {
        case Nil => GoTo(ExitController.noFranchise)
        case list => DateOfBirthController.dateOfBirthStep
      }
    }
    else DateOfBirthController.dateOfBirthStep
  }
}

