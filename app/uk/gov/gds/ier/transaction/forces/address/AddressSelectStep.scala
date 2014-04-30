package uk.gov.gds.ier.transaction.forces.address

import controllers.step.forces.routes._
import controllers.step.forces.{PreviousAddressFirstController, NationalityController}
import com.google.inject.Inject
import play.api.mvc.Call
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.model.{
  LastUkAddress,
  Addresses,
  PossibleAddress}
import uk.gov.gds.ier.security.EncryptionService
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.service.{AddressService, WithAddressService}
import uk.gov.gds.ier.step.{ForcesStep, Routes}
import uk.gov.gds.ier.transaction.forces.InprogressForces

class AddressSelectStep @Inject() (
    val serialiser: JsonSerialiser,
    val config: Config,
    val encryptionService: EncryptionService,
    val addressService: AddressService)
  extends ForcesStep
  with AddressSelectMustache
  with AddressForms
  with WithAddressService {

  val validation = selectStepForm

  val routes = Routes(
    get = AddressSelectController.get,
    post = AddressSelectController.post,
    editGet = AddressSelectController.editGet,
    editPost = AddressSelectController.editPost
  )

  def nextStep(currentState: InprogressForces) = {
    val hasUkAddress = Some(true)

    currentState.address match {
      case Some(LastUkAddress(`hasUkAddress`,_))
          => PreviousAddressFirstController.previousAddressFirstStep
      case _ => NationalityController.nationalityStep
    }
  }

  override val onSuccess = TransformApplication { currentState =>
    val addressWithAddressLine = currentState.address.map { lastUkAddress =>
      lastUkAddress.copy (
        address = lastUkAddress.address.map(addressService.fillAddressLine(_).copy(manualAddress = None))
      )
    }

    currentState.copy(
      address = addressWithAddressLine,
      possibleAddresses = None
    )
  } andThen GoToNextIncompleteStep()
}
