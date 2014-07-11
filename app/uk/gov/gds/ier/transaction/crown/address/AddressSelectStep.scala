package uk.gov.gds.ier.transaction.crown.address

import controllers.step.crown.routes._
import controllers.step.crown.{PreviousAddressFirstController, NationalityController}
import com.google.inject.Inject
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.model.{HasAddressOption, LastAddress}
import uk.gov.gds.ier.security.EncryptionService
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.service.{AddressService, WithAddressService}
import uk.gov.gds.ier.step.{CrownStep, Routes}
import uk.gov.gds.ier.transaction.crown.InprogressCrown
import uk.gov.gds.ier.assets.RemoteAssets

class AddressSelectStep @Inject() (
    val serialiser: JsonSerialiser,
    val config: Config,
    val encryptionService: EncryptionService,
    val addressService: AddressService,
    val remoteAssets: RemoteAssets)
  extends CrownStep
  with AddressSelectMustache
  with AddressForms
  with WithAddressService {

  val validation = addressForm

  val routing = Routes(
    get = AddressSelectController.get,
    post = AddressSelectController.post,
    editGet = AddressSelectController.editGet,
    editPost = AddressSelectController.editPost
  )

  def nextStep(currentState: InprogressCrown) = {
    currentState.address.flatMap(_.hasAddress) match {
      case Some(HasAddressOption.YesAndLivingThere) | Some(HasAddressOption.YesAndNotLivingThere)
        => PreviousAddressFirstController.previousAddressFirstStep
      case _ => NationalityController.nationalityStep
    }
  }

  override val onSuccess = TransformApplication { application =>
    val address = application.address.flatMap {_.address}
    val addressWithAddressLine =  address.map (address => addressService.fillAddressLine(address))

    application.copy(
      address = Some(LastAddress(
        hasAddress = application.address.flatMap {_.hasAddress},
        address = addressWithAddressLine
      )),
      possibleAddresses = None
    )
  } andThen GoToNextIncompleteStep()
}
