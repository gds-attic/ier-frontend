package uk.gov.gds.ier.transaction.overseas.confirmation

import uk.gov.gds.ier.model.InprogressOverseas
import uk.gov.gds.ier.step.ConfirmationStepController
import uk.gov.gds.ier.security.{EncryptionKeys, EncryptionService}
import uk.gov.gds.ier.serialiser.JsonSerialiser
import uk.gov.gds.ier.service.IerApiService
import uk.gov.gds.ier.config.Config
import uk.gov.gds.ier.validation.InProgressForm
import controllers.step.overseas.routes.OverseasConfirmationController
import controllers.step.overseas.routes.PreviouslyRegisteredController
import controllers.routes.CompleteController
import com.google.inject.Inject
import uk.gov.gds.ier.step.Routes

class ConfirmationStep @Inject() (val encryptionKeys: EncryptionKeys,
                                  val encryptionService: EncryptionService,
                                  val config: Config,
                                  val serialiser: JsonSerialiser,
                                  ierApi: IerApiService)
  extends ConfirmationStepController[InprogressOverseas]
  with ConfirmationForms
  with ConfirmationMustache {

  def factoryOfT() = InprogressOverseas()

  val routes = Routes(
    get = OverseasConfirmationController.get,
    post = OverseasConfirmationController.post,
    editGet = OverseasConfirmationController.get,
    editPost = OverseasConfirmationController.post
  )

  val validation = confirmationForm
  val previousRoute = Some(PreviouslyRegisteredController.get)

  def template(form:InProgressForm[InprogressOverseas]) = {
    Confirmation.confirmationPage(
      form,
      previousRoute.map(_.url).getOrElse("#"),
      routes.post.url
    )
  }

  def get = ValidSession requiredFor {
    request => application =>
      Ok(template(InProgressForm(validation.fillAndValidate(application))))
  }

  def post = ValidSession requiredFor {
    request => application =>
      validation.fillAndValidate(application).fold(
        hasErrors => {
          Ok(template(InProgressForm(hasErrors)))
        },
        validApplication => {
          val refNum = ierApi.generateReferenceNumber(validApplication)
          val remoteClientIP = request.headers.get("X-Real-IP")

          ierApi.submitOverseasApplication(remoteClientIP, validApplication, Some(refNum))
          Redirect(CompleteController.complete()).flashing(
            "refNum" -> refNum,
            "postcode" -> "SW1A 1AA"
          )
        }
      )
  }
}
