package uk.gov.gds.ier.transaction.overseas.openRegister

import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.security.EncryptionService
import uk.gov.gds.ier.step.{OverseaStepWithNewMustache, Routes}
import controllers.step.overseas.routes._
import scala.Some
import controllers.step.overseas.WaysToVoteController
import uk.gov.gds.ier.transaction.overseas.InprogressOverseas

class OpenRegisterStep @Inject ()(
    val serialiser: JsonSerialiser,
    val config: Config,
    val encryptionService : EncryptionService)

  extends OverseaStepWithNewMustache
  with OpenRegisterForms
  with OpenRegisterMustache {

  val validation = openRegisterForm
  val previousRoute = Some(AddressController.get)

  val routes = Routes(
    get = OpenRegisterController.get,
    post = OpenRegisterController.post,
    editGet = OpenRegisterController.editGet,
    editPost = OpenRegisterController.editPost
  )

  def nextStep(currentState: InprogressOverseas) = {
    WaysToVoteController.waysToVoteStep
  }

  override def isStepComplete(currentState: InprogressOverseas) = {
    currentState.openRegisterOptin.isDefined
  }
}
