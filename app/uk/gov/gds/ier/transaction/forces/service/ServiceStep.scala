package uk.gov.gds.ier.transaction.forces.service

import controllers.step.forces.RankController
import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.model._
import controllers.step.forces.routes._
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.security.EncryptionService
import uk.gov.gds.ier.step.{ForcesStep, Routes}
import uk.gov.gds.ier.transaction.forces.InprogressForces
import uk.gov.gds.ier.assets.RemoteAssets

class ServiceStep @Inject ()(
    val serialiser: JsonSerialiser,
    val config: Config,
    val encryptionService : EncryptionService,
    val remoteAssets: RemoteAssets)

  extends ForcesStep
    with ServiceForms
    with ServiceMustache {

  val validation = serviceForm

  val routes = Routes(
    get = ServiceController.get,
    post = ServiceController.post,
    editGet = ServiceController.editGet,
    editPost = ServiceController.editPost
  )

  override val onSuccess = TransformApplication { currentState =>
    currentState.service match {
      case Some(Service(Some(ServiceType.RoyalNavy),_))  =>
        currentState.copy(service = Some(Service(Some(ServiceType.RoyalNavy), None)))
      case Some(Service(Some(ServiceType.RoyalAirForce),_)) =>
        currentState.copy(service = Some(Service(Some(ServiceType.RoyalAirForce), None)))
      case _ =>  currentState
    }
  } andThen GoToNextIncompleteStep()

  def nextStep(currentState: InprogressForces) = {
    RankController.rankStep
  }
}
