package uk.gov.gds.ier.transaction.overseas.previouslyRegistered

import com.google.inject.Inject
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.security.EncryptionService
import uk.gov.gds.ier.model.{PreviouslyRegistered, InprogressOverseas}
import uk.gov.gds.ier.validation.InProgressForm
import play.api.mvc.Call
import play.api.templates.Html
import controllers.step.overseas.routes.{PreviouslyRegisteredController, DateOfBirthController}
import controllers.step.overseas.{LastRegisteredToVoteController, DateLeftUkController}
import uk.gov.gds.ier.step.OverseaStep
import uk.gov.gds.ier.step.Routes

class PreviouslyRegisteredStep @Inject() (
    val serialiser: JsonSerialiser,
    val config: Config,
    val encryptionService: EncryptionService)
  extends OverseaStep
  with PreviouslyRegisteredForms
  with PreviousRegisteredMustache {

  val validation = previouslyRegisteredForm
  val routes = Routes(
    get = PreviouslyRegisteredController.get,
    post = PreviouslyRegisteredController.post,
    editGet = PreviouslyRegisteredController.editGet,
    editPost = PreviouslyRegisteredController.editPost
  )
  val previousRoute = Some(DateOfBirthController.get)

  def nextStep(currentState: InprogressOverseas) = {
    currentState.previouslyRegistered match {
      case Some(PreviouslyRegistered(false)) => {
        LastRegisteredToVoteController.lastRegisteredToVoteStep
      }
      case Some(PreviouslyRegistered(true)) => {
        DateLeftUkController.dateLeftUkStep
      }
      case _ => this
    }
  }

  def template(
      form: InProgressForm[InprogressOverseas],
      postEndpoint: Call,
      backEndpoint:Option[Call]): Html = {
    previousRegisteredMustache(form.form, postEndpoint, backEndpoint)
  }
}
