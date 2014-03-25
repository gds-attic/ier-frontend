package uk.gov.gds.ier.transaction.crown.address

import controllers.step.crown.routes._
import com.google.inject.Inject
import uk.gov.gds.ier.model.InprogressCrown
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.validation._
import play.api.mvc.Call
import play.api.templates.Html
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.security.EncryptionService
import uk.gov.gds.ier.service.AddressService

import uk.gov.gds.ier.step.{Routes, CrownStep}

class AddressFirstStep @Inject ()(
    val serialiser: JsonSerialiser,
    val config: Config,
    val encryptionService : EncryptionService,
    val addressService: AddressService)
  extends CrownStep
  with AddressFirstMustache
  with AddressFirstForms {

  val validation = addressFirstForm
  val previousRoute = Some(StatementController.get)

  val routes = Routes(
    get = AddressFirstController.get,
    post = AddressFirstController.post,
    editGet = AddressFirstController.editGet,
    editPost = AddressFirstController.editPost
  )

  def nextStep(currentState: InprogressCrown) = {
    if (currentState.address.flatMap(_.hasUkAddress) == Some(true)) {
      controllers.step.crown.AddressController.addressStep
    } else {
      controllers.step.crown.AddressController.addressStep
    }
  }

  def template(
      form: InProgressForm[InprogressCrown],
      call:Call,
      backUrl: Option[Call]): Html = {

    addressFirstStepMustache(
      form.form,
      call.url,
      backUrl.map(_.url)
    )
  }
}
