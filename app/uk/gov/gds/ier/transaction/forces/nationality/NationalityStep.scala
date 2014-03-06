package uk.gov.gds.ier.transaction.forces.nationality

import controllers.step.forces.routes.AddressController
import controllers.step.forces.DateOfBirthController
import controllers.step.forces.routes.NationalityController
import controllers.routes.ExitController
import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.validation._
import play.api.mvc.Call
import play.api.templates.Html
import uk.gov.gds.ier.service.IsoCountryService
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.security.{EncryptionKeys, EncryptionService}
import uk.gov.gds.ier.model.InprogressForces
import uk.gov.gds.ier.step.{ForcesStep, Routes, Exit}

class NationalityStep @Inject ()(
    val serialiser: JsonSerialiser,
    val isoCountryService: IsoCountryService,
    val config: Config,
    val encryptionService : EncryptionService,
    val encryptionKeys : EncryptionKeys)
  extends ForcesStep
    with NationalityForms
    with NationalityMustache {

  val validation = nationalityForm
  val previousRoute = Some(AddressController.get)

  val routes = Routes(
    get = NationalityController.get,
    post = NationalityController.post,
    editGet = NationalityController.editGet,
    editPost = NationalityController.editPost
  )

  def template(
      form: InProgressForm[InprogressForces],
      postEndpoint: Call,
      backEndpoint:Option[Call]): Html = {
    nationalityMustache(form.form, postEndpoint, backEndpoint)
  }

  def nextStep(currentState: InprogressForces) = {

    if (currentState.nationality.flatMap(_.noNationalityReason) == None) {
      val franchises = currentState.nationality match {
        case Some(nationality) => isoCountryService.getFranchises(nationality)
        case None => List.empty
      }

      franchises match {
        case Nil => Exit(ExitController.noFranchise)
        case list => DateOfBirthController.dateOfBirthStep
      }
    }
    else DateOfBirthController.dateOfBirthStep
  }
}
