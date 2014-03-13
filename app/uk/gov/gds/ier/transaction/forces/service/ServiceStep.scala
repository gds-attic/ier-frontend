package uk.gov.gds.ier.transaction.forces.service

import controllers.step.forces.RankController
import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.model._
import play.api.templates.Html
import controllers.step.forces.routes._

import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.security.EncryptionService
import play.api.mvc.Call
import uk.gov.gds.ier.step.{ForcesStep, Routes}
import uk.gov.gds.ier.validation.InProgressForm

class ServiceStep @Inject ()(
    val serialiser: JsonSerialiser,
    val config: Config,
    val encryptionService : EncryptionService)

  extends ForcesStep
    with ServiceForms
    with ServiceMustache {

  val validation = serviceForm
  val previousRoute = Some(NinoController.get)

  val routes = Routes(
    get = ServiceController.get,
    post = ServiceController.post,
    editGet = ServiceController.editGet,
    editPost = ServiceController.editPost
  )

  def template(
      form: InProgressForm[InprogressForces],
      postEndpoint: Call,
      backEndpoint:Option[Call]): Html = {

    serviceMustache(form.form, postEndpoint, backEndpoint)
  }

  override def postSuccess(currentState: InprogressForces):InprogressForces = {
    currentState.service match {
      case Some(Service(Some(ServiceType.RoyalNavy),_))  =>
        currentState.copy(service = Some(Service(Some(ServiceType.RoyalNavy), None)))
      case Some(Service(Some(ServiceType.RoyalAirForce),_)) =>
        currentState.copy(service = Some(Service(Some(ServiceType.RoyalAirForce), None)))
      case _ =>  currentState
    }
  }

  def nextStep(currentState: InprogressForces) = {
    RankController.rankStep
  }
}